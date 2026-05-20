package com.cnmci.stats.repository;

import com.cnmci.core.model.Apprenti;
import com.cnmci.core.model.Profil;
import com.cnmci.core.model.Utilisateur;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends CrudRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findAllByProfil(Profil profil);

    @Query(value = "select * from (select a.id, a.email,count(b.id) as tot from utilisateur a left join " +
            "notification_controle b on (a.id = b.utilisateur_id and date(b.created_at) = date(now())) " +
            "where a.profil_id = 11 group by a.id, a.email) a order by tot asc",
            nativeQuery = true)
    List<Tuple> findAllUtilisateurWithNotificationRappel();
}
