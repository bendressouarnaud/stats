package com.cnmci.stats.service;

import com.cnmci.core.enums.CategorieEnrolement;
import com.cnmci.core.model.*;
import com.cnmci.stats.beans.MessageResponse;
import com.cnmci.stats.beans.PaymentWaveRequest;
import com.cnmci.stats.beans.WavePaymentOriginalRequest;
import com.cnmci.stats.beans.WavePaymentResponse;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            data.put("id", String.valueOf(artisan.getFirst().getId()));
            data.put("type", "ART");
            data.put("utilisateurId", String.valueOf(artisan.getFirst().getUtilisateur().getId()));
            return data;
        }

        if(artisan.isEmpty()){
            // Apprenti :
            List<Apprenti> apprenti = apprentiRepository.findByContact1(telephone.trim());
            if(apprenti.size() == 1){
                data.put("id", String.valueOf(apprenti.getFirst().getId()));
                data.put("type", "APP");
                data.put("utilisateurId", String.valueOf(apprenti.getFirst().getUtilisateur().getId()));
                return  data;
            }

            if(apprenti.isEmpty()){
                // Compagnon :
                List<Compagnon> compagnon = compagnonRepository.findByContact1(telephone.trim());
                if(compagnon.size() == 1){
                    data.put("id", String.valueOf(compagnon.getFirst().getId()));
                    data.put("type", "COM");
                    data.put("utilisateurId", String.valueOf(compagnon.getFirst().getUtilisateur().getId()));
                    return  data;
                }

                if(compagnon.isEmpty()){
                    // Entreprise :
                    List<Entreprise> entreprise = entrepriseRepository.findByContact(telephone.trim());
                    if(entreprise.size() == 1){
                        data.put("id", String.valueOf(entreprise.getFirst().getId()));
                        data.put("type", "ENT");
                        data.put("utilisateurId", String.valueOf(entreprise.getFirst().getUtilisateur().getId()));
                        return  data;
                    }
                }
            }
        }
        return null;
    }

    @Transactional
    public WavePaymentResponse generateWavePaymentLink(PaymentWaveRequest paymentWaveRequest,
                                        HttpServletRequest httpServletRequest){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + waveToken);
            headers.add("Content-Type", "application/json");

            // Get DATA :
            Map<String, String> dataIdType = getEntityData(paymentWaveRequest.telephone());
            if(dataIdType != null){
                // Call WEB Services :
                RestTemplate restTemplate = new RestTemplate();
                //String userMail = outilService.getBackUserConnectedName(httpServletRequest);
                //Utilisateur utilisateur = utilisateurRepository.findByEmail(userMail).get();

                String idToKeep = dataIdType.get("id") + "/" + dataIdType.get("type")
                        + "/" + String.valueOf(paymentWaveRequest.montant())+ "/0/" +
                        String.valueOf(dataIdType.get("utilisateurId"));
                log.info("Encodage : {}", idToKeep);
                String encodedString = Base64.getEncoder().encodeToString(idToKeep.getBytes());

                WavePaymentOriginalRequest objectRequest = new WavePaymentOriginalRequest();
                objectRequest.setAmount(paymentWaveRequest.montant());
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
                            .montant(paymentWaveRequest.montant())
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
            log.error("generateWavePaymentLink(...) : ", exc.toString());
            throw new OurGenericException(exc.toString());
        }
    }
}
