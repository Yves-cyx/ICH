package com.yingjian.api.security;

import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final JwtEncoder encoder;

    public TokenService(JwtEncoder encoder) { this.encoder = encoder; }

    public String issue(long memberId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(SecurityConfig.ISSUER)
                .subject(Long.toString(memberId)).issuedAt(now).expiresAt(now.plusSeconds(43200))
                .claim("scope", "member").build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
