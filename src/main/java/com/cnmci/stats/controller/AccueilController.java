package com.cnmci.stats.controller;

import com.cnmci.core.model.Parametres;
import com.cnmci.stats.beans.*;
import com.cnmci.stats.repository.ParametresRepository;
import com.cnmci.stats.service.ActionService;
import com.cnmci.stats.service.PaiementService;
import com.cnmci.stats.service.StatistiqueService;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    private final ActionService actionService;
    private final PaiementService paiementService;


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

    @Operation(summary = "Récupérer les entités dont le nom ou le contact correspond au paramètre")
    @GetMapping(value="/look-for-any-entity/{search}")
    private List<EntitySearchResponse> lookForAnyEntity(@PathVariable String search) {
        return statistiqueService.lookForAnyEntity(search);
    }

    @Operation(summary = "Récupérer les entités dont les critères de recherche ont été passés en paramètre")
    @PostMapping(value="/agent-asserment-request")
    private List<EntitySearchResponse> processAgentAssermenteRequest(@RequestBody ControleAgentSermenteRequest request) {
        return statistiqueService.processAgentAssermenteRequest(request);
    }

    @Operation(summary = "Pour gérer une Amende (création - Modification)")
    @PostMapping("/manage-amende")
    @Parameter(name = "id", description = "Id de l'amende")
    @Parameter(name = "montant", description = "montant de l'amende")
    @Parameter(name = "commentaire", description = "commentaire l'agent enrôleur")
    @Parameter(name = "entity_id", description = "Id de l'entité (Artisan, Apprenti, etc.)")
    @Parameter(name = "entity_type",
            description = "Type de l'entité (ART : Artisan, APP : Apprenti, COM : Compagnon, ENT : Entreprise")
    public MessageResponse manageAmende(@RequestBody AmendeRequest data,
                                        HttpServletRequest httpServletRequest){
        return actionService.manageAmende(data, httpServletRequest);
    }

    @Operation(summary = "Récupérer les 10 métiers d'artisans les plus représentés dans UNE commune")
    @GetMapping(value="/get-ten-biggest-activities-by-commune/{id}")
    private List<StatsData> getRepartitionMetierByCommune(@PathVariable long id) {
        return statistiqueService.getRepartitionMetierByCommune(id);
    }

    @Operation(summary = "Récupérer les 10 communes d'origine de naissance d'artisans les plus en vue dans UNE COMMUNE")
    @GetMapping(value="/get-ten-biggest-birthplace-by-commune/{id}")
    private List<StatsData> getArtisanFromBirthPlaceByCommune(@PathVariable long id) {
        return statistiqueService.getArtisanFromBirthPlaceByCommune(id);
    }

    @Operation(summary = "Récupérer les 10 métiers desquels proviennent les ARTISANS qui PAIENT le PLUS")
    @GetMapping(value="/get-ten-biggest-payers-by-commune/{id}")
    private List<StatsData> getPaymentByActivite(@PathVariable long id) {
        return statistiqueService.getPaymentByActivite(id);
    }

    @Operation(summary = "Récupérer les statistiques d'enrôlement des ARTISANS et ENTREPRISES" +
            " en fonction de la commune de d'activité")
    @GetMapping(value="/get-entities-from-quartier-ville/{id}")
    private List<EntityFromQuartier> getEntitiesByQuartier(@PathVariable long id) {
        return statistiqueService.getEntitiesByQuartier(id);
    }

    @Operation(summary = "Pour générer le lien de paiement souhaité par tout utilisateur")
    @PostMapping("/generate-user-payment-link")
    @Parameter(name = "montant", description = "Montant de la transaction")
    @Parameter(name = "telephone", description = "Contact de l'entité")
    public WavePaymentResponse generateWavePaymentLink(@RequestBody PaymentWaveRequest data,
                                        HttpServletRequest httpServletRequest){
        return paiementService.generateWavePaymentLink(data, httpServletRequest);
    }

}
