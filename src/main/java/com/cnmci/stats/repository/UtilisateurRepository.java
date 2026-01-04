package com.cnmci.stats.repository;

import com.cnmci.core.model.Utilisateur;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends CrudRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
}
