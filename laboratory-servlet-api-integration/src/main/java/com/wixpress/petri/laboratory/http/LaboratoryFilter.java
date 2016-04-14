package com.wixpress.petri.laboratory.http;

import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.FilterTypeIdResolver;
import com.wixpress.petri.laboratory.*;
import com.wixpress.petri.petri.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
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
    private UserRequestPetriClient userRequestPetriClient;
    private PetriTopology petriTopology;

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
    }


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        RequestScopedUserInfoStorage storage = userInfoStorage(httpServletRequest);

        Laboratory laboratory = laboratory(storage);

        httpServletRequest.getSession(true).setAttribute(PETRI_LABORATORY, laboratory);
        httpServletRequest.getSession().setAttribute(PETRI_USER_INFO_STORAGE, storage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final HttpServletResponseWrapper response = new CachingHttpResponse(resp, new ByteArrayServletStream(baos), new PrintWriter(baos));

        chain.doFilter(req, response);

        final UserInfo userInfo = storage.read();
        final UserInfo originalUserInfo = storage.readOriginal();
        userInfo.saveExperimentState(new CookieExperimentStateStorage(response), originalUserInfo);
        if (petriTopology.isWriteStateToServer()) {
            userInfo.saveExperimentState(new ServerStateExperimentStateStorage(petriClient), originalUserInfo);
        }
        resp.getOutputStream().write(baos.toByteArray());
    }


    private Laboratory laboratory(UserInfoStorage storage) throws MalformedURLException {
        Experiments experiments = new CachedExperiments(new PetriClientExperimentSource(petriClient));
        TestGroupAssignmentTracker tracker = new BILoggingTestGroupAssignmentTracker(new JodaTimeClock());
        return new TrackableLaboratory(experiments, tracker, storage, new DefaultErrorHandler(), 50, metricsReporter, userRequestPetriClient, petriTopology);
    }

    private RequestScopedUserInfoStorage userInfoStorage(HttpServletRequest httpServletRequest) {
        return new RequestScopedUserInfoStorage(
                new HttpRequestUserInfoExtractor(
                        httpServletRequest));
    }

    public void destroy() {
        metricsReporter.stopScheduler();
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        readProperties(filterConfig);

        try {
            petriClient = PetriRPCClient.makeFor(petriTopology.getPetriUrl());
            userRequestPetriClient = PetriRPCClient.makeUserRequestFor(petriTopology.getPetriUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        startMetricsReporterScheduler(petriTopology.getReportsScheduleTimeInMillis());

        FilterTypeIdResolver.useDynamicFilterClassLoading();
    }

    private void readProperties(FilterConfig filterConfig) {
        PetriProperties petriProperties = new PetriProperties(filterConfig.getServletContext());
        final String petriUrl = petriProperties.getProperty("petri.url");
        //off by default so as not to incur overhead without users being explicitly aware of it
        final Boolean writeStateToServer = Boolean.valueOf(petriProperties.getProperty("petri.writeStateToServer", "false"));
        final String reporterInterval = petriProperties.getProperty("reporter.interval", "300000");
        petriTopology = new PetriTopology() {

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

}
