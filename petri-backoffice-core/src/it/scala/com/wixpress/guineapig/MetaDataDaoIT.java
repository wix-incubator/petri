package com.wixpress.guineapig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.guineapig.dao.MetaDataDao;
import com.wixpress.guineapig.dao.MySqlMetaDataDao;
import com.wixpress.guineapig.dto.UserAgentRegex;
import com.wixpress.guineapig.util.DaoSupport;
import com.wixpress.guineapig.util.DaoTestSuiteIT;
import com.wixpress.guineapig.util.GuineaPigDBDriver;
import com.wixpress.guineapig.util.MetaDataTable;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MetaDataDaoIT extends DaoSupport {

    MetaDataDao metaDataDao;

    @Before
    public void init() throws SQLException {
        DataSource dataSource = getDataSource();
        JdbcTemplate template = new JdbcTemplate(dataSource);

        metaDataDao = new MySqlMetaDataDao(template, anObjectMapperWithScalaSupport());

        MetaDataTable metaDataTable = new MetaDataTable(dataSource);
        GuineaPigDBDriver dbDriver = GuineaPigDBDriver.dbDriver(metaDataTable);
        dbDriver.reloadSchema();
    }

    private ObjectMapper anObjectMapperWithScalaSupport() {
        return ObjectMapperFactory.makeObjectMapper();
    }
    
    @Test
    public void addFirstUserAgentRegex() throws IOException, ClassNotFoundException {
        UserAgentRegex userAgentRegex = new UserAgentRegex("(*)test(*)", "This is a Test Regex");
        metaDataDao.add(userAgentRegex);

        assertThat(metaDataDao.get(UserAgentRegex.class), is(asList(userAgentRegex)));
    }

    @Test
    public void addAUserAgentRegexToExistingOne() throws IOException, ClassNotFoundException {
        UserAgentRegex userAgentRegex1 = new UserAgentRegex("(*)test(*)", "This is a Test Regex");
        metaDataDao.add(userAgentRegex1);
        UserAgentRegex userAgentRegex2 = new UserAgentRegex("(*)test2(*)", "This is a second Test Regex");
        metaDataDao.add(userAgentRegex2);


        List<UserAgentRegex> retrievedData = metaDataDao.get(UserAgentRegex.class);
        assertThat(retrievedData, is(asList(userAgentRegex1, userAgentRegex2)));

    }

    @Test
    public void noChangeWhenAddingUserAgentRegexTwice() throws IOException, ClassNotFoundException {
        UserAgentRegex userAgentRegex = new UserAgentRegex("(*)test(*)", "This is a Test Regex");
        metaDataDao.add(userAgentRegex);
        metaDataDao.add(userAgentRegex);

        assertThat(metaDataDao.get(UserAgentRegex.class), is(asList(userAgentRegex)));

    }

    @Test
    public void deleteUserAgentRegex() throws IOException, ClassNotFoundException {
        UserAgentRegex userAgentRegex1 = new UserAgentRegex("(*)test(*)", "This is a Test Regex");
        metaDataDao.add(userAgentRegex1);
        UserAgentRegex userAgentRegex2 = new UserAgentRegex("(*)test2(*)", "This is a second Test Regex");
        metaDataDao.add(userAgentRegex2);
        metaDataDao.delete(UserAgentRegex.class, userAgentRegex1.regex());

        assertThat(metaDataDao.get(UserAgentRegex.class), is(asList(userAgentRegex2)));
    }


    @Test(expected = MySqlMetaDataDao.MetaDataNotFound.class)
    public void throwExceptionWhenDeletingNonExistingUserAgentRegex() throws IOException, ClassNotFoundException {
        UserAgentRegex userAgentRegex1 = new UserAgentRegex("(*)test(*)", "This is a Test Regex");
        metaDataDao.delete(UserAgentRegex.class, userAgentRegex1.regex());
    }


    @Test
    public void getEmptyListOfUserAgentRegex() throws IOException, ClassNotFoundException {
        assertThat(metaDataDao.get(UserAgentRegex.class).size(), is(0));
    }
}
