package com.example.bank.proj.sharedfolder.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JWTUtil {

    private static final String SECRET_KEY = "my-super-secret-key@random2442!!"; // keep in config/env for prod

    private Key getSigningKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateToken(String username) {
        long expirationMillis = System.currentTimeMillis() + 1000 * 60 * 10; // 10 minutes
        Date issuedAt = new Date();
        Date expiration = new Date(expirationMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUserName(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails, String username) {
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private Claims extractClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
       Date exp = extractClaims(token).getExpiration();
       return exp == null ? true : exp.before(new Date());
    }
}
// ```// filepath: c:\Users\rahul\OneDrive\Desktop\coding\Notes\SB\proj\src\main\java\com\example\bank\proj\sharedfolder\security\JWTUtil.java
// package com.example.bank.proj.sharedfolder.security;

// import java.util.Date;
// import java.nio.charset.StandardCharsets;
// import java.security.Key;

// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import javax.crypto.spec.SecretKeySpec;

// @Component
// public class JWTUtil {

//     private static final String SECRET_KEY = "my-super-secret-key@random2442!!"; // keep in config/env for prod

//     private Key getSigningKey() {
//         return new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//     }

//     public String generateToken(String username) {
//         long expirationMillis = System.currentTimeMillis() + 1000 * 60 * 10; // 10 minutes
//         Date issuedAt = new Date();
//         Date expiration = new Date(expirationMillis);

//         return Jwts.builder()
//                 .setSubject(username)
//                 .setIssuedAt(issuedAt)
//                 .setExpiration(expiration)
//                 .signWith(getSigningKey())
//                 .compact();
//     }

//     public String extractUserName(String token) {
//         return extractClaims(token).getSubject();
//     }

//     public boolean validateToken(String token, UserDetails userDetails, String username) {
//         return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
//     }

//     private Claims extractClaims(String token){
//         return Jwts.parserBuilder()
//                 .setSigningKey(getSigningKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }

//     private boolean isTokenExpired(String token) {
//        Date exp = extractClaims(token).getExpiration();
//        return exp == null ? true : exp.before(new Date());
//     }
// }