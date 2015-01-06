package com.wixpress.petri.petri;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.util.List;

import static com.wixpress.petri.petri.FullPetriClient.CreateFailed;
import static com.wixpress.petri.petri.FullPetriClient.CreateFailedData;

/**
 * @author: talyag
 * @since: 10/30/13
 */
public abstract class JdbcPetriDao<T, V> implements PetriDao<T, V> {
    protected final JdbcTemplate jdbcTemplate;
    protected final PetriMapper mapper;
    private final String fetchSql;
    protected final String selectSql;

    public JdbcPetriDao(JdbcTemplate jdbcTemplate, PetriMapper mapper, String fetchSql, String selectSql) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
        this.fetchSql = fetchSql;
        this.selectSql = selectSql;
    }

    @Override
    public List<T> fetch() {
        return jdbcTemplate.query(fetchSql, mapper);
    }

    @Override
    public T add(V obj) {
        String serializedObj;
        try {
            serializedObj = mapper.serialize(obj);
        } catch (Throwable e) {
            throw new FullPetriClient.PetriException(e);
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                createInsertStatement(serializedObj, obj),
                keyHolder);

        Number id = keyHolder.getKey();
        if (id != null) {
            postUpdate(id.intValue());
            return (T) jdbcTemplate.query(String.format(selectSql, id.intValue()), mapper).get(0);
        }
        throw new CreateFailed(new CreateFailedData(obj.getClass().getSimpleName(), identifierOf(obj)));
    }

    protected void postUpdate(int id) {
        //To change body of created methods use File | Settings | File Templates.
    }

    protected abstract String identifierOf(V obj);

    protected abstract PreparedStatementCreator createInsertStatement(String serializedObj, V obj);

}
