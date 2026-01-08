package com.cnmci.stats.repository;

import com.cnmci.core.model.*;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AmendeRepository extends CrudRepository<Amende, Long> {
    List<Amende> findAllByArtisan(Artisan data);
    List<Amende> findAllByApprenti(Apprenti data);
    List<Amende> findAllByCompagnon(Compagnon data);
    List<Amende> findAllByEntreprise(Entreprise data);
    List<Amende> findAllByUtilisateur(Utilisateur data);
    //
    List<Amende> findAllByOrderByMontantAsc();
}
