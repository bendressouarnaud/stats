package com.cnmci.stats.beans;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class QueryResponse {
    private String question;
    private String generatedSql;
    private List<Map<String, Object>> results;
    private String error;
    public static QueryResponse success(
            String question, String sql, List<Map<String, Object>> results) {
        QueryResponse r = new QueryResponse();
        r.setQuestion(question);
        r.setGeneratedSql(sql);
        r.setResults(results);
        return r;
    }
    public static QueryResponse error(String question, String sql, String error) {
        QueryResponse r = new QueryResponse();
        r.setQuestion(question);
        r.setGeneratedSql(sql);
        r.setError(error);
        return r;
    }
}
