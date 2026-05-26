package com.cnmci.stats.repository;

import com.cnmci.core.model.Commune;
import com.cnmci.core.model.Quartier;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuartierRepository extends CrudRepository<Quartier, Long> {
}
