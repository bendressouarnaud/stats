package com.cnmci.stats.parametrage;

import com.cnmci.core.model.Artisan;
import com.cnmci.stats.repository.ArtisanRepository;
import com.cnmci.stats.service.SmsService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.OffsetDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class MesTaches {

    // A t t r i b u t e s :
    private static final int DELAI_MOIS_APRES_ENROLEMENT = 3;
    private static final String PREFIX_NUMBER_ID = "+225";
    @Autowired
    ArtisanRepository artisanRepository;
    @Autowired
    SmsService smsService;


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
    //@Scheduled(cron="0 0 10 * * *", zone="Africa/Nouakchott")  // tous les jours à 9h
    @Scheduled(cron="0 * 10 * * *", zone="Africa/Nouakchott")  // toutes les minutes
    public void checkEnrolmentDelay(){
        // Pick
        System.out.println("Démarrage de l'envoi de rappel SMS");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Artisan> listeArtisan = artisanRepository.findAllByRappelSmsAndStatutPaiementIn(0, List.of(0, 1));
        listeArtisan.stream()
            .filter(
                    a -> compareDates(a.getCreatedAt()) >= DELAI_MOIS_APRES_ENROLEMENT
            )
            .forEach(
                a -> {
                    // Send
                    smsService.sendMessage(a.getNom(), checkContact(a.getContact1()),
                            a.getCreatedAt().format(dateTimeFormatter), a.getId());
                }
            );
        //
    }
}
