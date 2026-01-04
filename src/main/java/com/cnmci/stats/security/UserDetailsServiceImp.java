package com.cnmci.stats.security;

import com.cnmci.core.model.Profil;
import com.cnmci.core.model.Utilisateur;
import com.cnmci.stats.repository.ProfilRepository;
import com.cnmci.stats.repository.UtilisateurRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImp implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    @PersistenceUnit
    EntityManagerFactory emf;
    List<Profil> profils = new ArrayList<>();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        if(profils.isEmpty()){
            profils = profilRepository.findAllByOrderByLibelleAsc();
        }
        //
        User.UserBuilder builder = null;
        if (utilisateur != null) {
            builder = User.withUsername(username);
            builder.password(utilisateur.getPwd());
            builder.roles(utilisateur.getProfil().getLibelle());
        } else {
            throw new UsernameNotFoundException("User not found.");
        }
        return builder.build();
    }
}
