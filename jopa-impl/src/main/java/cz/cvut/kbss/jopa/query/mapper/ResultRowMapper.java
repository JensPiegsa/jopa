package cz.cvut.kbss.jopa.query.mapper;

import cz.cvut.kbss.ontodriver.ResultSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single {@link cz.cvut.kbss.jopa.model.annotations.SparqlResultSetMapping} instance.
 * <p>
 * This instance can contain multiple {@link SparqlResultMapper} instances, representing the individual mappings
 * specified by the {@link cz.cvut.kbss.jopa.model.annotations.SparqlResultSetMapping}.
 */
public class ResultRowMapper implements SparqlResultMapper {

    private final String name;

    private final List<SparqlResultMapper> rowMappers = new ArrayList<>();

    public ResultRowMapper(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the result set mapping represented by this mapper.
     *
     * @return Mapping name
     */
    public String getName() {
        return name;
    }

    List<SparqlResultMapper> getRowMappers() {
        return Collections.unmodifiableList(rowMappers);
    }

    void addMapper(SparqlResultMapper mapper) {
        rowMappers.add(mapper);
    }

    @Override
    public Object map(ResultSet resultSet) {
        if (rowMappers.size() == 1) {
            return rowMappers.get(0).map(resultSet);
        }
        final Object[] result = new Object[rowMappers.size()];
        int i = 0;
        for (SparqlResultMapper m : rowMappers) {
            result[i++] = m.map(resultSet);
        }
        return result;
    }
}
