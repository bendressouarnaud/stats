package com.cnmci.stats.repository;

import com.cnmci.core.model.ActionTerrain;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ActionTerrainRepository extends CrudRepository<ActionTerrain, Long> {
    List<ActionTerrain> findAllByActifAndSent(boolean actif, boolean sent);
}
