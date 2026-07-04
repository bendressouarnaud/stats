package com.cnmci.stats.service;

import com.cnmci.core.enums.CategorieEnrolement;
import com.cnmci.core.enums.StatutType;
import com.cnmci.core.model.*;
import com.cnmci.stats.beans.*;
import com.cnmci.stats.exception.OurGenericException;
import com.cnmci.stats.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementService {

    // A T T R I B U T E S :
    private final OutilService outilService;
    private final UtilisateurRepository utilisateurRepository;
    private final ArtisanRepository artisanRepository;
    private final ApprentiRepository apprentiRepository;
    private final CompagnonRepository compagnonRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final PaymentRequestCopieRepository paymentRequestCopieRepository;
    private final PaiementEnrolementRepository paiementEnrolementRepository;
    @Value("${client.wave.token}")
    private String waveToken;
    @Value("${backend.web.url}")
    private String backendWebUrl;
    @Value("${client.wave.apiurl}")
    private String waveUrl;



    // M E T H O D S :

    private CategorieEnrolement getCategorie(String requester){
        return switch (requester){
            case "ENT" -> CategorieEnrolement.ENTREPRISE;
            case "ART" -> CategorieEnrolement.ARTISAN;
            case "APP" -> CategorieEnrolement.APPRENTI;
            default -> CategorieEnrolement.COMPAGNON;
        };
    }

    private Map<String, String> getEntityData(String telephone){
        Map<String, String> data = new HashMap<>();
        // Artisan :
        List<Artisan> artisan = artisanRepository.findByContact1(telephone.trim());
        if(artisan.size() == 1){
            Artisan artData = artisan.getFirst();
            data.put("id", String.valueOf(artData.getId()));
            data.put("type", "ART");
            data.put("utilisateurId", String.valueOf(artData.getUtilisateur().getId()));
            List<PaiementEnrolement> listeP = paiementEnrolementRepository.findAllByArtisan(artData);
            int sommeAPayer = artData.getStatutType() == StatutType.ENROLE ? 15000 : 5000;
            data.put("sommeAPayer", String.valueOf(listeP.isEmpty() ? sommeAPayer :
                            (sommeAPayer - listeP.stream().mapToInt(
                                    PaiementEnrolement::getMontant).sum())
                    )
            );
            return data;
        }

        if(artisan.isEmpty()){
            // Apprenti :
            List<Apprenti> apprenti = apprentiRepository.findByContact1(telephone.trim());
            if(apprenti.size() == 1){
                Apprenti appData = apprenti.getFirst();
                data.put("id", String.valueOf(appData.getId()));
                data.put("type", "APP");
                data.put("utilisateurId", String.valueOf(appData.getUtilisateur().getId()));
                List<PaiementEnrolement> listeP = paiementEnrolementRepository.findAllByApprenti(appData);
                data.put("sommeAPayer", String.valueOf(listeP.isEmpty() ? 5000 :
                        (5000 - listeP.stream().mapToInt(
                                PaiementEnrolement::getMontant).sum()))
                );
                return data;
            }

            if(apprenti.isEmpty()){
                // Compagnon :
                List<Compagnon> compagnon = compagnonRepository.findByContact1(telephone.trim());
                if(compagnon.size() == 1){
                    Compagnon comData = compagnon.getFirst();
                    data.put("id", String.valueOf(comData.getId()));
                    data.put("type", "COM");
                    data.put("utilisateurId", String.valueOf(comData.getUtilisateur().getId()));
                    List<PaiementEnrolement> listeP = paiementEnrolementRepository.findAllByCompagnon(comData);
                    data.put("sommeAPayer", String.valueOf(listeP.isEmpty() ? 5000 :
                            (5000 - listeP.stream().mapToInt(
                                    PaiementEnrolement::getMontant).sum()))
                    );
                    return  data;
                }

                if(compagnon.isEmpty()){
                    // Entreprise :
                    List<Entreprise> entreprise = entrepriseRepository.findByContact(telephone.trim());
                    if(entreprise.size() == 1){
                        Entreprise entData = entreprise.getFirst();
                        data.put("id", String.valueOf(entData.getId()));
                        data.put("type", "ENT");
                        data.put("utilisateurId", String.valueOf(entData.getUtilisateur().getId()));
                        List<PaiementEnrolement> listeP = paiementEnrolementRepository.findAllByEntreprise(entData);
                        data.put("sommeAPayer", String.valueOf(listeP.isEmpty() ? 25000 :
                                (25000 - listeP.stream().mapToInt(
                                        PaiementEnrolement::getMontant).sum()))
                        );
                        return  data;
                    }
                }
            }
        }
        return null;
    }

    private boolean checkIfAlreadyPaid(String requester, long id){
        return switch (requester){
            case "ENT" -> {
                Optional<Entreprise> optEnt =
                        entrepriseRepository.findByIdAndStatutPaiement(id, 2);
                yield optEnt.isPresent();
            }
            case "ART" -> {
                Optional<Artisan> optEnt =
                        artisanRepository.findByIdAndStatutPaiement(id, 2);
                yield optEnt.isPresent();
            }
            case "APP" -> {
                Optional<Apprenti> optEnt =
                        apprentiRepository.findByIdAndStatutPaiement(id, 2);
                yield optEnt.isPresent();
            }
            default -> {
                Optional<Compagnon> optEnt =
                        compagnonRepository.findByIdAndStatutPaiement(id, 2);
                yield optEnt.isPresent();
            }
        };
    }

    @Transactional
    public WavePaymentResponse generateWavePaymentLink(PaymentWaveContactRequest paymentWaveRequest,
                                                       HttpServletRequest httpServletRequest){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + waveToken);
            headers.add("Content-Type", "application/json");

            // Get DATA :
            Map<String, String> dataIdType = getEntityData(paymentWaveRequest.telephone());
            if(dataIdType != null){
                if(checkIfAlreadyPaid(dataIdType.get("type"), Long.parseLong(dataIdType.get("id")))){
                    log.info("Le client {} avec Id {} a déjà soldé !", dataIdType.get("type"),
                            dataIdType.get("id"));
                    return null;
                }
                // Call WEB Services :
                RestTemplate restTemplate = new RestTemplate();
                //String userMail = outilService.getBackUserConnectedName(httpServletRequest);
                //Utilisateur utilisateur = utilisateurRepository.findByEmail(userMail).get();

                String idToKeep = dataIdType.get("id") + "/" + dataIdType.get("type")
                        + "/" + dataIdType.get("sommeAPayer") + "/0/" +
                        String.valueOf(dataIdType.get("utilisateurId"));
                log.info("Encodage : {}", idToKeep);
                String encodedString = Base64.getEncoder().encodeToString(idToKeep.getBytes());

                WavePaymentOriginalRequest objectRequest = new WavePaymentOriginalRequest();
                objectRequest.setAmount(Integer.parseInt(dataIdType.get("sommeAPayer")));
                objectRequest.setCurrency("XOF");
                objectRequest.setErrorUrl(
                        backendWebUrl + "invalidation/" + encodedString);
                objectRequest.setSuccessUrl(
                        backendWebUrl + "validation/" + encodedString);

                HttpEntity<WavePaymentOriginalRequest> entity = new HttpEntity<>(objectRequest, headers);
                ResponseEntity<WavePaymentResponse> responseEntity = restTemplate.postForEntity(waveUrl,
                        entity, WavePaymentResponse.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    // Delete previous request not completed :
                    PaymentRequest paymentRequest = paymentRequestRepository.
                            findByRequesterTypeAndRequesterIdAndEtat(
                                    dataIdType.get("type"), Long.parseLong(dataIdType.get("id")), 0);
                    if(paymentRequest != null){
                        paymentRequestRepository.delete(paymentRequest);
                    }

                    // Persist :
                    WavePaymentResponse wavePaymentResponse = responseEntity.getBody();
                    // Track this :
                    PaymentRequest prt = PaymentRequest.builder()
                            .requesterId(Long.parseLong(dataIdType.get("id")))
                            .requesterType(dataIdType.get("type"))
                            .montant(Integer.parseInt(dataIdType.get("sommeAPayer")))
                            .etat(0)
                            .waveId(wavePaymentResponse.getId())
                            .launchUrl(wavePaymentResponse.getWaveLaunchUrl())
                            .categorieEnrolement(getCategorie(dataIdType.get("type")))
                            .paymentType(0)
                            .build();
                    paymentRequestRepository.save(prt);
                    // track this too :
                    PaymentRequestCopie paymentRequestCopie = PaymentRequestCopie.builder()
                            .requesterId(Long.parseLong(dataIdType.get("id")))
                            .requesterType(dataIdType.get("type"))
                            .montant(Integer.parseInt(dataIdType.get("sommeAPayer")))
                            .etat(0)
                            .waveId(wavePaymentResponse.getId())
                            .launchUrl(wavePaymentResponse.getWaveLaunchUrl())
                            .categorieEnrolement(getCategorie(dataIdType.get("type")))
                            .paymentType(0)
                            .utilisateur(null)
                            .build();
                    paymentRequestCopieRepository.save(paymentRequestCopie);
                    return wavePaymentResponse;
                }
                else {
                    throw new OurGenericException("Impossible de poursuivre, une erreur est survenue !");
                }
            }
            else {
                // Raise EXCEPTION :
                throw new OurGenericException("Le numéro de téléphone renseigné est soit inexistant " +
                        "soit est un doublon !");
            }
        } catch (Exception exc) {
            log.error("generateWavePaymentLink(...) : ", exc.toString());
            throw new OurGenericException(exc.toString());
        }
    }

    @Transactional
    public WavePaymentResponse generateWavePaymentLinkFromCallCenter(PaymentWaveContactRequest paymentWaveRequest,
                                                       HttpServletRequest httpServletRequest){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + waveToken);
            headers.add("Content-Type", "application/json");

            // Get DATA :
            Map<String, String> dataIdType = getEntityData(paymentWaveRequest.telephone());
            if(dataIdType != null){
                if(checkIfAlreadyPaid(dataIdType.get("type"), Long.parseLong(dataIdType.get("id")))){
                    log.info("Le client {} avec Id {} a déjà soldé !", dataIdType.get("type"),
                            dataIdType.get("id"));
                    return null;
                }
                // Call WEB Services :
                RestTemplate restTemplate = new RestTemplate();
                String userMail = outilService.getBackUserConnectedName(httpServletRequest);
                Utilisateur utilisateur = utilisateurRepository.findByEmail(userMail).get();

                String idToKeep = dataIdType.get("id") + "/" + dataIdType.get("type")
                        + "/" + dataIdType.get("sommeAPayer") + "/0/" +
                        String.valueOf(utilisateur.getId());
                log.info("Encodage CALL CENTER : {}", idToKeep);
                String encodedString = Base64.getEncoder().encodeToString(idToKeep.getBytes());

                WavePaymentOriginalRequest objectRequest = new WavePaymentOriginalRequest();
                objectRequest.setAmount(Integer.parseInt(dataIdType.get("sommeAPayer")));
                objectRequest.setCurrency("XOF");
                objectRequest.setErrorUrl(
                        backendWebUrl + "invalidation/" + encodedString);
                objectRequest.setSuccessUrl(
                        backendWebUrl + "validation/" + encodedString);

                HttpEntity<WavePaymentOriginalRequest> entity = new HttpEntity<>(objectRequest, headers);
                ResponseEntity<WavePaymentResponse> responseEntity = restTemplate.postForEntity(waveUrl,
                        entity, WavePaymentResponse.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    // Delete previous request not completed :
                    PaymentRequest paymentRequest = paymentRequestRepository.
                            findByRequesterTypeAndRequesterIdAndEtat(
                                    dataIdType.get("type"), Long.parseLong(dataIdType.get("id")), 0);
                    if(paymentRequest != null){
                        paymentRequestRepository.delete(paymentRequest);
                    }

                    // Persist :
                    WavePaymentResponse wavePaymentResponse = responseEntity.getBody();
                    // Track this :
                    PaymentRequest prt = PaymentRequest.builder()
                            .requesterId(Long.parseLong(dataIdType.get("id")))
                            .requesterType(dataIdType.get("type"))
                            .montant(Integer.parseInt(dataIdType.get("sommeAPayer")))
                            .etat(0)
                            .waveId(wavePaymentResponse.getId())
                            .launchUrl(wavePaymentResponse.getWaveLaunchUrl())
                            .categorieEnrolement(getCategorie(dataIdType.get("type")))
                            .build();
                    paymentRequestRepository.save(prt);
                    return wavePaymentResponse;
                }
                else {
                    throw new OurGenericException("Impossible de poursuivre, une erreur est survenue !");
                }
            }
            else {
                // Raise EXCEPTION :
                throw new OurGenericException("Le numéro de téléphone renseigné est soit inexistant " +
                        "soit est un doublon !");
            }
        } catch (Exception exc) {
            log.error("generateWavePaymentLinkFromCallCenter(...) : ", exc.toString());
            throw new OurGenericException(exc.toString());
        }
    }
}
