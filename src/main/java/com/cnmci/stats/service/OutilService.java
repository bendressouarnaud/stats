package com.cnmci.stats.service;

import com.cnmci.stats.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutilService {

    // A T T R I B U T E S
    private final JwtUtil jwtUtil;

    // M E T H O D S :
    public String getBackUserConnectedName(HttpServletRequest request){
        try {
            String requestTokenHeader = request.getHeader("Authorization");
            String token = null;
            token = requestTokenHeader.substring(7);
            return jwtUtil.getUsernameFromToken(token);
        }
        catch (Exception exc){
            log.error("Exception getBackUserConnectedName() {}", exc.toString());
            return null;
        }
    }
}
