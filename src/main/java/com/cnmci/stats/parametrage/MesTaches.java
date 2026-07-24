package com.cnmci.stats.parametrage;

import com.cnmci.core.model.*;
import com.cnmci.stats.LibelleTotal;
import com.cnmci.stats.beans.AssermenteAction;
import com.cnmci.stats.beans.EntitePaidNotReceivingDocument;
import com.cnmci.stats.beans.PeopleToSendSmsTo;
import com.cnmci.stats.beans.UtilisateurNotifcationTaille;
import com.cnmci.stats.repository.*;
import com.cnmci.stats.service.MailService;
import com.cnmci.stats.service.SmsService;
import jakarta.persistence.Tuple;
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

    // Reset ACTION_TERRAIN Values    Europe/Paris
    // second minute hour day month weekday
    @Scheduled(cron="0 0 15 * * MON-FRI", zone="Africa/Nouakchott")
    @Transactional
    public void resetActionTerrainValues(){
        List<ActionTerrain> listeActions = actionTerrainRepository.findAllByActifAndSent(true, true);
        listeActions.forEach(
                a -> {
                    a.setSent(false);
                    actionTerrainRepository.save(a);
                }
        );
    }

    // Send AGENT ASSERMENTé ACTION    Europe/Paris
    @Scheduled(cron="0 45 15 * * MON-FRI", zone="Africa/Nouakchott")
    @Transactional
    public void sendAgentAssermenteActionOutside(){
        try{
            List<Tuple> liste = artisanRepository.getArtisanListAssignedToAgentAssermente();
            List<AssermenteAction> listeAction = liste.stream()
                    .map(l -> new AssermenteAction(
                                    l.get("agent_assermentes", String.class),
                                    l.get("artisans", String.class),
                                    l.get("somme_encaisse", Long.class)
                            )
                    ).toList();
            // Send the MAIL if NEEDED :
            List<Utilisateur> listeAgentAssermente =
                    utilisateurRepository.findAllByProfil(profilRepository.findById(11L).get());
            List<String> listeEnCopie = new ArrayList<>();
            listeEnCopie.addAll(listeAgentAssermente.stream()
                    .map(l -> l.getEmail().trim())
                    .toList());
            //listeEnCopie.add("lancidiomande@gmail.com");
            listeEnCopie.add("mbambi@sfpci.com");
            listeEnCopie.add("arnaud.koffi@sfpci.com");
            listeEnCopie.add("koneyibrahima@gmail.com");
            listeEnCopie.add("yfulgence10@gmail.com");
            String[] tabEmail = listeEnCopie.toArray(new String[0]);
            if(!listeAction.isEmpty()) {
                mailService.mailResumeActionAssermente(listeAction, "gvamaracoulibaly@gmail.com", tabEmail);
            }
        } catch (Exception e) {
            System.out.println("sendAgentAssermenteActionOutside(...) : " + e.toString());
        }
    }

    @Scheduled(cron="0 30 15 * * MON-FRI", zone="Africa/Nouakchott")
    @Transactional
    public void sendTotalNotPaidArtisanByCrm(){
        try{
            List<Tuple> listeArtisans = artisanRepository.getArtisanByCrmNotSoldOutYet();
            List<LibelleTotal> listeDonne = listeArtisans.stream()
                    .map(l -> new LibelleTotal(
                            l.get("label", String.class),
                            l.get("tot", Long.class)
                    ))
                    .toList();
            // Pick all 'ROLE_FORMALITE_CRM' :
            List<Utilisateur> listeSG = utilisateurRepository.findAllByProfil(profilRepository.findById(5L).get());
            List<String> listeEnCopie = new ArrayList<>();
            listeEnCopie.addAll(listeSG.stream()
                    .map(l -> l.getEmail().trim())
                    .toList());
            //listeEnCopie.add("lancidiomande@gmail.com");
            listeEnCopie.add("mbambi@sfpci.com");
            listeEnCopie.add("arnaud.koffi@sfpci.com");
            listeEnCopie.add("koneyibrahima@gmail.com");
            listeEnCopie.add("yfulgence10@gmail.com");
            listeEnCopie.add("princedesirekoffi@gmail.com");
            String[] tabEmail = listeEnCopie.toArray(new String[0]);
            mailService.mailArtisansNotSoldOutYet(listeDonne, "gvamaracoulibaly@gmail.com", tabEmail);
        } catch (Exception e) {
            System.out.println("sendTotalNotPaidArtisanByCrm(...) : " + e.toString());
        }
    }

    @Scheduled(cron="0 */20 9-11 * * MON-FRI", zone="Africa/Nouakchott")
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
            // PICK ONLY ID's
            /*listeTransmission.forEach(
                a -> {
                    a.setUtilisateurAgentAssermente(act.getUtilisateur());
                    a.setDateAssignationAssermente(OffsetDateTime.now());
                    artisanRepository.save(a);
                }
            );*/
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
            long idUser = holdAction.getUtilisateur().getId();
            smsService.sendMessage(listeUsers, idUser);
            // Send email to AGENT ASSERMENTE :
            String emailAssermente = holdAction.getUtilisateur().getEmail();
            String[] listeMails = new String[2];
            // Add more addresses :
            //listeMails[0] = "lancidiomande@gmail.com";
            listeMails[0] = "mbambi@sfpci.com";
            listeMails[1] = "arnaud.koffi@sfpci.com";
            mailService.entitiesInLateToAgentAssermente(listeUsers, listeMails,
                    emailAssermente);
            // Update FLAG :
            holdAction.setSent(true);
            actionTerrainRepository.save(holdAction);
        }
    }

    @Scheduled(cron="0 50 15 * * MON-FRI", zone="Africa/Nouakchott")
    @Transactional
    public void sendReportForThoseWhoPaidAndNotReceivingDocument(){
        try{
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            List<Artisan> listeArtisan = artisanRepository.getArtisanWhoPaidAndNotReceivingDocument();
            List<EntitePaidNotReceivingDocument> listeDonnee = listeArtisan.stream()
                    .map(a -> new EntitePaidNotReceivingDocument(
                            a.getNom() + " " + a.getPrenom(),
                            a.getContact1(),
                            a.getNoteSuiviCallCenter(),
                            a.getSuiviCallCenterDate().format(dateTimeFormatter),
                            a.getMetier().getLibelle(),
                            a.getActivite().getCommune().getLibelle()
                    )
                    ).toList();

            // Send the MAIL if NEEDED :
            List<Utilisateur> listeAgentCallCenter =
                    utilisateurRepository.findAllByProfil(profilRepository.findById(16L).get());
            List<String> listeEnCopie = new ArrayList<>();
            listeEnCopie.addAll(listeAgentCallCenter.stream()
                    .map(l -> l.getEmail().trim())
                    .toList());
            listeEnCopie.add("mbambi@sfpci.com");
            listeEnCopie.add("arnaud.koffi@sfpci.com");
            listeEnCopie.add("koneyibrahima@gmail.com");
            listeEnCopie.add("yfulgence10@gmail.com");
            String[] tabEmail = listeEnCopie.toArray(new String[0]);
            if(!listeDonnee.isEmpty()) {
                mailService.mailAboutThosePayingAndNotGivingBackDocument(listeDonnee, "gvamaracoulibaly@gmail.com", tabEmail);
            }
        } catch (Exception e) {
            System.out.println("sendReportForThoseWhoPaidAndNotReceivingDocument(...) : " + e.toString());
        }
    }
}
