package com.cnmci.stats.repository;

import com.cnmci.core.model.Apprenti;
import com.cnmci.core.model.Utilisateur;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface ApprentiRepository extends CrudRepository<Apprenti, Long> {
    List<Apprenti> findAllByStatutKycAndStatutPaiement(int kyc, int paiement, Pageable pageable);
    List<Apprenti> findAllByStatutKycOrStatutPaiement(int kyc, int paiement, Pageable pageable);
    List<Apprenti> findAllByStatutPaiement(int paiement, Pageable pageable);
    List<Apprenti> findAllByStatutKyc(int kyc, Pageable pageable);
    List<Apprenti> findAll(Pageable pageable);
    List<Apprenti> findAllByUtilisateur(Utilisateur user);
    List<Apprenti> findAllByOrderByNomAsc();
    Apprenti findByNumeroImmatriculation(String numero);
    Apprenti findByEmailIgnoreCaseOrContact1(String email, String contact);
    List<Apprenti> findAllByNomIgnoreCaseContainingOrPrenomIgnoreCaseContainingOrContact1Containing(String nom, String prenom, String contact);

    @Query(value = "select * from apprenti a where (a.statut_kyc = 0 and a.statut_paiement in (0,1,2)) or " +
            "(a.statut_kyc = 1 and a.statut_paiement in (0,1))",
            nativeQuery = true)
    List<Apprenti> findAllApprentiEnAttente(Pageable pageable);

    @Query(value = "select * from apprenti a where a.statut_kyc = 1 and a.statut_paiement = 2",
            nativeQuery = true)
    List<Apprenti> findAllApprentiValidate(Pageable pageable);

    @Query(value = "select * from apprenti a where a.statut_kyc = :statutKyc and " +
            "a.statut_paiement = :statutPaiement and " +
            "date(a.created_at) between date(:dateDebut) AND date(:dateFin)",
            nativeQuery = true)
    List<Apprenti> findAllApprentiFromReporting(int statutKyc, int statutPaiement,
                                              LocalDate dateDebut, LocalDate dateFin);
}
