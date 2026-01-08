package com.cnmci.stats.service;

import com.cnmci.core.model.*;
import com.cnmci.stats.beans.EntitySearchResponse;
import com.cnmci.stats.beans.StatsBean;
import com.cnmci.stats.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public List<StatsBean> getEntitiesStatistiques(){
        List<StatsBean> retour = new ArrayList<>();
        // ARTISAN :
        List<Artisan> artisans = artisanRepository.findAllByOrderByNomAsc();
        int populationArtisan = artisans.size();
        long attenduArtisan = 15000L * populationArtisan;
        // En cours
        long encoursArtisan = paiementEnrolementRepository.findAllByArtisanIn(
                artisans.stream().filter(
                        a -> a.getStatutPaiement() == 2
                ).toList()
            ).stream().mapToInt(
                PaiementEnrolement::getMontant
            ).sum();
        double pourcentageArtisan = (double) (encoursArtisan * 100) / attenduArtisan;
        retour.add(new StatsBean("Artisans", populationArtisan, attenduArtisan, encoursArtisan, roundValue(pourcentageArtisan)));

        // APPRENTI :
        List<Apprenti> apprentis = apprentiRepository.findAllByOrderByNomAsc();
        int populationApprenti = apprentis.size();
        long attenduApprenti = 5000L * populationApprenti;
        // En cours
        long encoursApprenti = paiementEnrolementRepository.findAllByApprentiIn(
                apprentis.stream().filter(
                        a -> a.getStatutPaiement() == 2
                ).toList()
        ).stream().mapToInt(
                PaiementEnrolement::getMontant
        ).sum();
        double pourcentageApprenti = (double) (encoursApprenti * 100) / attenduApprenti;
        retour.add(new StatsBean("Apprentis", populationApprenti, attenduApprenti, encoursApprenti, roundValue(pourcentageApprenti)));

        // C O M P A G N O N :
        List<Compagnon> compagnons = compagnonRepository.findAllByOrderByNomAsc();
        int populationCompagnon = compagnons.size();
        long attenduCompagnon = 5000L * populationCompagnon;
        // En cours
        long encoursCompagnon = paiementEnrolementRepository.findAllByCompagnonIn(
                compagnons.stream().filter(
                        a -> a.getStatutPaiement() == 2
                ).toList()
        ).stream().mapToInt(
                PaiementEnrolement::getMontant
        ).sum();
        double pourcentageCompagnon = (double) (encoursCompagnon * 100) / attenduCompagnon;
        retour.add(new StatsBean("Compagnons", populationCompagnon, attenduCompagnon, encoursCompagnon, roundValue(pourcentageCompagnon)));

        // E N T R E P R I S E :
        List<Entreprise> entreprises = entrepriseRepository.findAllByOrderByRaisonSocialeAsc();
        int populationEntreprise = entreprises.size();
        long attenduEntreprise = 5000L * populationEntreprise;
        // En cours
        long encoursEntreprise = paiementEnrolementRepository.findAllByEntrepriseIn(
                entreprises.stream().filter(
                        a -> a.getStatutPaiement() == 2
                ).toList()
        ).stream().mapToInt(
                PaiementEnrolement::getMontant
        ).sum();
        double pourcentageEntreprise = (double) (encoursEntreprise * 100) / attenduEntreprise;
        retour.add(new StatsBean("Entreprises", populationEntreprise, attenduEntreprise, encoursEntreprise, roundValue(pourcentageEntreprise)));

        return retour;
    }

    public List<EntitySearchResponse> lookForAnyEntity(String search){
        List<EntitySearchResponse> retour = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // ARTISAN
        List<Artisan> artisans = artisanRepository.
                findAllByNomIgnoreCaseContainingOrPrenomIgnoreCaseContainingOrContact1Containing(search.trim(),
                        search.trim(), search.trim());
        retour.addAll(artisans.stream().map(
        a -> EntitySearchResponse.builder()
            .id(a.getId())
            .nom(a.getNom() + " " +a.getPrenom())
            .contact(a.getContact1())
            .datenaissance(a.getDateNaissance().format(dateTimeFormatter))
            .metier(a.getActivite().getMetierPrincipale().getLibelle())
            .paiement(a.getStatutPaiement())
            .commune(a.getCommuneResidence().getLibelle())
            .type("Artisans")
            .image(getImageIfExists(a.getPhotoArtisan()))
            .datenrolement(a.getCreatedAt().format(dateTimeFormatter))
            .quartier(a.getQuartierResidence())
            .amende(a.getAmendes().size())
            .latitude(processGeoData(a.getLatitude()))
            .longitude(processGeoData(a.getLongitude()))
            .build()
        ).toList());

        // ApPRENTI
        List<Apprenti> apprentis = apprentiRepository.
                findAllByNomIgnoreCaseContainingOrPrenomIgnoreCaseContainingOrContact1Containing(search.trim(),
                        search.trim(), search.trim());
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
                        .quartier(a.getQuartierResidence())
                        .amende(a.getAmendes().size())
                        .latitude(processGeoData(a.getLatitude()))
                        .longitude(processGeoData(a.getLongitude()))
                        .build()
        ).toList());

        // COMPAGNON
        List<Compagnon> compagnons = compagnonRepository.
                findAllByNomIgnoreCaseContainingOrPrenomIgnoreCaseContainingOrContact1Containing(search.trim(),
                        search.trim(), search.trim());
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
                        .build()
        ).toList());

        // ENTREPRISES
        List<Entreprise> entreprises = entrepriseRepository.
                findAllByRaisonSocialeIgnoreCaseContainingOrContactContaining(search.trim(), search.trim());
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
                                .quartier(a.getQuartier())
                                .amende(a.getAmendes().size())
                                .latitude(processGeoData(a.getLatitude()))
                                .longitude(processGeoData(a.getLongitude()))
                                .build()
                ).toList());
        return retour;
    }
}
