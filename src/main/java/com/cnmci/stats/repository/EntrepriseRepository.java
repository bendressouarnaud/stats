package com.cnmci.stats.repository;

import com.cnmci.core.model.Entreprise;
import com.cnmci.core.model.Utilisateur;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface EntrepriseRepository extends CrudRepository<Entreprise, Long> {
    List<Entreprise> findAllByStatutKycOrStatutPaiement(int kyc, int paiement, Pageable pageable);
    List<Entreprise> findAllByStatutPaiement(int paiement, Pageable pageable);
    List<Entreprise> findAllByStatutKyc(int kyc, Pageable pageable);
    List<Entreprise> findAllByOrderByRaisonSocialeAsc();
    List<Entreprise> findAllByUtilisateur(Utilisateur user);
    Entreprise findByNumeroRea(String numero);
    Entreprise findByContact(String contact);
    List<Entreprise> findAllByRaisonSocialeIgnoreCaseOrContact(String libelle, String contact);
    List<Entreprise> findAllByRaisonSocialeIgnoreCaseContainingOrContact(String libelle, String contact);

    @Query(value = "select * from entreprise a where (a.statut_kyc = 0 and a.statut_paiement in (0,1,2)) or " +
            "(a.statut_kyc = 1 and a.statut_paiement in (0,1))",
            nativeQuery = true)
    List<Entreprise> findAllEntrepriseEnAttente(Pageable pageable);

    @Query(value = "select * from entreprise a where a.statut_kyc = 1 and a.statut_paiement = 2",
            nativeQuery = true)
    List<Entreprise> findAllEntrepriseValidate(Pageable pageable);

    @Query(value = "select * from entreprise a where a.statut_kyc = :statutKyc and " +
            "a.statut_paiement = :statutPaiement and " +
            "date(a.created_at) between date(:dateDebut) AND date(:dateFin)",
            nativeQuery = true)
    List<Entreprise> findAllEntrepriseFromReporting(int statutKyc, int statutPaiement,
                                                  LocalDate dateDebut, LocalDate dateFin);
}
