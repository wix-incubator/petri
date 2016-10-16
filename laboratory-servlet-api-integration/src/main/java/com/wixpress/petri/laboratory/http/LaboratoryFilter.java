package com.wixpress.petri.laboratory.http;

import com.wix.hoopoe.koboshi.cache.ReadOnlyTimestampedLocalCache;
import com.wix.hoopoe.koboshi.cache.defaults.ResilientCaches;
import com.wix.hoopoe.koboshi.registry.RemoteDataFetcherRegistry;
import com.wix.hoopoe.koboshi.remote.RemoteDataSource;
import com.wix.hoopoe.koboshi.servlet.ResilientCacheRegistryEndpoint$;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.amplitude.AmplitudeAdapter;
import com.wixpress.petri.experiments.domain.ConductibleExperiments;
import com.wixpress.petri.experiments.domain.ExternalDataFetchers;
import com.wixpress.petri.experiments.domain.FilterTypeIdResolver;
import com.wixpress.petri.laboratory.*;
import com.wixpress.petri.petri.*;
import scala.Tuple2;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;


/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 10/6/14
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class LaboratoryFilter implements Filter {

    public static final String PETRI_USER_INFO_STORAGE = "petri_userInfoStorage";
    public static final String PETRI_LABORATORY = "petri_laboratory";

    private ServerMetricsReporter metricsReporter;
    private PetriClient petriClient;
    private LaboratoryProperties laboratoryProperties;
    private FilterParametersExtractorsConfig filterParametersExtractorsConfig;
    private UserRequestPetriClient userRequestPetriClient;
    private LaboratoryTopology laboratoryTopology;
    private CompositeTestGroupAssignmentTracker tracker = CompositeTestGroupAssignmentTracker.create(new BILoggingTestGroupAssignmentTracker(new JodaTimeClock()));
    private ReadOnlyTimestampedLocalCache<ConductibleExperiments> cache;


    public LaboratoryFilter() {
    }

    private static class ByteArrayServletStream extends ServletOutputStream {

        ByteArrayOutputStream baos;

        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        public void write(int param) throws IOException {
            baos.write(param);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        RequestScopedUserInfoStorage storage = userInfoStorage(httpServletRequest);

        Laboratory laboratory = laboratory(storage);

        httpServletRequest.getSession(true).setAttribute(PETRI_LABORATORY, laboratory);
        httpServletRequest.getSession().setAttribute(PETRI_USER_INFO_STORAGE, storage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final HttpServletResponseWrapper wrappedResponse = new CachingHttpResponse(resp, new ByteArrayServletStream(baos), new PrintWriter(baos));

        chain.doFilter(req, wrappedResponse);

        final UserInfo userInfo = storage.read();
        final UserInfo originalUserInfo = storage.readOriginal();
        userInfo.saveExperimentState(new CookieExperimentStateStorage(wrappedResponse, laboratoryProperties.getPetriCookieName()), originalUserInfo);
        if (laboratoryTopology.isWriteStateToServer()) {
            userInfo.saveExperimentState(new ServerStateExperimentStateStorage(petriClient), originalUserInfo);
        }
        resp.getOutputStream().write(baos.toByteArray());
    }


    private Laboratory laboratory(UserInfoStorage storage) throws MalformedURLException {
        Experiments experiments = new CachedExperiments(new TransientCacheExperimentSource(cache, new JodaTimeClock()));

        return new TrackableLaboratory(experiments, tracker, storage, new DefaultErrorHandler(), 50, metricsReporter, userRequestPetriClient, laboratoryTopology, new ExternalDataFetchers(null));
    }

    private RequestScopedUserInfoStorage userInfoStorage(HttpServletRequest httpServletRequest) {
        return new RequestScopedUserInfoStorage(
                new HttpRequestUserInfoExtractor(
                        httpServletRequest, laboratoryProperties.getPetriCookieName(), filterParametersExtractorsConfig));
    }

    public void destroy() {
        metricsReporter.stopScheduler();
    }


    public void init(FilterConfig filterConfig) throws ServletException {
        final ServletContext context = filterConfig.getServletContext();
        laboratoryProperties = new DefaultLaboratoryProperties(context);
        readProperties();

        filterParametersExtractorsConfig = FilterParametersExtractorsConfig.readConfig(context);

        String amplitudeUrl = laboratoryProperties.getProperty("amplitude.url");
        String amplitudeApiKey = laboratoryProperties.getProperty("amplitude.api.key");
        String amplitudeTimeoutMs = laboratoryProperties.getProperty("amplitude.timeout.ms");

        if (amplitudeUrl != null && amplitudeApiKey != null) {
            tracker = tracker.add(new AmplitudeTestGroupAssignmentTracker(
                    AmplitudeAdapter.create(amplitudeUrl, amplitudeApiKey, amplitudeTimeoutMs)));
        }

        try {
            petriClient = PetriRPCClient.makeFor(laboratoryTopology.getPetriUrl());
            userRequestPetriClient = PetriRPCClient.makeUserRequestFor(laboratoryTopology.getPetriUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        startMetricsReporterScheduler(laboratoryTopology.getReportsScheduleTimeInMillis());

        FilterTypeIdResolver.useDynamicFilterClassLoading();
        cache = setupKoboshiCache(context);
    }

    private ReadOnlyTimestampedLocalCache<ConductibleExperiments> setupKoboshiCache(final ServletContext context) {
        File koboshiCacheFolder = getKoboshiCacheFolder();
        final Tuple2<com.wix.hoopoe.koboshi.cache.ResilientCaches, RemoteDataFetcherRegistry> cachesAndRegistry =
                ResilientCaches.resilientCachesAndRegistry(koboshiCacheFolder);

        context.setAttribute(ResilientCacheRegistryEndpoint$.MODULE$.RegistryKey(), cachesAndRegistry._2());

        return cachesAndRegistry._1()
                .aResilientCacheBuilder(ConductibleExperiments.class, new PetriClientRemoteDataSource(petriClient))
                .withTimestampedData()
                .build();
    }

    private File getKoboshiCacheFolder() {
        File koboshiCacheFolder = Paths.get(System.getProperty("user.home"), "koboshi").toFile();
        koboshiCacheFolder.mkdirs();
        return koboshiCacheFolder;
    }

    private void readProperties() {
        final String petriUrl = laboratoryProperties.getProperty("petri.url");
        //off by default so as not to incur overhead without users being explicitly aware of it
        final Boolean writeStateToServer = Boolean.valueOf(laboratoryProperties.getProperty("petri.writeStateToServer", "false"));
        final String reporterInterval = laboratoryProperties.getProperty("reporter.interval", "300000");
        laboratoryTopology = new LaboratoryTopology() {

            @Override
            public String getPetriUrl() {
                return petriUrl;
            }

            @Override
            public Long getReportsScheduleTimeInMillis() {
                long scheduleReportInterval = Long.parseLong(reporterInterval);
                return scheduleReportInterval;
            }

            @Override
            public boolean isWriteStateToServer() {
                return writeStateToServer;
            }

            @Override
            public String getAuthorizationServiceUrl() {
                return "";
            }

        };
    }

    private void startMetricsReporterScheduler(Long reportsScheduleTimeInMillis) {
        metricsReporter = new ServerMetricsReporter(petriClient, Executors.newScheduledThreadPool(5), reportsScheduleTimeInMillis);
        metricsReporter.startScheduler();
    }

    private static class CachingHttpResponse extends HttpServletResponseWrapper {
        private final ServletOutputStream sos;
        private final PrintWriter pw;

        public CachingHttpResponse(ServletResponse resp, ServletOutputStream sos, PrintWriter pw) {
            super((HttpServletResponse) resp);
            this.sos = sos;
            this.pw = pw;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return sos;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return pw;
        }
    }

    private static class PetriClientRemoteDataSource implements RemoteDataSource<ConductibleExperiments> {
        private final PetriClient petriClient;

        public PetriClientRemoteDataSource(PetriClient petriClient) {
            this.petriClient = petriClient;
        }

        @Override
        public ConductibleExperiments fetch() {
            return new ConductibleExperiments(petriClient.fetchActiveExperiments());
        }
    }
}
