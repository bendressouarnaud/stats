package com.cnmci.stats.repository;

import com.cnmci.core.model.Compagnon;
import com.cnmci.core.model.Utilisateur;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompagnonRepository extends CrudRepository<Compagnon, Long> {
    List<Compagnon> findAll(Pageable pageable);
    List<Compagnon> findAllByStatutKycAndStatutPaiement(int kyc, int paiement, Pageable pageable);
    List<Compagnon> findAllByStatutPaiement(int paiement, Pageable pageable);
    List<Compagnon> findAllByStatutKyc(int kyc, Pageable pageable);
    List<Compagnon> findAllByOrderByNomAsc();
    List<Compagnon> findAllByUtilisateur(Utilisateur user);
    Compagnon findByNumeroImmatriculation(String numero);
    Compagnon findByEmailIgnoreCaseOrContact1(String email, String contact);
    List<Compagnon> findByContact1(String contact);
    List<Compagnon> findAllByNomIgnoreCaseContainingOrPrenomIgnoreCaseContainingOrContact1Containing(String nom, String prenom, String contact);

    @Query(value = "select * from compagnon a where (a.statut_kyc = 0 and a.statut_paiement in (0,1,2)) or " +
            "(a.statut_kyc = 1 and a.statut_paiement in (0,1))",
            nativeQuery = true)
    List<Compagnon> findAllCompagnonEnAttente(Pageable pageable);

    @Query(value = "select * from compagnon a where a.statut_kyc = 1 and a.statut_paiement = 2",
            nativeQuery = true)
    List<Compagnon> findAllCompagnonValidate(Pageable pageable);

    @Query(value = "select * from compagnon a where a.statut_kyc = :statutKyc and " +
            "a.statut_paiement = :statutPaiement and " +
            "date(a.created_at) between date(:dateDebut) AND date(:dateFin)",
            nativeQuery = true)
    List<Compagnon> findAllCompagnonFromReporting(int statutKyc, int statutPaiement,
                                                LocalDate dateDebut, LocalDate dateFin);
}
