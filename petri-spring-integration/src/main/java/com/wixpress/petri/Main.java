package com.wixpress.petri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.*;
import org.apache.commons.dbcp.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 9/7/14
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String... args) {
        try {

            // TODO: move these into a property file

            final String username = "auser";
            final String password = "sa";
            final String url = "jdbc:h2:mem:test;MODE=MySQL";
            final int port = 9011;

            // TODO: refactor this into an assembly layer
            BasicDataSource ds = new BasicDataSource();
            ds.setUsername(username);
            ds.setPassword(password);
            ds.setUrl(url);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

            ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
            MappingErrorHandler mappingErrorHandler = new ConsoleMappingErrorHandler();
            PetriMapper experimentMapper = new ExperimentMapper(objectMapper,mappingErrorHandler);
            OriginalIDAwarePetriDao<Experiment, ExperimentSnapshot> experimentsDao = new JdbcExperimentsDao(jdbcTemplate, experimentMapper);
            Clock clock = new JodaTimeClock();
            PetriMapper specMapper = new SpecMapper(objectMapper,mappingErrorHandler);
            DeleteEnablingPetriDao<ExperimentSpec, ExperimentSpec> specsDao = new JdbcSpecsDao(jdbcTemplate,specMapper);
            PetriNotifier notifier = new NoopPetriNotifier();
            PetriRpcServer petri = new PetriRpcServer(experimentsDao,clock,specsDao,notifier);

            JsonRPCServer rpcServer = new JsonRPCServer(petri, objectMapper, port,PetriClient.class);
            rpcServer.start();
            // TODO: rpcServer.join() -> In order to do this run the server on a different thread when testing.

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static class NoopPetriNotifier implements PetriNotifier {
        @Override
        public void notify(String title, String message, String user) {
            // do nothing...
        }
    }

    private static class ConsoleMappingErrorHandler implements MappingErrorHandler {
        @Override
        public void handleError(String string, String entityDescription, IOException e) {
            e.printStackTrace();      // TODO: Log these
        }
    }
}
