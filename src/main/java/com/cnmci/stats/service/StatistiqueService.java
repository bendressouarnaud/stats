package com.cnmci.stats.service;

import com.cnmci.core.enums.StatutType;
import com.cnmci.core.model.*;
import com.cnmci.stats.beans.*;
import com.cnmci.stats.repository.*;
import jakarta.persistence.Tuple;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.round;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatistiqueService {

    // A T T R I B U T E S :
    private final ArtisanRepository artisanRepository;
    private final ApprentiRepository apprentiRepository;
    private final CompagnonRepository compagnonRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final PaiementEnrolementRepository paiementEnrolementRepository;
    private final OutilService outilService;
    private final UtilisateurRepository utilisateurRepository;


    // M E T H O D S :
    private double roundValue(double value){
        BigDecimal initialDecimal = new BigDecimal(value);
        BigDecimal valueRounded = initialDecimal.setScale(2, RoundingMode.HALF_UP);
        return valueRounded.doubleValue();
    }

    private String getImageIfExists(String image){
        return image != null ? image : "";
    }

    private double processGeoData(Double data){
        return data != null ? data : 0.0;
    }

    public List<DailyPaymentLineChart> getLatestDailyPayments(){
        return paiementEnrolementRepository.findLatestDailyPayments().stream().map(
                p -> DailyPaymentLineChart.builder()
                        .day((p.get("dte", Date.class).toString()).substring(5))
                        .total((p.get("total", Long.class)).doubleValue() / 100000)
                        .build()
        ).toList();
    }

    public List<StatsBean> getEntitiesStatistiques(HttpServletRequest httpServletRequest){

        List<StatsBean> retour = new ArrayList<>();

        String userMail = outilService.getBackUserConnectedName(httpServletRequest);
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userMail.trim()).get();

        switch (utilisateur.getProfil().getCode()){
            case "ROLE_FORMALITE_CRM":
            case "ROLE_PRESIDENT_CRM":
                // ARTISAN :
                Tuple totalArtisanNew = artisanRepository.findTotalArtisanFromCrm(
                        utilisateur.getCrm().getId(), 0);
                Tuple totalArtisanReNew = artisanRepository.findTotalArtisanFromCrm(
                        utilisateur.getCrm().getId(), 3);
                int populationArtisan = (totalArtisanNew.get("tot", Long.class)).intValue();
                int populationArtisanRenew = (totalArtisanReNew.get("tot", Long.class)).intValue();
                long attenduArtisan = (15000L * populationArtisan) + (5000L * populationArtisanRenew);
                // En cours
                Tuple encoursTupleArtisan = paiementEnrolementRepository.
                        findAmountPaidByArtisanFromCrm(utilisateur.getCrm().getId());
                long encoursArtisan = encoursTupleArtisan != null ?
                        encoursTupleArtisan.get("total", Long.class) : 0;
                double pourcentageArtisan = (double) (encoursArtisan * 100) / attenduArtisan;
                retour.add(new StatsBean("Artisans", (populationArtisan + populationArtisanRenew), attenduArtisan, encoursArtisan,
                        pourcentageArtisan > 0 ? roundValue(pourcentageArtisan) : 0));

                // APPRENTI :
                Tuple totalApprenti = apprentiRepository.findTotalFromCrm(utilisateur.getCrm().getId(), 0);
                Tuple totalApprentiRenew = apprentiRepository.findTotalFromCrm(utilisateur.getCrm().getId(), 3);
                int populationApprenti = (totalApprenti.get("total", BigDecimal.class)).intValue();
                int populationApprentiRenew = (totalApprentiRenew.get("total", BigDecimal.class)).intValue();
                long attenduApprenti = 5000L * (populationApprenti + populationApprentiRenew);
                // En cours
                //System.out.println("Id : "+ String.valueOf(utilisateur.getCrm().getId()));
                Tuple encoursTupleApprenti = paiementEnrolementRepository.
                        findAmountPaidByApprentiFromCrm(utilisateur.getCrm().getId());
                long encoursApprenti = encoursTupleApprenti != null ?
                        encoursTupleApprenti.get("total", Long.class) : 0;
                double pourcentageApprenti = (double) (encoursApprenti * 100) / attenduApprenti;
                retour.add(new StatsBean("Apprentis", (populationApprenti + populationApprentiRenew), attenduApprenti, encoursApprenti,
                        pourcentageApprenti > 0 ? roundValue(pourcentageApprenti) : 0));

                // C O M P A G N O N :
                Tuple totalCompagnon = compagnonRepository.findTotalFromCrm(utilisateur.getCrm().getId(), 0);
                Tuple totalCompagnonRenew = compagnonRepository.findTotalFromCrm(utilisateur.getCrm().getId(), 3);
                int populationCompagnon = (totalCompagnon.get("total", BigDecimal.class)).intValue();
                int populationCompagnonRenew = (totalCompagnonRenew.get("total", BigDecimal.class)).intValue();
                long attenduCompagnon = 5000L * (populationCompagnon + populationCompagnonRenew);
                // En cours
                Tuple encoursTupleCompagnon = paiementEnrolementRepository.
                        findAmountPaidByCompagnonFromCrm(utilisateur.getCrm().getId());
                long encoursCompagnon = encoursTupleCompagnon != null ?
                        encoursTupleCompagnon.get("total", Long.class) : 0;
                double pourcentageCompagnon = (double) (encoursCompagnon * 100) / attenduCompagnon;
                retour.add(new StatsBean("Compagnons", (populationCompagnon + populationCompagnonRenew), attenduCompagnon, encoursCompagnon,
                        pourcentageCompagnon > 0 ? roundValue(pourcentageCompagnon) : 0));

                // E N T R E P R I S E :
                Tuple totalEntreprise = entrepriseRepository.findTotalEntrepriseFromCrm(utilisateur.getCrm().getId());
                int populationEntreprise = (totalEntreprise.get("tot", Long.class)).intValue();
                long attenduEntreprise = 25000L * populationEntreprise;
                // En cours
                Tuple encoursTupleEntreprise = paiementEnrolementRepository.
                        findAmountPaidByEntrepriseFromCrm(utilisateur.getCrm().getId());
                long encoursEntreprise = encoursTupleEntreprise != null ?
                        encoursTupleEntreprise.get("total", Long.class) : 0;
                double pourcentageEntreprise = (double) (encoursEntreprise * 100) / attenduEntreprise;
                retour.add(new StatsBean("Entreprises", populationEntreprise, attenduEntreprise, encoursEntreprise,
                        pourcentageEntreprise > 0 ? roundValue(pourcentageEntreprise) : 0));
                break;

            default:
                // ARTISAN :
                Tuple totalArtisanDef = artisanRepository.findTotalArtisan(0);
                Tuple totalArtisanDefRenew = artisanRepository.findTotalArtisan(3);
                int populationArtisanDef = (totalArtisanDef.get("tot", Long.class)).intValue();
                int populationArtisanDefRenew = (totalArtisanDefRenew.get("tot", Long.class)).intValue();
                long attenduArtisanDef = (15000L * populationArtisanDef) + (5000L * populationArtisanDefRenew);
                // En cours
                Tuple encoursTupleArtisanDef = paiementEnrolementRepository.
                        findAmountPaidByArtisan();
                long encoursArtisanDef = encoursTupleArtisanDef.get("total", Long.class);
                double pourcentageArtisanDef = (double) (encoursArtisanDef * 100) / attenduArtisanDef;
                retour.add(new StatsBean("Artisans",
                        (populationArtisanDef + populationArtisanDefRenew),
                        attenduArtisanDef, encoursArtisanDef, roundValue(pourcentageArtisanDef)));

                // APPRENTI :
                Tuple totalApprentiDef = apprentiRepository.findTotal();
                int populationApprentiDef = (totalApprentiDef.get("total", Long.class)).intValue();
                long attenduApprentiDef = 5000L * populationApprentiDef;
                // En cours
                Tuple encoursTupleApprentiDef = paiementEnrolementRepository.
                        findAmountPaidByApprenti();
                long encoursApprentiDef = encoursTupleApprentiDef.get("total", Long.class);
                double pourcentageApprentiDef = (double) (encoursApprentiDef * 100) / attenduApprentiDef;
                retour.add(new StatsBean("Apprentis", populationApprentiDef, attenduApprentiDef, encoursApprentiDef, roundValue(pourcentageApprentiDef)));

                // C O M P A G N O N :
                Tuple totalCompagnonDef = compagnonRepository.findTotal();
                int populationCompagnonDef = (totalCompagnonDef.get("total", Long.class)).intValue();
                long attenduCompagnonDef = 5000L * populationCompagnonDef;
                // En cours
                Tuple encoursTupleCompagnonDef = paiementEnrolementRepository.
                        findAmountPaidByCompagnon();
                long encoursCompagnonDef = encoursTupleCompagnonDef.get("total", Long.class);
                double pourcentageCompagnonDef = (double) (encoursCompagnonDef * 100) / attenduCompagnonDef;
                retour.add(new StatsBean("Compagnons", populationCompagnonDef, attenduCompagnonDef, encoursCompagnonDef, roundValue(pourcentageCompagnonDef)));

                // E N T R E P R I S E :
                Tuple totalEntrepriseDef = entrepriseRepository.findTotalEntreprise();
                int populationEntrepriseDef = (totalEntrepriseDef.get("tot", Long.class)).intValue();
                long attenduEntrepriseDef = 25000L * populationEntrepriseDef;
                // En cours
                Tuple encoursTupleEntrepriseDef = paiementEnrolementRepository.
                        findAmountPaidByEntreprise();
                long encoursEntrepriseDef = encoursTupleEntrepriseDef.get("total", Long.class);
                double pourcentageEntrepriseDef = (double) (encoursEntrepriseDef * 100) / attenduEntrepriseDef;
                retour.add(new StatsBean("Entreprises", populationEntrepriseDef, attenduEntrepriseDef, encoursEntrepriseDef, roundValue(pourcentageEntrepriseDef)));
                break;
        }
        return retour;
    }

    public List<StatsBean> getEntitiesStatistiquesCrm(long idCrm){
        List<StatsBean> retour = new ArrayList<>();
        // ARTISAN :
        Tuple totalArtisanNew = artisanRepository.findTotalArtisanFromCrm(
                idCrm, 0);
        Tuple totalArtisanReNew = artisanRepository.findTotalArtisanFromCrm(
                idCrm, 3);
        int populationArtisan = (totalArtisanNew.get("tot", Long.class)).intValue();
        int populationArtisanRenew = (totalArtisanReNew.get("tot", Long.class)).intValue();
        long attenduArtisan = (15000L * populationArtisan) + (5000L * populationArtisanRenew);
        // En cours
        Tuple encoursTupleArtisan = paiementEnrolementRepository.
                findAmountPaidByArtisanFromCrm(idCrm);
        long encoursArtisan = encoursTupleArtisan != null ?
                encoursTupleArtisan.get("total", Long.class) : 0;
        double pourcentageArtisan = (double) (encoursArtisan * 100) / attenduArtisan;
        retour.add(new StatsBean("Artisans", (populationArtisan + populationArtisanRenew), attenduArtisan, encoursArtisan,
                pourcentageArtisan > 0 ? roundValue(pourcentageArtisan) : 0));

        // APPRENTI :
        Tuple totalApprenti = apprentiRepository.findTotalFromCrm(idCrm, 0);
        Tuple totalApprentiRenew = apprentiRepository.findTotalFromCrm(idCrm, 3);
        int populationApprenti = (totalApprenti.get("total", BigDecimal.class)).intValue();
        int populationApprentiRenew = (totalApprentiRenew.get("total", BigDecimal.class)).intValue();
        long attenduApprenti = 5000L * (populationApprenti + populationApprentiRenew);
        // En cours
        //System.out.println("Id : "+ String.valueOf(utilisateur.getCrm().getId()));
        Tuple encoursTupleApprenti = paiementEnrolementRepository.
                findAmountPaidByApprentiFromCrm(idCrm);
        long encoursApprenti = encoursTupleApprenti != null ?
                encoursTupleApprenti.get("total", Long.class) : 0;
        double pourcentageApprenti = (double) (encoursApprenti * 100) / attenduApprenti;
        retour.add(new StatsBean("Apprentis", (populationApprenti + populationApprentiRenew), attenduApprenti, encoursApprenti,
                pourcentageApprenti > 0 ? roundValue(pourcentageApprenti) : 0));

        // C O M P A G N O N :
        Tuple totalCompagnon = compagnonRepository.findTotalFromCrm(idCrm, 0);
        Tuple totalCompagnonRenew = compagnonRepository.findTotalFromCrm(idCrm, 3);
        int populationCompagnon = (totalCompagnon.get("total", BigDecimal.class)).intValue();
        int populationCompagnonRenew = (totalCompagnonRenew.get("total", BigDecimal.class)).intValue();
        long attenduCompagnon = 5000L * (populationCompagnon + populationCompagnonRenew);
        // En cours
        Tuple encoursTupleCompagnon = paiementEnrolementRepository.
                findAmountPaidByCompagnonFromCrm(idCrm);
        long encoursCompagnon = encoursTupleCompagnon != null ?
                encoursTupleCompagnon.get("total", Long.class) : 0;
        double pourcentageCompagnon = (double) (encoursCompagnon * 100) / attenduCompagnon;
        retour.add(new StatsBean("Compagnons", (populationCompagnon + populationCompagnonRenew), attenduCompagnon, encoursCompagnon,
                pourcentageCompagnon > 0 ? roundValue(pourcentageCompagnon) : 0));

        // E N T R E P R I S E :
        Tuple totalEntreprise = entrepriseRepository.findTotalEntrepriseFromCrm(idCrm);
        int populationEntreprise = (totalEntreprise.get("tot", Long.class)).intValue();
        long attenduEntreprise = 25000L * populationEntreprise;
        // En cours
        Tuple encoursTupleEntreprise = paiementEnrolementRepository.
                findAmountPaidByEntrepriseFromCrm(idCrm);
        long encoursEntreprise = encoursTupleEntreprise != null ?
                encoursTupleEntreprise.get("total", Long.class) : 0;
        double pourcentageEntreprise = (double) (encoursEntreprise * 100) / attenduEntreprise;
        retour.add(new StatsBean("Entreprises", populationEntreprise, attenduEntreprise, encoursEntreprise,
                pourcentageEntreprise > 0 ? roundValue(pourcentageEntreprise) : 0));
        return retour;
    }


    public List<EntitySearchResponse> processAgentAssermenteRequest(ControleAgentSermenteRequest request){
        List<EntitySearchResponse> retour = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // ARTISAN
        List<Artisan> artisans = artisanRepository.findAllArtisanFromAgentAsserment(request.getQuartier(), request.getEtat(),
                request.getDebut(), request.getFin());
        retour.addAll(artisans.stream().map(
                a -> EntitySearchResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom() + " " +a.getPrenom())
                        .contact(a.getContact1())
                        .datenaissance(a.getDateNaissance().format(dateTimeFormatter))
                        .metier(a.getMetier().getLibelle())
                        .paiement(a.getStatutPaiement())
                        .commune(a.getCommuneResidence().getLibelle())
                        .type("Artisans")
                        .image(getImageIfExists(a.getPhotoArtisan()))
                        .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
                        .quartier(a.getActivite().getQuartierSiegeId().getLibelle()) // Quartier de l'ACTIVITE
                        .amende(a.getAmendes().size())
                        .latitude(processGeoData(a.getLatitude()))
                        .longitude(processGeoData(a.getLongitude()))
                        .build()
        ).toList());

        // APPRENTI
        List<Apprenti> apprentis = apprentiRepository.findAllApprentiFromAgentAsserment(request.getQuartier(), request.getEtat(),
                request.getDebut(), request.getFin());
        retour.addAll(apprentis.stream().map(
                a -> EntitySearchResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom() + " " +a.getPrenom())
                        .contact(a.getContact1())
                        .datenaissance(a.getDateNaissance().format(dateTimeFormatter))
                        .metier(a.getMetier().getLibelle())
                        .paiement(a.getStatutPaiement())
                        .commune(a.getCommuneResidence().getLibelle())
                        .type("Apprentis")
                        .image(getImageIfExists(a.getPhotoApprenti()))
                        .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
                        .quartier(
                                a.getArtisanApprentis().isEmpty() ?
                                        a.getEntrepriseApprentis().stream().findFirst().get().getEntreprise().getQuartierSiegeId().getLibelle() :
                                        a.getArtisanApprentis().stream().findFirst().get().getArtisan().getActivite().getQuartierSiegeId().getLibelle()
                        )
                        .amende(a.getAmendes().size())
                        .latitude(processGeoData(a.getLatitude()))
                        .longitude(processGeoData(a.getLongitude()))
                        .build()
        ).toList());

        // COMPAGNON
        List<Compagnon> compagnons = compagnonRepository.findAllCompagnonFromAgentAsserment(request.getQuartier(), request.getEtat(),
                request.getDebut(), request.getFin());
        retour.addAll(compagnons.stream().map(
                a -> EntitySearchResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom() + " " +a.getPrenom())
                        .contact(a.getContact1())
                        .datenaissance(a.getDateNaissance().format(dateTimeFormatter))
                        .metier(a.getMetier().getLibelle())
                        .paiement(a.getStatutPaiement())
                        .commune(a.getCommuneResidence().getLibelle())
                        .type("Compagnons")
                        .image(getImageIfExists(a.getPhotoCompagnon()))
                        .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
                        .quartier(
                                a.getArtisanCompagnons().isEmpty() ?
                                        a.getEntrepriseCompagnons().stream().findFirst().get().getEntreprise().getQuartierSiegeId().getLibelle() :
                                        a.getArtisanCompagnons().stream().findFirst().get().getArtisan().getActivite().getQuartierSiegeId().getLibelle()
                        )
                        .amende(a.getAmendes().size())
                        .latitude(processGeoData(a.getLatitude()))
                        .longitude(processGeoData(a.getLongitude()))
                        .build()
        ).toList());

        // ENTREPRISE :
        List<Entreprise> entreprises = entrepriseRepository.findAllEntrepriseFromAgentAsserment(request.getQuartier(), request.getEtat(),
                request.getDebut(), request.getFin());
        retour.addAll(entreprises.stream().map(
                a -> EntitySearchResponse.builder()
            .id(a.getId())
            .nom(a.getRaisonSociale())
            .contact(a.getContact())
            .datenaissance(a.getDateCreation().format(dateTimeFormatter))
            .metier(a.getActivitePrincipale().getLibelle())
            .paiement(a.getStatutPaiement())
            .commune(a.getCommune().getLibelle())
            .type("Entreprises")
            .image("")
            .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
            .quartier(a.getQuartierSiegeId().getLibelle())
            .amende(a.getAmendes().size())
            .latitude(processGeoData(a.getLatitude()))
            .longitude(processGeoData(a.getLongitude()))
            .build()
        ).toList());
        return retour;
    }


    //private int getAmountAlreadyPaid()


    public List<EntitySearchResponse> lookForAnyEntity(String search){
        List<EntitySearchResponse> retour = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // ARTISAN
        List<Artisan> artisans = artisanRepository.
                findAllArtisanByNomPrenomOrContact(search.trim().toLowerCase());
        retour.addAll(artisans.stream().map(
        a -> EntitySearchResponse.builder()
            .id(a.getId())
            .nom(a.getNom() + " " +a.getPrenom())
            .contact(a.getContact1())
            .datenaissance(a.getDateNaissance().format(dateTimeFormatter))
            .metier(a.getMetier().getLibelle())
            .paiement(a.getStatutPaiement())
            .commune(a.getCommuneResidence().getLibelle())
            .type("Artisans")
            .image(getImageIfExists(a.getPhotoArtisan()))
            .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
            .quartier(a.getQuartierResidence()) // Quartier de l'ACTIVITE
            .amende(a.getAmendes().size())
            .latitude(processGeoData(a.getLatitude()))
            .longitude(processGeoData(a.getLongitude()))
            .montant((a.getStatutType() == StatutType.ENROLE ? 15000 : 5000) -
                    (paiementEnrolementRepository.findAllByArtisan(a).stream().mapToInt(
                        PaiementEnrolement::getMontant).sum()))
            .build()
        ).toList());

        // ApPRENTI
        List<Apprenti> apprentis = apprentiRepository.
                findAllApprentiByNomPrenomOrContact(search.trim().toLowerCase());
        retour.addAll(apprentis.stream().map(
                a -> EntitySearchResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom() + " " +a.getPrenom())
                        .contact(a.getContact1())
                        .datenaissance(a.getDateNaissance() != null ?
                                a.getDateNaissance().format(dateTimeFormatter) : "")
                        .metier(a.getMetier().getLibelle())
                        .paiement(a.getStatutPaiement())
                        .commune(a.getCommuneResidence().getLibelle())
                        .type("Apprentis")
                        .image(getImageIfExists(a.getPhotoApprenti()))
                        .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
                        .quartier(a.getQuartierResidence())
                        .amende(a.getAmendes().size())
                        .latitude(processGeoData(a.getLatitude()))
                        .longitude(processGeoData(a.getLongitude()))
                        .montant(5000 - (paiementEnrolementRepository.findAllByApprenti(a).stream().mapToInt(
                                PaiementEnrolement::getMontant).sum()))
                        .build()
        ).toList());

        // COMPAGNON
        List<Compagnon> compagnons = compagnonRepository.
                findAllCompagnonByNomPrenomOrContact(search.trim().toLowerCase());
        retour.addAll(compagnons.stream().map(
                a -> EntitySearchResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom() + " " +a.getPrenom())
                        .contact(a.getContact1())
                        .datenaissance(a.getDateNaissance().format(dateTimeFormatter))
                        .metier(a.getMetier().getLibelle())
                        .paiement(a.getStatutPaiement())
                        .commune(a.getCommuneResidence().getLibelle())
                        .type("Compagnons")
                        .image(getImageIfExists(a.getPhotoCompagnon()))
                        .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
                        .quartier(a.getQuartierResidence())
                        .amende(a.getAmendes().size())
                        .latitude(processGeoData(a.getLatitude()))
                        .longitude(processGeoData(a.getLongitude()))
                        .montant(5000 - (paiementEnrolementRepository.findAllByCompagnon(a).stream().mapToInt(
                                PaiementEnrolement::getMontant).sum()))
                        .build()
        ).toList());

        // ENTREPRISES
        List<Entreprise> entreprises = entrepriseRepository.
                findAllEntrepriseByRaisonSocialeOrContact(search.trim().toLowerCase());
        retour.addAll(entreprises.stream().map(
                        a -> EntitySearchResponse.builder()
                                .id(a.getId())
                                .nom(a.getRaisonSociale())
                                .contact(a.getContact())
                                .datenaissance(a.getDateCreation() != null ?
                                        a.getDateCreation().format(dateTimeFormatter) : "")
                                .metier(a.getActivitePrincipale().getLibelle())
                                .paiement(a.getStatutPaiement())
                                .commune(a.getCommune().getLibelle())
                                .type("Entreprises")
                                .image("")
                                .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
                                .quartier(a.getQuartierSiegeId().getLibelle())
                                .amende(a.getAmendes().size())
                                .latitude(processGeoData(a.getLatitude()))
                                .longitude(processGeoData(a.getLongitude()))
                                .montant(25000 - (paiementEnrolementRepository.findAllByEntreprise(a).stream().mapToInt(
                                        PaiementEnrolement::getMontant).sum()))
                                .build()
                ).toList());
        return retour;
    }

    private double processDouble(double valeur){
        BigDecimal startValue = new BigDecimal(valeur);
        return startValue.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Les métiers les plus représentés :
    public List<StatsData> getRepartitionMetierByCommune(long communeId){
        List<Tuple> liste = artisanRepository.getMetierByCommune(communeId);
        var totalDonnee =  liste.stream().mapToLong(l -> l.get("total", Long.class)).sum();
        // Now compute in PERCENTAGE :
        return liste.stream()
                .map(
                        l -> new StatsData(
                                l.get("libelle", String.class),
                                processDouble(((double)(l.get("total", Long.class) * 100) / totalDonnee))
                        )
                ).toList();
    }

    // Population d'ARTISAN par COMMUNE de NAISSANCE
    public List<StatsData> getArtisanFromBirthPlaceByCommune(long communeId){
        List<Tuple> liste = artisanRepository.getArtisanFromBirthPlaceByCommune(communeId);
        var totalDonnee =  liste.stream().mapToLong(l -> l.get("total", Long.class)).sum();
        // Now compute in PERCENTAGE :
        return liste.stream()
                .map(
                        l -> new StatsData(
                                l.get("libelle", String.class),
                                processDouble(((double)(l.get("total", Long.class) * 100) / totalDonnee))
                        )
                ).toList();
    }

    // Repartition de paiement par secteur d’activité
    public List<StatsData> getPaymentByActivite(long communeId){
        List<Tuple> liste = artisanRepository.getPaymentByActivite(communeId);
        var totalDonnee =  liste.stream().mapToLong(l -> l.get("total", Long.class)).sum();
        // Now compute in PERCENTAGE :
        return liste.stream()
                .map(
                        l -> new StatsData(
                                l.get("libelle", String.class),
                                processDouble(((double)(l.get("total", Long.class) * 100) / totalDonnee))
                        )
                ).toList();
    }

    // Display per TOWN, the number of ENTITIES BY NEIGHBORHOOD :
    public List<EntityFromQuartier> getEntitiesByQuartier(long idCommune){
        return artisanRepository.findAllEntitiesFromTown(idCommune).stream()
                .map(a -> EntityFromQuartier.builder()
                        .quartier(a.get("libelle", String.class))
                        .date(a.get("date", Date.class).toString())
                        .total(a.get("total", Long.class))
                        .build()
                ).toList();

    }

    public List<ArtisanFromMairieResponse> getArtisanByCommuneIdAndArtisanId(long idCommune, long artisanId){
        return artisanRepository.getArtisanByCommuneIdAndArtisanId(idCommune, artisanId).stream()
                .map(a -> ArtisanFromMairieResponse.builder()
                        .id(a.get("id", Long.class))
                        .nom(a.get("nomartisan", String.class))
                        .date((a.get("date", Date.class)).toString())//.toLocalDateTime().format(dateTimeFormatter))
                        .contact1(a.get("contact1", String.class))
                        .contact2(a.get("contact2", String.class))
                        .quartier(a.get("quartier_affaire", String.class))
                        .metier(a.get("job", String.class))
                        .longitude(a.get("lon", Double.class))
                        .latitude(a.get("lat", Double.class))
                        .build()
                ).toList();

    }

    // Afficher les STATS UTILISATEUR PAR CRM
    public List<ArtisanDailyCreatedUser> getUserStatsByCrm(long idCrm){
        return artisanRepository.getArtisanCreatedDailyByUserAndFromCrm(idCrm).stream()
                .map(a -> new ArtisanDailyCreatedUser(
                        a.get("id", Long.class),
                        a.get("agent", String.class),
                        a.get("tot_artisan", Long.class),
                        a.get("somme_recouvree", Long.class)
                        )
                ).toList();
    }

    @Transactional
    public List<EntitySearchResponse> getArtisanTrackedListFromAgentAssermente(HttpServletRequest httpServletRequest) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String userMail = outilService.getBackUserConnectedName(httpServletRequest);
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userMail.trim()).get();
        List<Artisan> artisans = artisanRepository.
                findAllByUtilisateurAgentAssermenteAndStatutPaiementIn(utilisateur, List.of(0, 1));
        return artisans.stream().map(
                a -> EntitySearchResponse.builder()
                        .id(a.getId())
                        .nom(a.getNom() + " " +a.getPrenom())
                        .contact(a.getContact1())
                        .datenaissance(a.getDateNaissance().format(dateTimeFormatter))
                        .metier(a.getMetier().getLibelle())
                        .paiement(a.getStatutPaiement())
                        .commune(a.getCommuneResidence().getLibelle())
                        .type("Artisans")
                        .image(getImageIfExists(a.getPhotoArtisan()))
                        .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
                        .quartier(a.getQuartierResidence()) // Quartier de l'ACTIVITE
                        .amende(a.getAmendes().size())
                        .latitude(processGeoData(a.getLatitude()))
                        .longitude(processGeoData(a.getLongitude()))
                        .montant(15000 - (paiementEnrolementRepository.findAllByArtisan(a).stream().mapToInt(
                                PaiementEnrolement::getMontant).sum()))
                        .build()
        ).toList();
    }

    private List<BeanMonthData> fillMonthForData(List<Tuple> donnee, int... choice){
        List<BeanMonthData> retour = new ArrayList<>();
        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9 , 10, 11, 12).forEach(
                mois -> {
                    Optional<BeanMonthData> optionalMois = donnee.stream().map(
                            tuple -> new BeanMonthData(
                                    (tuple.get("mois", BigDecimal.class)).intValue(),
                                    choice.length == 0 ?
                                            (tuple.get("tot", BigDecimal.class)).longValue() :
                                            tuple.get("tot", Long.class)
                            )
                    )
                    .filter(beanMonthData -> beanMonthData.mois() == mois)
                            .findFirst();
                    if(optionalMois.isEmpty()){
                        retour.add(
                                new BeanMonthData(mois, 0)
                        );
                    }
                    else {
                        retour.add(optionalMois.get());
                    }
                }
        );
        return retour;
    }

    // GLOBAL :
    public List<BeanMonthData> getGlobalTotalEnroleByMonth(){
        return fillMonthForData(artisanRepository.getGlobalTotalEnroleByMonth());
    }

    public List<BeanMonthData> getGlobalTotalPaymentByMonth(){
        return fillMonthForData(artisanRepository.getGlobalTotalPaymentByMonth(), 0);
    }

    // CRM :
    public List<BeanMonthData> getCrmTotalEnorleByMonth(long idCrm){
        return fillMonthForData(artisanRepository.getCrmTotalEnroleByMonth(idCrm));
    }

    public List<BeanMonthData> getCrmTotalPaymentByMonth(long idCrm){
        return fillMonthForData(artisanRepository.getCrmTotalPaymentByMonth(idCrm));
    }

    // CONTROLE :
    public List<BeanMonthData> getGlobalTotalRecouvrementByMonth(){
        return fillMonthForData(artisanRepository.getGlobalTotalRecouvrementByMonth());
    }
}
