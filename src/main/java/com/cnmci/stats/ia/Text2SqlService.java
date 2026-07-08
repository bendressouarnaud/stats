package com.cnmci.stats.ia;

import com.cnmci.stats.beans.QueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Text2SqlService {

    //
    private final SchemaService schemaService;
    private final OllamaService ollamaService;
    private final SqlExecutionService sqlExecutionService;
    private String leSchema = "";


    // Methods
    public QueryResponse processQuestion(String question) {
        String generatedSql = null;
        try {
            if(leSchema.isEmpty()){
                leSchema = schemaService.getSchemaDescription();
            }
            generatedSql = ollamaService.generateSql(question, leSchema);
            System.out.println("Generated SQL : " + generatedSql);
            List<Map<String, Object>> results = new ArrayList<>();
            if(generatedSql.contains("sql")){
                System.out.println("Contains 'sql'");
                String[] tampTableau = generatedSql.split("sql");
                String newQuery = tampTableau[1];
                results = sqlExecutionService.execute(newQuery);
            }
            else{
                System.out.println("Not Containing 'sql'");
                results = sqlExecutionService.execute(generatedSql.replace("```sql", ""));
            }
            return QueryResponse.success(question, generatedSql, results);
        } catch (SecurityException e) {
            return QueryResponse.error(question, generatedSql,
                    "Blocked: only SELECT queries are allowed.");
        } catch (Exception e) {
            System.out.println("Error processing question: " + e.toString());
            return QueryResponse.error(question, generatedSql, e.getMessage());
        }
    }
}
