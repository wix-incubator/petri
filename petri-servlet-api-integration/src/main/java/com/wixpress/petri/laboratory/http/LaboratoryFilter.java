package com.wixpress.petri.laboratory.http;

import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.HostResolver;
import com.wixpress.petri.laboratory.*;
import com.wixpress.petri.petri.JodaTimeClock;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.ServerMetricsReporter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Properties;
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
    private final PetriProperties petriProperties = new PetriProperties();
    private String petriUrl;

    private static final long SCHEDULE_REPORT_INTERVAL = 300000l;
    private  ServerMetricsReporter metricsReporter ;
    private PetriClient petriClient;

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
        UserInfoStorage storage = userInfoStorage(httpServletRequest);

        Laboratory laboratory = laboratory(storage);

        httpServletRequest.getSession(true).setAttribute(PETRI_LABORATORY, laboratory);
        httpServletRequest.getSession().setAttribute(PETRI_USER_INFO_STORAGE, storage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final HttpServletResponseWrapper response = new CachingHttpResponse(resp, new ByteArrayServletStream(baos), new PrintWriter(baos));

        chain.doFilter(req, response);

        final UserInfo ui = storage.read();
        ui.saveExperimentState(new CookieExperimentStateStorage(response));
        resp.getOutputStream().write(baos.toByteArray());
    }


    private Laboratory laboratory(UserInfoStorage storage) throws MalformedURLException {
        Experiments experiments = new CachedExperiments(new PetriClientExperimentSource(petriClient));
        TestGroupAssignmentTracker tracker = new BILoggingTestGroupAssignmentTracker(new JodaTimeClock());
        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            public void handle(String message, Throwable cause, ExceptionType exceptionType) {
                cause.printStackTrace();
            }

        };

        return new TrackableLaboratory(experiments, tracker, storage, errorHandler, 50, metricsReporter);
    }

    private RequestScopedUserInfoStorage userInfoStorage(HttpServletRequest httpServletRequest) {
        return new RequestScopedUserInfoStorage(
                new HttpRequestUserInfoExtractor(
                        httpServletRequest, new HostResolver()));
    }

    public void destroy() {
        metricsReporter.stopScheduler();
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        String laboratoryConfig = filterConfig.getInitParameter("laboratoryConfig");
        InputStream input = filterConfig.getServletContext().getResourceAsStream(laboratoryConfig);

        Properties p = petriProperties.fromStream(input);

        petriUrl = p.getProperty("petri.url");

        try {
            petriClient = PetriRPCClient.makeFor(petriUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String reporterInterval = p.getProperty("reporter.interval");
        long scheduleReportInterval = reporterInterval == null ?  SCHEDULE_REPORT_INTERVAL : Long.parseLong(reporterInterval);
        metricsReporter = new ServerMetricsReporter(petriClient , Executors.newScheduledThreadPool(5), scheduleReportInterval);
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
