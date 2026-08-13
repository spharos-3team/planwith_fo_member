package com.planwith.planwith_fo_member.adapter.in.web;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_member.adapter.out.security.JwtRsaKeyProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "jwks", description = "JWT public keys")
public class JwksController {

	private final JwtRsaKeyProvider keyProvider;

	public JwksController(JwtRsaKeyProvider keyProvider) {
		this.keyProvider = keyProvider;
	}

	@GetMapping(value = "/oauth2/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "JWKS 공개키 조회")
	public ResponseEntity<Map<String, Object>> jwks() {
		return ResponseEntity.ok(keyProvider.publicJwkSet().toJSONObject());
	}
}
