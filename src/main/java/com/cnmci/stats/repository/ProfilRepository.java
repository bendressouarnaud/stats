package com.cnmci.stats.repository;

import com.cnmci.core.model.Profil;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProfilRepository extends CrudRepository<Profil, Long> {
    List<Profil> findAllByOrderByLibelleAsc();
}
