package com.cnmci.stats.service;

import com.cnmci.core.model.Artisan;
import com.cnmci.stats.beans.SmsRequest;
import com.cnmci.stats.beans.SmsResponseToken;
import com.cnmci.stats.repository.ArtisanRepository;
import com.cnmci.stats.repository.ParametresRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    // A T T R I B U T E S  :
    private final ArtisanRepository artisanRepository;
    private final ParametresRepository parametresRepository;
    private int totalSmsEnvoye = 0;
    private String accessToken = "0";
    private Boolean sendSms = null;


    // M E T H O D S :
    private boolean checkSendingParameter(){
        if(sendSms == null){
            sendSms = parametresRepository.findById(1L).get().isEnvoiSms();
        }
        return sendSms;
    }


    //@Async
    @Transactional
    public void sendMessage(String client, String contact, String dateEnrolement, long idArtisan){
        if(checkSendingParameter()) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<SmsResponseToken> response = null;
                HttpHeaders headers = new HttpHeaders();

                if (totalSmsEnvoye == 0) {
                    // Get TOKEN :
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                    //Map<String, String> listeParam = new HashMap<>();
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
                }

                if (!accessToken.isEmpty()) {
                    // Move on :
                    restTemplate = new RestTemplate();
                    headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Authorization", "Bearer " + accessToken);

                    StringBuilder contenu = new StringBuilder();
                    contenu.append("Bonjour ");
                    contenu.append(client);
                    contenu.append(". ");
                    contenu.append("La Chambre des METIERS vous rappelle à solder les frais de votre enrôlement effectué le ");
                    contenu.append(dateEnrolement);

                    // Bonjour DIARASSOUBALO. La Chambre des métiers de CIV vous rappelle à solder les frais de votre enrôlement effectué le 2026-03-67
                    HttpEntity<SmsRequest> entitySms = new HttpEntity<>(
                            new SmsRequest("SMS", contact,
                                    contenu.toString()), headers);
                    restTemplate.postForLocation("https://messaging.sfpci.com/api/v1/messages",
                            entitySms
                    );
                    log.info("SMS transmis");

                    // Set FLAG :
                    Artisan artisan = artisanRepository.findById(idArtisan).get();
                    int updateRappel = artisan.getRappelSms() + 1;
                    artisan.setRappelSms(updateRappel);
                    artisanRepository.save(artisan);

                    totalSmsEnvoye++;
                    if (totalSmsEnvoye > 20) {
                        // Reset :
                        totalSmsEnvoye = 0;
                    }
                } else {
                    System.out.println("Impossible de transmettre le SMS");
                }
            } catch (Exception e) {
                log.error("Exception rencontrée : {}", e.toString());
            }
        }
    }
}
