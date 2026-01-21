package com.cnmci.stats.repository;

import com.cnmci.core.model.Artisan;
import com.cnmci.core.model.Utilisateur;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface ArtisanRepository extends CrudRepository<Artisan, Long> {
    List<Artisan> findAllByStatutKycAndStatutPaiement(int kyc, int paiement, Pageable pageable);
    List<Artisan> findAllByStatutKycOrStatutPaiement(int kyc, int paiement, Pageable pageable);
    List<Artisan> findAllByStatutPaiement(int paiement, Pageable pageable);
    List<Artisan> findAllByStatutKyc(int kyc, Pageable pageable);
    List<Artisan> findAllByStatutKycAndStatutPaiement(int kyc, int paiement);
    List<Artisan> findAllByUtilisateur(Utilisateur user);
    List<Artisan> findAllByOrderByNomAsc();
    List<Artisan> findAll(Pageable pageable);
    Artisan findByNumeroRegistre(String numero);
    List<Artisan> findByContact1(String contact);
    // Recherche par NUMERO ou EMAIL
    Artisan findByEmailIgnoreCaseOrContact1(String email, String contact);
    List<Artisan> findAllByNomIgnoreCaseContainingOrPrenomIgnoreCaseContainingOrContact1Containing(String nom,
                                                                                                   String prenom,
                                                                                                   String contact);

    @Query(value = "select e.label, e.id, count(a.id) as totentreprise, count(f.id) as totartisan from commune b " +
            "left join entreprise a  on b.id = a.commune_id left join artisan f on b.id = f.commune_residence_id " +
            "inner join sous_prefecture c on c.id = b.sous_prefecture_id inner join departement d on d.id = " +
            "c.departement_id inner join crm e on e.id = d.crm_id group by e.label, e.id",
            nativeQuery = true)
    List<Tuple> findAllArtisanEntrepriseByCrm();

    @Query(value = "select * from artisan a where (a.statut_kyc = 0 and a.statut_paiement in (0,1,2)) or " +
            "(a.statut_kyc = 1 and a.statut_paiement in (0,1))",
            nativeQuery = true)
    List<Artisan> findAllArtisanEnAttente(Pageable pageable);

    @Query(value = "select * from artisan a where a.statut_kyc = 1 and a.statut_paiement = 2",
            nativeQuery = true)
    List<Artisan> findAllArtisanValidate(Pageable pageable);

    @Query(value = "select * from artisan a where a.statut_kyc = :statutKyc and " +
            "a.statut_paiement = :statutPaiement and " +
            "date(a.created_at) between date(:dateDebut) AND date(:dateFin)",
            nativeQuery = true)
    List<Artisan> findAllArtisanFromReporting(int statutKyc, int statutPaiement,
                                              LocalDate dateDebut, LocalDate dateFin);


    // Return CONNECTED USER's DATA :
    @Query(value = "select * from (" +
            "select date(a.created_at),'artisan' entite, count(a.*) total from artisan a inner join utilisateur b on " +
            "b.id = a.utilisateur_id " +
            "where b.id = :userId and date(a.created_at) between date(:dateDebut) AND date(:dateFin) " +
            "group by date(a.created_at) " +
            "union " +
            "select date(a.created_at),'apprenti' entite, count(a.*) total from apprenti a inner join utilisateur b on " +
            "b.id = a.utilisateur_id " +
            "where b.id = :userId and date(a.created_at) between date(:dateDebut) AND date(:dateFin) " +
            "group by date(a.created_at) " +
            "union " +
            "select date(a.created_at),'compagnon' entite, count(a.*) total from compagnon a inner join utilisateur b on " +
            "b.id = a.utilisateur_id " +
            "where b.id = :userId and date(a.created_at) between date(:dateDebut) AND date(:dateFin) " +
            "group by date(a.created_at) " +
            "union " +
            "select date(a.created_at),'entreprise' entite, count(a.*) total from entreprise a inner join utilisateur b on " +
            "b.id = a.utilisateur_id " +
            "where b.id = :userId and date(a.created_at) between date(:dateDebut) AND date(:dateFin) " +
            "group by date(a.created_at) " +
            ") a order by date desc",
            nativeQuery = true)
    List<Tuple> findAllUserConnectedCreatedEntities(long userId, LocalDate dateDebut, LocalDate dateFin);

    // Return CONNECTED USER's DATA :
    @Query(value = "select * from (" +
            "select b.nom, b.prenom,date(a.created_at), 'artisan' entites, count(a.*) total from artisan a inner join utilisateur b on " +
            "b.id = a.utilisateur_id where date(a.created_at) between date(:dateDebut) AND date(:dateFin) " +
            "group by b.nom, b.prenom,date(a.created_at) " +
            "union " +
            "select b.nom, b.prenom,date(a.created_at), 'apprenti' entites, count(a.*) total from apprenti a inner join utilisateur b on " +
            "b.id = a.utilisateur_id where date(a.created_at) between date(:dateDebut) AND date(:dateFin) " +
            "group by b.nom, b.prenom,date(a.created_at) " +
            "union " +
            "select b.nom, b.prenom,date(a.created_at), 'compagnon' entites, count(a.*) total from compagnon a inner join utilisateur b on " +
            "b.id = a.utilisateur_id where date(a.created_at) between date(:dateDebut) AND date(:dateFin) " +
            "group by b.nom, b.prenom,date(a.created_at) " +
            "union " +
            "select b.nom, b.prenom,date(a.created_at), 'entreprise' entites, count(a.*) total from entreprise a inner join utilisateur b on " +
            "b.id = a.utilisateur_id where date(a.created_at) between date(:dateDebut) AND date(:dateFin) " +
            "group by b.nom, b.prenom,date(a.created_at) " +
            ") a  order by date asc",
            nativeQuery = true)
    List<Tuple> findAllUsersCreatedEntities(LocalDate dateDebut, LocalDate dateFin);

    //  REPARTITION METIER COMMUNE
    @Query(value = "select c.libelle, count(a.*) as total from artisan a inner join activite b on a.activite_id = b.id " +
            "inner join metier c on c.id = b.metier_principale_id where b.commune_id = :communeId group by c.libelle order by " +
            "count(a.*) desc limit 10",
            nativeQuery = true)
    List<Tuple> getMetierByCommune(long communeId);

    //  Population d'ARTISAN par COMMUNE de NAISSANCE pour une COMMUNE
    @Query(value = "select b.id, b.libelle, count(a.*) as total from artisan a inner join commune b on a.commune_naissance_id " +
            "= b.id inner join activite c on a.activite_id = c.id where c.commune_id = 498 group by b.id, b.libelle " +
            "order by count(a.*) desc limit 10",
            nativeQuery = true)
    List<Tuple> getArtisanFromBirthPlaceByCommune(long communeId);

    //  Repartition de paiement par secteur d’activité
    @Query(value = "select d.libelle, count(b.*) as total from artisan a inner join paiement_enrolement b " +
            "on a.id = b.artisan_id inner join activite c on a.activite_id = c.id inner join metier d on " +
            "d.id = c.metier_principale_id where c.commune_id = :communeId group by d.libelle order by " +
            "count(b.*) desc limit 10",
            nativeQuery = true)
    List<Tuple> getPaymentByActivite(long communeId);
}
