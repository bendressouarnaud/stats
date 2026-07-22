package com.cnmci.stats.service;

import com.cnmci.core.model.NotificationControle;
import com.cnmci.core.model.Utilisateur;
import com.cnmci.stats.LibelleTotal;
import com.cnmci.stats.beans.AssermenteAction;
import com.cnmci.stats.beans.PeopleToSendSmsTo;
import com.cnmci.stats.repository.NotificationControleRepository;
import com.cnmci.stats.repository.ParametresRepository;
import com.cnmci.stats.repository.UtilisateurRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
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
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationControleRepository notificationControleRepository;
    private Boolean sendMail = null;


    // METHODS :
    private boolean checkSendingParameter(){
        if(sendMail == null){
            sendMail = parametresRepository.findById(1L).get().isEnvoiMail();
        }
        return sendMail;
    }

    //@Async
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void entitiesInLateToAgentAssermente(List<PeopleToSendSmsTo> listeUsers, String[] mails, String emailAssermente){
        if(checkSendingParameter()) {
            try {
                MimeMessage mimeMessage = emailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,
                        "utf-8");
                StringBuilder contenu = new StringBuilder();
                contenu.append("<h2> Liste des personnes </h2>");
                contenu.append("<table style='border: 1px solid black; border-collapse: collapse;'>");
                contenu.append("<tr><th style='border: 1px solid black; border-collapse: collapse'>Nom</th><th style='border: 1px solid black; border-collapse: collapse'>Contact</th><th style='border: 1px solid black; border-collapse: collapse'>Commune activité</th>");
                contenu.append("<th style='border: 1px solid black; border-collapse: collapse'>Quartier d'activité</th><th style='border: 1px solid black; border-collapse: collapse'>Montant déjà payé</th><th style='border: 1px solid black; border-collapse: collapse'>Profil</th></tr>");
                for(PeopleToSendSmsTo po : listeUsers){
                    contenu.append("<tr><td style='border: 1px solid black; border-collapse: collapse'>");
                    contenu.append(po.nom());
                    contenu.append("</td><td style='border: 1px solid black; border-collapse: collapse'>");
                    contenu.append(po.contact());
                    contenu.append("</td><td style='border: 1px solid black; border-collapse: collapse'>");
                    contenu.append(po.commune());
                    contenu.append("</td><td style='border: 1px solid black; border-collapse: collapse'>");
                    contenu.append(po.quartier());
                    contenu.append("</td><td style='border: 1px solid black; border-collapse: collapse'>");
                    contenu.append(po.solde());
                    contenu.append("</td><td style='border: 1px solid black; border-collapse: collapse'>");
                    contenu.append(po.profil());
                    contenu.append("</td></tr>");
                }
                contenu.append("</table>");

                // Envoi du MAIL :
                helper.setText(String.valueOf(contenu), true);
                helper.setTo(emailAssermente);
                helper.setCc(mails);
                helper.setSubject("Personnes en retard de régularisation");
                helper.setFrom(emailSenderAddress);
                emailSender.send(mimeMessage);

                // Raise FLAG for the FIRST MAIL:
                Utilisateur user = utilisateurRepository.findByEmail(emailAssermente).get();
                notificationControleRepository.save(
                        NotificationControle.builder()
                        .utilisateur(user)
                        .build());
                log.info("Mail de RAPPEL envoyé à : {}", emailAssermente);
            } catch (Exception exc) {
                System.out.println("mailCreation(...) : " + exc.toString());
                //log.error("mailCreation(...) : {}", exc.toString());
            }
        }
    }

    public void mailArtisansNotSoldOutYet(List<LibelleTotal> listeDonne, String responsableAssermente, String[] mails){
        if(checkSendingParameter()) {
            try {
                MimeMessage mimeMessage = emailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,
                        "utf-8");
                StringBuilder contenu = new StringBuilder();
                contenu.append("<h2> Nombre des Artisans / CRM </h2>");
                contenu.append("<div> Bonjour Mr <span style='font-weight: bold'>Coulibaly</span>. Nous vous prions de trouver ci-dessous le </div>");
                contenu.append("<div> Total des Artisans par CRM n'ayant pas encore fini le paiement des frais </div>");
                contenu.append("<table style='border: 1px solid black; border-collapse: collapse;'>");
                contenu.append("<tr><th style='border: 1px solid black; border-collapse: collapse'>CRM</th><th style='border: 1px solid black; border-collapse: collapse'>Total</th></tr>");
                for(LibelleTotal dt : listeDonne){
                    contenu.append("<tr><td style='border: 1px solid black; border-collapse: collapse'>");
                    contenu.append(dt.commune());
                    contenu.append("</td><td style='border: 1px solid black; border-collapse: collapse'>");
                    contenu.append(dt.total());
                    contenu.append("</td></tr>");
                }
                contenu.append("</table>");

                // Envoi du MAIL :
                helper.setText(String.valueOf(contenu), true);
                helper.setTo(responsableAssermente);
                helper.setCc(mails);
                helper.setSubject("Total des Artisans/CRM n'ayant pas encore soldé");
                helper.setFrom(emailSenderAddress);
                emailSender.send(mimeMessage);
            } catch (Exception exc) {
                System.out.println("mailCreation(...) : " + exc.toString());
                //log.error("mailCreation(...) : {}", exc.toString());
            }
        }
    }

    public void mailResumeActionAssermente(List<AssermenteAction> listeDonne, String mailTo, String[] mails){
        if(checkSendingParameter()) {
            try {
                MimeMessage mimeMessage = emailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,
                        "utf-8");
                StringBuilder contenu = new StringBuilder();
                contenu.append("<h2> ACTIONS TERRAINS </h2>");
                if(listeDonne.isEmpty()){
                    contenu.append("<h3>Aucune ACTION TERRAIN n'a été menée aujourd'hui par l'équipe de contrôle !</h3>");
                }
                else {
                    contenu.append("<div> Bonjour Mr <span style='font-weight: bold'>Coulibaly</span>. Nous vous prions de trouver ci-dessous la </div>");
                    contenu.append("<div> liste des Artisans revus par les AGENTS ASSERMENTES </div>");
                    contenu.append("<table style='border: 1px solid black; border-collapse: collapse;'>");
                    contenu.append("<tr><th style='border: 1px solid black; border-collapse: collapse'>AGENT</th><th style='border: 1px solid black; border-collapse: collapse'>ENTIT&Eacute;S</th>");
                    contenu.append("<th style='border: 1px solid black; border-collapse: collapse'>SOMME ENCAISSEE</th></tr>");
                    for(AssermenteAction dt : listeDonne){
                        contenu.append("<tr><td style='border: 1px solid black; border-collapse: collapse'>");
                        contenu.append(dt.agent());
                        contenu.append("</td><td style='border: 1px solid black; border-collapse: collapse'>");
                        contenu.append(dt.artisan());
                        contenu.append("</td><td style='border: 1px solid black; border-collapse: collapse'>");
                        contenu.append(dt.total());
                        contenu.append("</td></tr>");
                    }
                    contenu.append("</table>");
                }
                // Envoi du MAIL :
                helper.setText(String.valueOf(contenu), true);
                helper.setTo(mailTo);
                helper.setCc(mails);
                helper.setSubject("Bilan des ACTIONS TERRAINS - AGENTS ASSERMENTES");
                helper.setFrom(emailSenderAddress);
                emailSender.send(mimeMessage);
            } catch (Exception exc) {
                System.out.println("mailCreation(...) : " + exc.toString());
                //log.error("mailCreation(...) : {}", exc.toString());
            }
        }
    }

}
