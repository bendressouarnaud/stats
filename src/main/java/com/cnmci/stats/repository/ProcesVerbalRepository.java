package com.cnmci.stats.repository;

import com.cnmci.core.model.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcesVerbalRepository extends CrudRepository<ProcesVerbal, Long> {
    List<ProcesVerbal> findAllByUtilisateur(Utilisateur utilisateur);
    List<ProcesVerbal> findAllByArtisan(Artisan artisan);
    List<ProcesVerbal> findAllByArtisanAndActif(Artisan artisan, boolean actif);
    List<ProcesVerbal> findAllByApprenti(Apprenti apprenti);
    List<ProcesVerbal> findAllByApprentiAndActif(Apprenti apprenti, boolean actif);
    List<ProcesVerbal> findAllByCompagnon(Compagnon compagnon);
    List<ProcesVerbal> findAllByCompagnonAndActif(Compagnon compagnon, boolean actif);
    List<ProcesVerbal> findAllByEntreprise(Entreprise entreprise);
    List<ProcesVerbal> findAllByEntrepriseAndActif(Entreprise entreprise, boolean actif);
    ProcesVerbal findByNumeroPvAndCodeValidation(String numeroPv, String codeValidation);

    @Query(value = "select * from proces_verbal a where date(a.date_reglement) = date(now()) + 1 and actif = true",
            nativeQuery = true)
    List<ProcesVerbal> findAllToSendReminder();
}
