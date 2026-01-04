package com.cnmci.stats.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.cnmci.stats.controller.AccueilController.SECRET_KEY;


@Service
public class JwtUtil {

    private static final long serialVersionUID = -2550185165626007488L;
    //                                            jour  hr   mn   sc
    public static final long JWT_TOKEN_VALIDITY = 1 * 3 * 60 * 60;
    //private static final Key SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    //@Value("${jwt.secret}")
    private String secret="K8jemange";

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    //for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).build().parseSignedClaims(token.replace("\"","")).getPayload();
    }

    //check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //generate token for user
    public String generateToken(UserDetails userDetails, Date date) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails, date);
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, UserDetails userDetails, Date date) {
        /*return Jwts.builder().setClaims(claims).subject(subject).issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey()).compact();*/
        return Jwts.builder()
                //.claim("roles", claims)
                .claim("roles", userDetails.getAuthorities())
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                // JWT_TOKEN_VALIDITY * 1000 -> in milliseconds
                .expiration(date) //
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    private SecretKey getSigningKey() {
        return Jwts.SIG.HS512.key().build();
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        //return username.equals(userDetails.getUsername());
    }

}
