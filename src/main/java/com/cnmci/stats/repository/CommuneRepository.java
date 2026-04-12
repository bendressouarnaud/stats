package com.cnmci.stats.repository;

import com.cnmci.core.model.Commune;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommuneRepository extends CrudRepository<Commune, Long> {
    List<Commune> findAllByOrderByLibelleAsc();
}
