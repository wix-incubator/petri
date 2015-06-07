package com.wixpress.petri.laboratory.http;

import com.wixpress.petri.laboratory.ExceptionType;
import com.wixpress.petri.laboratory.ExperimentStateStorage;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.RAMPetriClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponseWrapper;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Created by talyag on 6/4/15.
 */
public class ServerStateExperimentStateStorage implements ExperimentStateStorage {
    private static final Logger logger = LoggerFactory.getLogger(ServerStateExperimentStateStorage.class);
    private PetriClient petriClient;
    private ThreadPoolExecutor executorService;

    public ServerStateExperimentStateStorage(PetriClient petriClient) {
        this.petriClient = petriClient;
        this.executorService = new ThreadPoolExecutor(5, 5, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.DiscardPolicy());
    }

    @Override
    public void storeAnonymousExperimentsLog(String key, String experimentsLog) {

    }

    @Override
    public void storeUserExperimentsLog(final UUID userInSessionId, final UUID userIdToPersistBy, final String experimentsLog) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    petriClient.saveUserState(userIdToPersistBy, experimentsLog);
                } catch(Exception e) {
                    logger.error(String.format("Unexpected exception while writing user state to server for user %s with state: %s (user in session is %s) ", userIdToPersistBy, experimentsLog, userInSessionId), e, ExceptionType.ErrorWritingToServer);
                }

            }
        });
    }

    @Override
    public void storeExperimentsOverrides(Map<String, String> overrides) {

    }
}
