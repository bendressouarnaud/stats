package com.cnmci.stats.parametrage;

import com.cnmci.core.model.*;
import com.cnmci.stats.beans.PeopleToSendSmsTo;
import com.cnmci.stats.beans.UtilisateurNotifcationTaille;
import com.cnmci.stats.repository.*;
import com.cnmci.stats.service.MailService;
import com.cnmci.stats.service.SmsService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class MesTaches {

    // A t t r i b u t e s :
    private static final int DELAI_MOIS_APRES_ENROLEMENT = 3;
    private static final String PREFIX_NUMBER_ID = "+225";
    private static final int LIMIT_USER = 20;
    @Autowired
    ArtisanRepository artisanRepository;
    @Autowired
    ActionTerrainRepository actionTerrainRepository;
    @Autowired
    ApprentiRepository apprentiRepository;
    @Autowired
    CompagnonRepository compagnonRepository;
    @Autowired
    UtilisateurRepository utilisateurRepository;
    @Autowired
    CommuneRepository communeRepository;
    @Autowired
    ProfilRepository profilRepository;
    @Autowired
    SmsService smsService;
    @Autowired
    MailService mailService;


    // M E T H O D S :
    private int compareDates(OffsetDateTime created){
        OffsetDateTime today = OffsetDateTime.now();
        Period period = Period.between(
                created.toLocalDate(), // start
                today.toLocalDate()    // end
        );
        // The result of this method can be a negative period if the end is before the start.
        return period.getMonths();
    }

    private String checkContact(String contact){
        return contact.trim().startsWith(PREFIX_NUMBER_ID) ? contact : (PREFIX_NUMBER_ID + contact);
    }

    //
    @Scheduled(cron="0 */20 9-11 * * *", zone="Africa/Nouakchott")
    @Transactional
    public void checkEnrolmentDelay(){
        List<PeopleToSendSmsTo> listeUsers = new ArrayList<>();
        // Pick PARAMETRIZED DATA from all USERS :
        List<ActionTerrain> listeActions = actionTerrainRepository.findAllByActifAndSent(true, false);
        ActionTerrain holdAction = null;
        for(ActionTerrain act : listeActions){
            List<Artisan> listeArtisan = artisanRepository.findAllByQuartierSiege(act.getQuartier().getId());
            List<Artisan> listeTransmission = listeArtisan.stream()
                    .filter(
                            a -> compareDates(a.getCreatedAt()) >= DELAI_MOIS_APRES_ENROLEMENT
                    ).toList();
            // Appply LIMITATION from there :
            var taille = listeTransmission.size();
            listeUsers.addAll(listeTransmission.subList(0, Math.min(LIMIT_USER, taille)).stream()
                    .map(
                            a -> new PeopleToSendSmsTo(a.getNom(), checkContact(a.getContact1()),
                                    0, a.getId(), a.getActivite().getCommune().getLibelle(),
                                    (a.getActivite().getQuartierSiegeId() != null ?
                                            a.getActivite().getQuartierSiegeId().getLibelle() : "---"),
                                    a.getPaiementEnrolements().isEmpty() ? 0 :
                                            a.getPaiementEnrolements().stream().
                                                    mapToInt(PaiementEnrolement::getMontant).sum(),
                                    "Artisan")
                    ).toList());
            // Hold USER's
            holdAction = act;
            // Stop :
            break;
        }

        // Pick
        /*System.out.println("Démarrage de l'envoi de rappel SMS");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Artisan> listeArtisan = artisanRepository.findAllByRappelSmsAndStatutPaiementIn(0, List.of(0, 1));
        List<Artisan> listeTransmission = listeArtisan.stream()
            .filter(
                    a -> compareDates(a.getCreatedAt()) >= DELAI_MOIS_APRES_ENROLEMENT
            ).toList();
        // Appply LIMITATION from there :
        var taille = listeTransmission.size();
        listeUsers.addAll(listeTransmission.subList(0, Math.min(LIMIT_USER, taille)).stream()
            .map(
                a -> new PeopleToSendSmsTo(a.getNom(), checkContact(a.getContact1()),
                        0, a.getId(), a.getActivite().getCommune().getLibelle(),
                        (a.getActivite().getQuartierSiegeId() != null ?
                                a.getActivite().getQuartierSiegeId().getLibelle() : "---"),
                        a.getPaiementEnrolements().isEmpty() ? 0 :
                                a.getPaiementEnrolements().stream().
                                        mapToInt(PaiementEnrolement::getMontant).sum(),
                        "Artisan")
            ).toList());
        //

        // APPRENTI
        List<Apprenti> listeApprenti = apprentiRepository.findAllByRappelSmsAndStatutPaiementIn(0, List.of(0, 1));
        List<Apprenti> listeTransmissionApprenti = listeApprenti.stream()
                .filter(
                        a -> compareDates(a.getCreatedAt()) >= DELAI_MOIS_APRES_ENROLEMENT
                ).toList();
        // Appply LIMITATION from there :
        var tailleApprenti = listeTransmissionApprenti.size();
        listeUsers.addAll(listeTransmissionApprenti.subList(0, Math.min(LIMIT_USER, tailleApprenti)).stream()
                .map(
                        a -> new PeopleToSendSmsTo(a.getNom(), checkContact(a.getContact1()),
                                1, a.getId(), "",
                                "",
                                a.getPaiementEnrolements().isEmpty() ? 0 :
                                        a.getPaiementEnrolements().stream().
                                                mapToInt(PaiementEnrolement::getMontant).sum(),
                                "Apprenti")
                ).toList());

        // COMPAGNON :
        List<Compagnon> listeCompagnon = compagnonRepository.findAllByRappelSmsAndStatutPaiementIn(0, List.of(0, 1));
        List<Compagnon> listeTransmissionCompagnon = listeCompagnon.stream()
                .filter(
                        a -> compareDates(a.getCreatedAt()) >= DELAI_MOIS_APRES_ENROLEMENT
                ).toList();
        // Appply LIMITATION from there :
        var tailleCompagnon = listeTransmissionCompagnon.size();
        listeUsers.addAll(listeTransmissionCompagnon.subList(0, Math.min(LIMIT_USER, tailleCompagnon)).stream()
                .map(
                        a -> new PeopleToSendSmsTo(a.getNom(), checkContact(a.getContact1()),
                                2, a.getId(), "",
                                "",
                                a.getPaiementEnrolements().isEmpty() ? 0 :
                                        a.getPaiementEnrolements().stream().
                                                mapToInt(PaiementEnrolement::getMontant).sum(),
                                "Compagnon")
                ).toList());

        System.out.println("Total des entittés SMS : " + String.valueOf(listeUsers.size()));
        */

        // Send :
        if(!listeUsers.isEmpty()){
            smsService.sendMessage(listeUsers);
            // Send email to AGENT ASSERMENTE :
            String emailAssermente = holdAction.getUtilisateur().getEmail();
            String[] listeMails = new String[3];
            // Add more addresses :
            listeMails[0] = "lancidiomande@gmail.com";
            listeMails[1] = "mbambi@sfpci.com";
            listeMails[2] = "ngbandamakonan@gmail.com";
            mailService.entitiesInLateToAgentAssermente(listeUsers, listeMails,
                    emailAssermente);
            // Update FLAG :
            holdAction.setSent(true);
            actionTerrainRepository.save(holdAction);
        }
    }
}
