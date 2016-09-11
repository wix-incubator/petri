package com.wixpress.guineapig.util;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class MetaDataTable implements GuineaPigTable {
    private JdbcTemplate jdbcTemplate;

    public MetaDataTable(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void dropTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS meta_data");
    }

    @Override
    public void createTable() {
        jdbcTemplate.execute("CREATE TABLE meta_data(data_type VARCHAR(32), data_value varchar(16000), PRIMARY KEY(data_type))");
    }
}
