package com.cnmci.stats.service;

import com.cnmci.stats.beans.PeopleToSendSmsTo;
import com.cnmci.stats.repository.ParametresRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    // ATTRIBUTES
    @Value("${spring.mail.username}")
    private String emailSenderAddress;
    private final JavaMailSender emailSender;
    private final ParametresRepository parametresRepository;
    private Boolean sendMail = null;


    // METHODS :
    private boolean checkSendingParameter(){
        if(sendMail == null){
            sendMail = parametresRepository.findById(1L).get().isEnvoiMail();
        }
        return sendMail;
    }

    //@Async
    public void entitiesInLateToAgentAssermente(List<PeopleToSendSmsTo> listeUsers, String[] mails){
        if(checkSendingParameter()) {
            try {
                MimeMessage mimeMessage = emailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,
                        "utf-8");
                StringBuilder contenu = new StringBuilder();
                contenu.append("<h2> Liste des personnes </h2>");
                contenu.append("<table><tr><th>Nom</th><th><Contact></th><th><Commune activité></th>");
                contenu.append("<th><Quartier d'activité></th><th><Montant déjà payé></th><th><Profil></th></tr>");
                for(PeopleToSendSmsTo po : listeUsers){
                    contenu.append("<tr><td>");
                    contenu.append(po.nom());
                    contenu.append("</td><td>");
                    contenu.append(po.contact());
                    contenu.append("</td><td>");
                    contenu.append(po.commune());
                    contenu.append("</td><td>");
                    contenu.append(po.quartier());
                    contenu.append("</td><td>");
                    contenu.append(po.solde());
                    contenu.append("</td><td>");
                    contenu.append(po.profil());
                    contenu.append("</td></tr>");
                }
                contenu.append("</table>");

                // Envoi du MAIL :
                helper.setText(String.valueOf(contenu), true);
                helper.setTo(mails);
                helper.setSubject("Personnes en retard de régularisation");
                helper.setFrom(emailSenderAddress);
                emailSender.send(mimeMessage);
            } catch (Exception exc) {
                System.out.println("mailCreation(...) : " + exc.toString());
                //log.error("mailCreation(...) : {}", exc.toString());
            }
        }
    }

}
