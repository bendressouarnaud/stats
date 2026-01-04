package com.cnmci.stats.controller;

import com.cnmci.core.model.Parametres;
import com.cnmci.stats.beans.StatsBean;
import com.cnmci.stats.repository.ParametresRepository;
import com.cnmci.stats.service.StatistiqueService;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Tag(name="StatGen")
public class AccueilController {

    // Attributes :
    public static Key SECRET_KEY;
    private final ParametresRepository parametresRepository;
    private final StatistiqueService statistiqueService;


    // M E T H O D S :
    @PostConstruct
    private void initialize(){
        /*try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource(firebaseConfig).
                                    getInputStream())).build();
            if (FirebaseApp.getApps().isEmpty()) {
                this.firebaseApp = FirebaseApp.initializeApp(options);
                //System.out.println("initializeApp");
            } else {
                this.firebaseApp = FirebaseApp.getInstance();
                //System.out.println("getInstance");
            }
        } catch (IOException e) {
        }*/

        if(parametresRepository.findById(1L).isEmpty()){
            // Create parameters :
            Parametres parametres = new Parametres();
            parametres.setId(1L);
            parametres.setEnvoiMail(false);
            parametres.setByteArray(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded());
            // Persist :
            parametresRepository.save(parametres);
        }
        //
        SECRET_KEY = Keys.hmacShaKeyFor(Objects.requireNonNull(parametresRepository.findById(1L)
                .map(
                        Parametres::getByteArray
                ).orElse(null))
        );
    }


    @Operation(summary = "Récupérer les STATISTIQUES globales des entités")
    @GetMapping(value="/get-entities-stats")
    private List<StatsBean> processApprentiMobile() {
        return statistiqueService.getEntitiesStatistiques();
    }

}
