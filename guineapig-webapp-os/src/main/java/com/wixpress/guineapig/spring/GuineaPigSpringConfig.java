package com.wixpress.guineapig.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.wixpress.guineapig.dao.MetaDataDao;
import com.wixpress.guineapig.dao.MySqlMetaDataDao;
import com.wixpress.guineapig.services.*;
import com.wixpress.guineapig.spi.GlobalGroupsManagementService;
import com.wixpress.guineapig.spi.HardCodedScopesProvider;
import com.wixpress.guineapig.spi.SpecExposureIdRetriever;
import com.wixpress.guineapig.spi.SupportedLanguagesProvider;
import com.wixpress.guineapig.topology.ClientTopology;
import com.wixpress.guineapig.topology.GuineapigDBTopology;
import com.wixpress.guineapig.topology.ServerTopology;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.JodaTimeClock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
@Import({VelocityConfig.class})
public class GuineaPigSpringConfig {

    @Bean
    public ClientTopology clientTopology() {
        return new ClientTopology();
    }

    @Bean
    public ServerTopology serverTopology() {
        return new ServerTopology();
    }

    @Bean
    public ExperimentEventPublisher experimentEventPublisher() {
        return new ExperimentEventPublisher((exception, action, source) -> exception.printStackTrace());
    }

    @Bean
    public DataSource dataSource(GuineapigDBTopology topology) throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setUser(topology.username);
        dataSource.setPassword(topology.password);
        dataSource.setDriverClass(topology.driverClassName);
        dataSource.setJdbcUrl(topology.url);
        dataSource.setMinPoolSize(topology.minPoolSize);
        dataSource.setMaxPoolSize(topology.maxPoolSize);
        dataSource.setInitialPoolSize(topology.initialPoolSize);
        dataSource.setAcquireIncrement(topology.acquireIncrement);
        dataSource.setTestConnectionOnCheckin(topology.testConnectionOnCheckin);
        dataSource.setTestConnectionOnCheckout(topology.testConnectionOnCheckout);
        dataSource.setPreferredTestQuery(topology.preferredTestQuery);
        dataSource.setAutoCommitOnClose(topology.autoCommitOnClose);
        dataSource.setIdleConnectionTestPeriod(topology.idleConnectionTestPeriod);
        dataSource.setMaxIdleTime(topology.maxIdleTime);
        dataSource.setMaxIdleTimeExcessConnections(topology.maxIdleTimeExcessConnections);
        dataSource.setCheckoutTimeout(topology.checkoutTimeout);
        dataSource.setNumHelperThreads(topology.numHelperThreads);
        dataSource.setUnreturnedConnectionTimeout(topology.unreturnedConnectionTimeout);
        dataSource.setDebugUnreturnedConnectionStackTraces(topology.debugUnreturnedConnectionStackTraces);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public MetaDataDao metaDataDao(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return new MySqlMetaDataDao(jdbcTemplate, objectMapper);
    }

    @Bean
    public ObjectMapper defaultObjectMapper() {
        return ObjectMapperFactory.makeObjectMapper();
    }

    @Bean
    public SpecService specsService(FullPetriClient fullPetriClient) {
        return new PetriSpecService(fullPetriClient);
    }


    @Bean
    public ExperimentMgmtService experimentMgmtService(EventPublisher experimentEventPublisher, FullPetriClient fullPetriClient, HardCodedScopesProvider hardCodedScopesProvider){
        return new ExperimentMgmtService(new JodaTimeClock(), experimentEventPublisher, fullPetriClient, hardCodedScopesProvider);
    }

    @Bean
    public MetaDataService metaDataService(MetaDataDao metaDataDao,
                                           FullPetriClient fullPetriClient,
                                           HardCodedScopesProvider hardCodedSpecsProvider,
                                           SpecExposureIdRetriever specExposureIdRetriever,
                                           SupportedLanguagesProvider languageResolver,
                                           GlobalGroupsManagementService globalGroupsManagementService) {
        return new MetaDataService(metaDataDao, fullPetriClient, hardCodedSpecsProvider, specExposureIdRetriever, languageResolver, globalGroupsManagementService);
    }


}
