package com.cnmci.stats.ia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SchemaService {

    // A T T R I B U T E :
    @PersistenceContext
    EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;


    // M E T H O D :
    public List<String> getTableNames() {
        return jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
                String.class
        );
    }

    private String processCamelCaseCharacter(String colonneName, String table){
        if(table.equals("Artisan") && colonneName.equals("lieuNaissance")){
            return "commune_naissance";
        }
        else if(table.equals("Artisan") && colonneName.equals("pays")){
            return "nationalite";
        }
        else {
            String[] lesCharacteres = colonneName.split("");
            StringBuilder newOne = new StringBuilder();
            for (String character : lesCharacteres) {
                if (Pattern.matches("[A-Z]", character)) {
                    newOne.append("_");
                    newOne.append(character.toLowerCase());
                } else {
                    newOne.append(character);
                }
            }
            return newOne.toString();
        }
    }

    public String getSchemaDescription() {
        StringBuilder schema = new StringBuilder();
        for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
            Class<?> javaType = entity.getJavaType();
            schema.append("Entity: ").append(javaType.getSimpleName()).append("\nFields:\n");
            for (Attribute<?, ?> attr : entity.getAttributes()) {
                String kind = "";
                if (attr instanceof SingularAttribute<?, ?> sa && sa.isId()){
                    kind = " [ID]";
                }
                // FK and collection detection...
                if(attr.getPersistentAttributeType().name().equals("MANY_TO_ONE")){
                    String tampon = processCamelCaseCharacter(attr.getName(), javaType.getSimpleName());
                    if(tampon.equals("nationalite")){
                        schema.append("  - ").append(tampon).append(kind).append("\n");
                    }
                    else{
                        schema.append("  - ").append(tampon).append("_id").append(kind).append("\n");
                    }
                }
                else if(!attr.getPersistentAttributeType().name().equals("ONE_TO_MANY")){
                    schema.append("  - ").append(processCamelCaseCharacter(attr.getName(), javaType.getSimpleName())).append(kind).append("\n");
                }
                /*else{

                }*/
            }
            System.out.println(schema.toString());
        }
        return schema.toString();
    }


}
