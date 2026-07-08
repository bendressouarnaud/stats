package com.cnmci.stats.ia;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SqlExecutionService {

    // ATTRIBUTES
    private final JdbcTemplate jdbcTemplate;
    private final SchemaService schemaService;


    // METHODS :
    public List<Map<String, Object>> execute(String sql) {
        /*System.out.println("SQL Query : " + sql);
        if (!sql.trim().toUpperCase().startsWith("SELECT")) {
            throw new SecurityException(
                    "Only SELECT queries are allowed. Received: " + sql
            );
        }*/
        String fixedSql = fixTableNameCasing(sql);
        System.out.println("Executing SQL: {}" + fixedSql);
        return jdbcTemplate.queryForList(fixedSql);
    }
    private String fixTableNameCasing(String sql) {
        List<String> actualTables = schemaService.getTableNames();
        String fixed = sql;
        for (String table : actualTables) {
            fixed = fixed.replaceAll("(?i)\\b" + table + "\\b", table);
        }
        return fixed;
    }
}
