package com.maheer9272.LedgerCore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiry}")
    private Long expiry;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        //Extracting the list of Authorities
        List<String> authorities =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

        //Claims means payload
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(
                        now.plusSeconds(expiry)
                )
                .subject(
                        authentication.getName()
                )
                .claim(
                        "authorities",
                        authorities
                )
                .build();

        Jwt jwt = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        );

        return jwt.getTokenValue();
    }
}