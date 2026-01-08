package com.cnmci.stats.service;

import com.cnmci.core.model.*;
import com.cnmci.stats.beans.AmendeRequest;
import com.cnmci.stats.beans.MessageResponse;
import com.cnmci.stats.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionService {

    // ATTRIBUTES :
    private final AmendeRepository amendeRepository;
    private final ArtisanRepository artisanRepository;
    private final ApprentiRepository apprentiRepository;
    private final CompagnonRepository compagnonRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final OutilService outilService;



    // METHODS :
    private void selectAppropriateEntity(Amende amende, String type, long id){
        switch (type){
            case "ART":
                Artisan artisan = artisanRepository.findById(id).get();
                amende.setArtisan(artisan);
                break;

            case "ENT":
                Entreprise entreprise = entrepriseRepository.findById(id).get();
                amende.setEntreprise(entreprise);
                break;

            case "APP":
                Apprenti apprenti = apprentiRepository.findById(id).get();
                amende.setApprenti(apprenti);
                break;

            default:
                Compagnon compagnon = compagnonRepository.findById(id).get();
                amende.setCompagnon(compagnon);
                break;
        }
    }


    @Transactional
    public MessageResponse manageAmende(AmendeRequest data,
                                        HttpServletRequest httpServletRequest){
        Amende amende = amendeRepository.findById(data.getId()).orElseGet(
                () -> {
                    return Amende.builder()
                            .amendeSolde(false)
                            .build();
                }
        );
        amende.setMontant(data.getMontant());
        amende.setCommentaire(data.getCommentaire());
        selectAppropriateEntity(amende, data.getEntityType(), data.getEntityId());
        String userMail = outilService.getBackUserConnectedName(httpServletRequest);
        amende.setUtilisateur(utilisateurRepository.findByEmail(userMail).get());
        // Save :
        Amende newOrUpdate = amendeRepository.save(amende);

        log.info("Amende traitée");
        return MessageResponse.builder()
                .id(newOrUpdate.getId())
                .localDateTime(LocalDateTime.now())
                .message("Amende succès")
                .httpStatus(HttpStatus.OK.value())
                .build();
    }
}
