package com.cnmci.stats.repository;

import com.cnmci.core.model.Apprenti;
import com.cnmci.core.model.Artisan;
import com.cnmci.core.model.Crm;
import com.cnmci.core.model.Utilisateur;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ArtisanRepository extends CrudRepository<Artisan, Long> {
    List<Artisan> findAllByStatutKycAndStatutPaiement(int kyc, int paiement, Pageable pageable);
    List<Artisan> findAllByStatutKycOrStatutPaiement(int kyc, int paiement, Pageable pageable);
    List<Artisan> findAllByStatutPaiement(int paiement, Pageable pageable);
    List<Artisan> findAllByStatutKyc(int kyc, Pageable pageable);
    List<Artisan> findAllByStatutKycAndStatutPaiement(int kyc, int paiement);
    List<Artisan> findAllByUtilisateur(Utilisateur user);
    List<Artisan> findAllByOrderByNomAsc();
    List<Artisan> findAllByCrm(Crm crm);
    List<Artisan> findAll(Pageable pageable);
    Artisan findByNumeroRegistre(String numero);
    List<Artisan> findByContact1(String contact);
    Optional<Artisan> findByIdAndStatutPaiement(long id, int statutPaiement);
    // Recherche par NUMERO ou EMAIL
    Artisan findByEmailIgnoreCaseOrContact1(String email, String contact);
    List<Artisan> findAllByNomIgnoreCaseContainingOrPrenomIgnoreCaseContainingOrContact1Containing(String nom,
                                                                                                   String prenom,
                                                                                                   String contact);

    @Query(value = "select * from artisan where concat(lower(nom),' ', lower(prenom)) like %:wordToSearch% or " +
            "contact1 like %:wordToSearch%",
            nativeQuery = true)
    List<Artisan> findAllArtisanByNomPrenomOrContact(String wordToSearch);

    List<Artisan> findAllByUtilisateurAgentAssermenteAndStatutPaiementIn(Utilisateur agent, List<Integer> statut);

    @Query(value = "select e.label, e.id, count(a.id) as totentreprise, count(f.id) as totartisan from commune b " +
            "left join entreprise a  on b.id = a.commune_id left join artisan f on b.id = f.commune_residence_id " +
            "inner join sous_prefecture c on c.id = b.sous_prefecture_id inner join departement d on d.id = " +
            "c.departement_id inner join crm e on e.id = d.crm_id group by e.label, e.id",
            nativeQuery = true)
    List<Tuple> findAllArtisanEntrepriseByCrm();

    @Query(value = "select * from (" +
            "select b.libelle ville, c.libelle, date(a.created_at), count(a.id) total from activite a inner join commune b on b.id = a.commune_id " +
            "inner join quartier c on c.id = a.quartier_siege_id where b.id = :idCommune " +
            "group by b.libelle, c.libelle, date(a.created_at) " +
            "union all " +
            "select b.libelle ville, c.libelle, date(a.created_at), count(a.id) total from entreprise a inner join commune b on a.commune_id = b.id " +
            "inner join quartier c on c.id = a.quartier_siege_id where b.id = :idCommune " +
            "group by b.libelle, c.libelle, date(a.created_at)" +
            ") a order by date desc",
            nativeQuery = true)
    List<Tuple> findAllEntitiesFromTown(long idCommune);

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

    @Query(value = "select distinct a.* from artisan a inner join activite b on a.activite_id = b.id " +
            "inner join quartier c on c.id = b.quartier_siege_id " +
            "where c.id = :quartierId and a.statut_paiement = :statutPaiement and " +
            "date(a.created_at) between date(:dateDebut) AND date(:dateFin)",
            nativeQuery = true)
    List<Artisan> findAllArtisanFromAgentAsserment(long quartierId, int statutPaiement,
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


    // Données de la CRM :
    @Query(value = "select count(*) as tot from artisan where crm_id = :crmId and " +
            "statut_type = :statutType",
            nativeQuery = true)
    Tuple findTotalArtisanFromCrm(long crmId, int statutType);
    // Global
    @Query(value = "select count(*) as tot from artisan where statut_type = :statutType",
            nativeQuery = true)
    Tuple findTotalArtisan(int statutType);


    // those who have not paid yet or have paid a part :
    List<Artisan> findAllByRappelSmsAndStatutPaiementIn(int rappelSms, List<Integer> statutPaiement);

    @Query(value = "select distinct a.* from artisan a inner join activite b on a.activite_id = b.id " +
            "where b.quartier_siege_id = :quartierSiegeId and a.statut_paiement in (0,1) and " +
            "a.utilisateur_agent_assermente_id is null",
            nativeQuery = true)
    List<Artisan> findAllByQuartierSiege(long quartierSiegeId);


    @Query(value = "select a.id,concat(a.nom,' ',a.prenom) nomartisan,date(a.date_naissance), contact1, contact2, " +
            "c.libelle quartier_affaire, d.libelle as job, case when longitude is not null then longitude " +
            "else 0 end as lon, " +
            "case when latitude is not null then latitude " +
            "else 0 end as lat from artisan a inner join activite b " +
            "on a.activite_id = b.id inner join quartier c on c.id = b.quartier_siege_id inner join metier d on d.id = " +
            "a.metier_id where b.commune_id = :communeId and a.id > :artisanId",
            nativeQuery = true)
    List<Tuple> getArtisanByCommuneIdAndArtisanId(long communeId, long artisanId);

    @Query(value = "select b.label, count(a.id) as tot from artisan a inner join crm b on a.crm_id = b.id " +
            "where a.statut_paiement in (0,1) group by b.label",
            nativeQuery = true)
    List<Tuple> getArtisanByCrmNotSoldOutYet();

    @Query(value = "select a.id, concat(a.nom,' ',a.prenom) as agent_assermentes," +
            "concat(b.nom,' ',b.prenom) as artisans," +
            "case when sum(c.montant) is not null then sum(c.montant) " +
            "else 0 end as somme_encaisse " +
            "from utilisateur a inner join artisan b on (a.id = b.utilisateur_agent_assermente_id " +
            "and date(date_assignation_assermente) = date(now())) " +
            "left join paiement_enrolement c on c.artisan_id = b.id " +
            "where a.id in (select distinct utilisateur_id from action_terrain where sent = false) " +
            "group by a.id, concat(a.nom,' ',a.prenom), concat(b.nom,' ',b.prenom) " +
            "union " +
            "select b.id, concat(b.nom,' ',b.prenom) agent_assermentes," +
            "concat(c.nom,' ',c.prenom) as artisans, a.montant as somme_encaisse " +
            "from paiement_enrolement a inner join utilisateur b on " +
            "(a.utilisateur_id = b.id and date(a.created_at) = date(now())) " +
            "inner join artisan c on c.id = a.artisan_id " +
            "where b.id in (select distinct utilisateur_id from action_terrain d)",
            nativeQuery = true)
    List<Tuple> getArtisanListAssignedToAgentAssermente();

    @Query(value = "select a.id,concat(a.nom,' ',a.prenom) as agent, count(b.id) tot_artisan," +
            "case when sum(c.montant) is not null then sum(c.montant) else 0 end as somme_recouvree " +
            "from utilisateur a left join artisan b on (a.id = b.utilisateur_id and date(b.created_at) = date(now())) " +
            "left join paiement_enrolement c on (b.id = c.artisan_id and date(c.created_at) = date(now())) " +
            "where a.crm_id = :idCRm and a.actif = true group by a.id,concat(a.nom,' ',a.prenom)",
            nativeQuery = true)
    List<Tuple> getArtisanCreatedDailyByUserAndFromCrm(long idCRm);

    // GLOBAL
    @Query(value = "select mois, sum(total) as tot from (" +
            "select EXTRACT(month FROM a.created_at) mois, count(id) total from artisan a " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, count(id) total from apprenti a " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, count(id) total from compagnon a " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, count(id) total from entreprise a " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "group by EXTRACT(month FROM a.created_at) " +
            ") a group by mois order by mois asc",
            nativeQuery = true)
    List<Tuple> getGlobalTotalEnroleByMonth();

    @Query(value = "select EXTRACT(month FROM a.created_at) mois, sum(montant) tot from paiement_enrolement a " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) group by EXTRACT(month FROM a.created_at)",
            nativeQuery = true)
    List<Tuple> getGlobalTotalPaymentByMonth();

    // BY CRM
    @Query(value = "select mois, sum(total) as tot from (" +
            "select EXTRACT(month FROM a.created_at) mois, count(id) total from artisan a " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "and a.crm_id = :idCrm " +
            "group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, count(a.id) total from apprenti a " +
            "inner join artisan_apprenti b on a.id = b.apprenti_id " +
            "inner join artisan c on c.id = b.artisan_id " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "and c.crm_id = :idCrm " +
            "group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, count(a.id) total from apprenti a " +
            "inner join entreprise_apprenti b on a.id = b.apprenti_id " +
            "inner join entreprise c on c.id = b.entreprise_id " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "and c.crm_id = :idCrm " +
            "group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, count(a.id) total from compagnon a " +
            "inner join artisan_compagnon b on a.id = b.compagnon_id " +
            "inner join artisan c on c.id = b.artisan_id " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "and c.crm_id = :idCrm " +
            "group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, count(a.id) total from compagnon a " +
            "inner join entreprise_compagnon b on a.id = b.compagnon_id " +
            "inner join entreprise c on c.id = b.entreprise_id " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "and c.crm_id = :idCrm " +
            "group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, count(id) total from entreprise a " +
            "where EXTRACT(YEAR FROM a.created_at) = EXTRACT(YEAR FROM now()) " +
            "and a.crm_id = :idCrm " +
            "group by EXTRACT(month FROM a.created_at) " +
            ") a group by mois " +
            "order by mois asc",
            nativeQuery = true)
    List<Tuple> getCrmTotalEnroleByMonth(long idCrm);

    @Query(value = "select mois, sum(tot) as tot from (" +
            "select EXTRACT(month FROM a.created_at) mois, sum(montant) tot from paiement_enrolement a " +
            "inner join artisan b on a.artisan_id = b.id where EXTRACT(YEAR FROM a.created_at) = " +
            "EXTRACT(YEAR FROM now()) and b.crm_id = :idCrm group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, sum(montant) tot from paiement_enrolement a " +
            "inner join apprenti b on a.apprenti_id = b.id " +
            "inner join artisan_apprenti c on c.apprenti_id = b.id " +
            "inner join artisan d on d.id = c.artisan_id " +
            "where EXTRACT(YEAR FROM a.created_at) = " +
            "EXTRACT(YEAR FROM now()) and d.crm_id = :idCrm group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, sum(montant) tot from paiement_enrolement a " +
            "inner join apprenti b on a.apprenti_id = b.id " +
            "inner join entreprise_apprenti c on c.apprenti_id = b.id " +
            "inner join entreprise d on d.id = c.entreprise_id " +
            "where EXTRACT(YEAR FROM a.created_at) = " +
            "EXTRACT(YEAR FROM now()) and d.crm_id = :idCrm group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, sum(montant) tot from paiement_enrolement a " +
            "inner join compagnon b on a.compagnon_id = b.id " +
            "inner join artisan_compagnon c on c.compagnon_id = b.id " +
            "inner join artisan d on d.id = c.artisan_id " +
            "where EXTRACT(YEAR FROM a.created_at) = " +
            "EXTRACT(YEAR FROM now()) and d.crm_id = :idCrm group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, sum(montant) tot from paiement_enrolement a " +
            "inner join compagnon b on a.compagnon_id = b.id " +
            "inner join entreprise_compagnon c on c.compagnon_id = b.id " +
            "inner join entreprise d on d.id = c.entreprise_id " +
            "where EXTRACT(YEAR FROM a.created_at) = " +
            "EXTRACT(YEAR FROM now()) and d.crm_id = :idCrm group by EXTRACT(month FROM a.created_at) " +
            "union all " +
            "select EXTRACT(month FROM a.created_at) mois, sum(montant) tot from paiement_enrolement a " +
            "inner join entreprise b on a.entreprise_id = b.id where EXTRACT(YEAR FROM a.created_at) = " +
            "EXTRACT(YEAR FROM now()) and b.crm_id = :idCrm group by EXTRACT(month FROM a.created_at) " +
            ") a group by mois " +
            "order by mois asc",
            nativeQuery = true)
    List<Tuple> getCrmTotalPaymentByMonth(long idCrm);
}
