package com.cnmci.stats.repository;

import com.cnmci.core.model.Parametres;
import com.cnmci.core.model.Partenaire;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartenaireRepository extends CrudRepository<Partenaire, Long> {
}
