package com.cnmci.stats.ia;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OllamaService {


    //ATTRIBUTES
    private final OllamaChatModel ollamaChatModel;

    // METHODS :
    public String generateSql(String question, String schemaDescription) {
        SystemMessage systemMessage = SystemMessage.from("""
                You are a MySQL SQL expert. Your only job is to convert natural language questions into valid MySQL SQL queries.
                
                Rules:
                - Return ONLY the SQL query, nothing else.
                - Do NOT include markdown code blocks, backticks, or any explanation.
                - Use EXACT table and column names as they appear in the schema - do not change case.
                - Always use proper MySQL syntax.
                - If the question cannot be answered with the given schema, return exactly:
                  SELECT 'Unable to answer with given schema';
                
                ---
                
                Here are some examples to guide your output:
                
                Example 1:
                Question:
                Retourne la liste des artisans dont la commune d'activité est YOPOUGON et dont le métier est BLANCHISSERIE
                
                SQL Query:
                Select a.id, a.nom,a.prenom, contact1 from artisan a inner join activite b on a.activite_id = b.id inner
                join commune c on c.id = b.commune_id where c.libelle = 'Cocody' and a.metier_id = (select id from metier where
                libelle = 'Couturier');
                
                ---
                
                Example 2:
                Question:
                Retourne la liste des artisans dont la commune d'activité est Cocody
                
                SQL Query:
                Select a.id, a.nom,a.prenom, contact1 from artisan a inner join activite b on a.activite_id = b.id inner join 
                commune c on c.id = b.commune_id where c.libelle = 'Cocody';
                
                ---
                
                Example 3:
                Question:
                Ramène moi les apprentis de l'artisan dont le contact est 0707853142
                
                SQL Query:
                select c.id, concat(c.nom,' ',c.prenom) as nom, c.contact1 from artisan a inner join artisan_apprenti b on a.id = 
                b.artisan_id inner join apprenti c on c.id = b.apprenti_id where a.contact1 = '0707853142';
                
                ---
                
                Example 4:
                Question:
                Combien a payé l'artisan dont le contact est x 
                
                SQL Query:
                select a.id,a.nom,a.prenom,a.contact1,sum(b.montant) from artisan a inner join paiement_enrolement b on a.id = b.artisan_id 
                where contact1 = 'x' group by a.id,a.nom,a.prenom,a.contact1;
                
                ---
                
                Example 5:
                Question:
                Les artisans qui ont déjà soldé
                
                SQL Query:
                select a.id,a.nom,a.prenom,a.contact1,case when a.statut_type = 0 then 'Enrôlement' else 'Renouvellement' end as type_enrolement 
                from artisan a where a.statut_paiement = 2;
                """);
        UserMessage userMessage = UserMessage.from("""
                Database Schema:
                %s
                
                Question: %s
                
                SQL Query:
                """.formatted(schemaDescription, question));
        Response<AiMessage> response = ollamaChatModel.generate(systemMessage, userMessage);
        String rawSql = response.content().text();
        return extractSql(rawSql);
    }
    private String extractSql(String raw) {
        String cleaned = raw.trim();
        // Strip markdown code fences if the model returns them anyway
        Pattern codeBlock = Pattern.compile(
                "⁠ (?:sql)?\\s*(.*?)\\s* ⁠",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = codeBlock.matcher(cleaned);
        if (matcher.find()) {
            cleaned = matcher.group(1).trim();
        }
        // Take only the first statement
        if (cleaned.contains(";")) {
            cleaned = cleaned.substring(0, cleaned.indexOf(";") + 1).trim();
        }
        return cleaned;
    }
}
