package com.planwith.planwith_fo_member.adapter.out.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.planwith.planwith_fo_member.application.port.out.AccessTokenIssuerPort;
import com.planwith.planwith_fo_member.config.JwtProperties;

@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuerPort {

	private static final List<String> DEFAULT_ROLES = List.of("USER");
	private static final List<String> DEFAULT_SCOPES = List.of("profile:read");

	private final JwtProperties properties;
	private final JwtEncoder jwtEncoder;

	public JwtAccessTokenIssuer(JwtProperties properties, JwtRsaKeyProvider keyProvider) {
		this.properties = properties;
		this.jwtEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new com.nimbusds.jose.jwk.JWKSet(keyProvider.rsaKey())));
	}

	@Override
	public IssuedAccessToken issue(UUID memberUuid, String sessionId) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.accessTokenTtl());
		String jti = UUID.randomUUID().toString();

		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
				.keyId(keyProviderKeyId())
				.build();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.issuer())
				.subject(memberUuid.toString())
				.audience(List.of(properties.audience()))
				.issuedAt(now)
				.notBefore(now)
				.expiresAt(expiresAt)
				.id(jti)
				.claim("roles", DEFAULT_ROLES)
				.claim("scope", String.join(" ", DEFAULT_SCOPES))
				.claim("session_id", sessionId)
				.build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		long expiresIn = Math.max(1L, properties.accessTokenTtl().toSeconds());
		return new IssuedAccessToken(token, expiresIn, sessionId, DEFAULT_ROLES, DEFAULT_SCOPES);
	}

	private String keyProviderKeyId() {
		return properties.keyId() == null || properties.keyId().isBlank()
				? "planwith-member-local"
				: properties.keyId();
	}
}
