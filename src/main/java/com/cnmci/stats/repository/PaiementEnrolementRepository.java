package com.cnmci.stats.repository;
import com.cnmci.core.model.*;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PaiementEnrolementRepository extends CrudRepository<PaiementEnrolement, Long> {
    PaiementEnrolement findByCompagnon(Compagnon compagnon);
    PaiementEnrolement findByApprenti(Apprenti apprenti);
    PaiementEnrolement findByArtisan(Artisan artisan);
    PaiementEnrolement findByEntreprise(Entreprise entreprise);

    List<PaiementEnrolement> findAllByCompagnon(Compagnon compagnon);
    List<PaiementEnrolement> findAllByCompagnonIn(List<Compagnon> compagnon);
    List<PaiementEnrolement> findAllByApprenti(Apprenti apprenti);
    List<PaiementEnrolement> findAllByApprentiIn(List<Apprenti> apprenti);
    List<PaiementEnrolement> findAllByArtisan(Artisan artisan);
    List<PaiementEnrolement> findAllByArtisanIn(List<Artisan> artisan);
    List<PaiementEnrolement> findAllByEntreprise(Entreprise entreprise);
    List<PaiementEnrolement> findAllByEntrepriseIn(List<Entreprise> entreprise);

    List<PaiementEnrolement> findAllByOrderByMontantAsc();

    @Query(value = "select a.denomination, e.libelle as prestation, d.valeur, f.libelle as remuneration, " +
        "sum(c.montant) total_montant from " +
        "partenaire a inner join utilisateur b on a.id = b.partenaire_id inner join paiement_enrolement c ON " +
        "c.utilisateur_id = b.id inner join partenaire_prestation d on d.partenaire_id = a.id " +
        "inner join prestation e on (e.id = d.prestation_id and e.id = 1) " +
        "inner join remuneration f on f.id = d.remuneration_id " +
        "group by a.denomination, e.libelle, d.valeur, f.libelle",
        nativeQuery = true)
    List<Tuple> findAllEnrolementPayment();

    @Query(value = "select date(a.created_at) dte, sum(montant) total from paiement_enrolement a " +
            "group by date(a.created_at) order by date(a.created_at) desc limit 7",
            nativeQuery = true)
    List<Tuple> findLatestDailyPayments();
}
