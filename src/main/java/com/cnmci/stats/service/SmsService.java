package com.cnmci.stats.service;

import com.cnmci.core.model.Apprenti;
import com.cnmci.core.model.Artisan;
import com.cnmci.core.model.Compagnon;
import com.cnmci.core.model.Utilisateur;
import com.cnmci.stats.beans.PeopleToSendSmsTo;
import com.cnmci.stats.beans.SmsRequest;
import com.cnmci.stats.beans.SmsResponseToken;
import com.cnmci.stats.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    // A T T R I B U T E S  :
    private final ArtisanRepository artisanRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ApprentiRepository apprentiRepository;
    private final CompagnonRepository compagnonRepository;
    private final ParametresRepository parametresRepository;
    private int totalSmsEnvoye = 0;
    private String accessToken = "";
    private Boolean sendSms = null;


    // M E T H O D S :
    private boolean checkSendingParameter(){
        if(sendSms == null){
            sendSms = parametresRepository.findById(1L).get().isEnvoiSms();
        }
        return sendSms;
    }


    //@Async
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void sendMessage(List<PeopleToSendSmsTo> listeUsers, long userActionTerrain){
        if(checkSendingParameter()) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<SmsResponseToken> response = null;
                HttpHeaders headers = new HttpHeaders();

                log.info("totalSmsEnvoye : {}", totalSmsEnvoye);

                for(PeopleToSendSmsTo data : listeUsers){
                    if (totalSmsEnvoye == 0) {
                        // Get TOKEN :
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                        MultiValueMap<String, String> listeParam = new LinkedMultiValueMap<>();
                        listeParam.add("client_id", "messaging-service-client");
                        listeParam.add("client_secret", "6LXmtscYKly7ZDhDJvz4Kkm5Qc5gJt08");
                        listeParam.add("username", "cnmci");
                        listeParam.add("password", "CnMotQpOTDWv");
                        listeParam.add("grant_type", "password");

                        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(listeParam, headers);

                        response = restTemplate.exchange("https://sso.sfpci.com/realms/messaging-service-realm/protocol/openid-connect/token",
                                HttpMethod.POST,
                                entity,
                                SmsResponseToken.class);
                        accessToken = response.getStatusCode().is2xxSuccessful() ? response.getBody().accessToken() : "";
                        log.info("accessToken : {}", accessToken);
                    }

                    if (!accessToken.isEmpty()) {
                        // Move on :
                        restTemplate = new RestTemplate();
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set("Authorization", "Bearer " + accessToken);

                        StringBuilder contenu = new StringBuilder();
                        contenu.append("Bonjour ");
                        contenu.append(data.nom());
                        contenu.append(". ");
                        contenu.append("La Chambre des METIERS vous rappelle à finaliser votre enrôlement : ");
                        contenu.append("https://cnmci.sfpci.com/link-pay");

                        HttpEntity<SmsRequest> entitySms = new HttpEntity<>(
                                new SmsRequest("SMS", data.contact(),
                                        contenu.toString()), headers);
                        try {
                            restTemplate.postForLocation("https://messaging.sfpci.com/api/v1/messages",
                                    entitySms
                            );
                        }
                        catch (Exception exc){
                            // Reset this to get new TOKEN :
                            totalSmsEnvoye = 0;
                            log.error("Exception lors de l'envoi : {}", exc.toString());
                        }
                        log.info("SMS transmis pour : {}", data.contact());

                        // Set FLAG :
                        switch (data.mode()){
                            case 0:
                                Artisan artisan = artisanRepository.findById(data.id()).get();
                                int updateRappel = artisan.getRappelSms() + 1;
                                artisan.setRappelSms(updateRappel);
                                artisan.setUtilisateurAgentAssermente(
                                        utilisateurRepository.findById(userActionTerrain).get());
                                artisan.setDateAssignationAssermente(OffsetDateTime.now());
                                artisanRepository.save(artisan);
                                break;

                            case 1:
                                Apprenti apprenti = apprentiRepository.findById(data.id()).get();
                                int updateRappelApprenti = apprenti.getRappelSms() + 1;
                                apprenti.setRappelSms(updateRappelApprenti);
                                apprentiRepository.save(apprenti);
                                break;

                            default:
                                Compagnon compagnon = compagnonRepository.findById(data.id()).get();
                                int updateRappelCompagnon = compagnon.getRappelSms() + 1;
                                compagnon.setRappelSms(updateRappelCompagnon);
                                compagnonRepository.save(compagnon);
                                break;
                        }

                        totalSmsEnvoye++;
                        if (totalSmsEnvoye > 20) {
                            // Reset :
                            totalSmsEnvoye = 0;
                        }
                    } else {
                        System.out.println("Impossible de transmettre le SMS");
                    }
                }
                // Reset THIS everytime a SET of SMS is sent :
                totalSmsEnvoye = 0;
            } catch (Exception e) {
                log.error("Exception rencontrée : {}", e.toString());
            }
        }
    }
}
