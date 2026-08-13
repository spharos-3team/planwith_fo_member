package com.planwith.planwith_fo_member.adapter.out.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.planwith.planwith_fo_member.config.JwtProperties;

@Component
public class JwtRsaKeyProvider {

	private static final Logger log = LoggerFactory.getLogger(JwtRsaKeyProvider.class);

	private final RSAKey rsaKey;

	public JwtRsaKeyProvider(JwtProperties properties) {
		this.rsaKey = loadOrGenerate(properties);
	}

	public RSAKey rsaKey() {
		return rsaKey;
	}

	public JWKSet publicJwkSet() {
		return new JWKSet(rsaKey.toPublicJWK());
	}

	private RSAKey loadOrGenerate(JwtProperties properties) {
		String keyId = StringUtils.hasText(properties.keyId()) ? properties.keyId() : "planwith-member-local";
		if (StringUtils.hasText(properties.privateKeyPath()) && StringUtils.hasText(properties.publicKeyPath())) {
			try {
				RSAPrivateKey privateKey = readPrivateKey(Path.of(properties.privateKeyPath()));
				RSAPublicKey publicKey = readPublicKey(Path.of(properties.publicKeyPath()));
				return new RSAKey.Builder(publicKey)
						.privateKey(privateKey)
						.keyID(keyId)
						.build();
			}
			catch (Exception exception) {
				throw new IllegalStateException("JWT RSA 키 파일을 읽을 수 없습니다.", exception);
			}
		}

		log.warn("JWT_PRIVATE_KEY_PATH/JWT_PUBLIC_KEY_PATH 미설정 — 로컬용 RSA 키를 생성합니다. 운영에서는 공유 키 파일을 설정하세요.");
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();
			return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
					.privateKey((RSAPrivateKey) keyPair.getPrivate())
					.keyID(keyId)
					.build();
		}
		catch (Exception exception) {
			throw new IllegalStateException("JWT RSA 키 생성에 실패했습니다.", exception);
		}
	}

	private RSAPrivateKey readPrivateKey(Path path) throws Exception {
		byte[] decoded = decodePem(Files.readString(path, StandardCharsets.UTF_8), "PRIVATE KEY");
		return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
	}

	private RSAPublicKey readPublicKey(Path path) throws Exception {
		byte[] decoded = decodePem(Files.readString(path, StandardCharsets.UTF_8), "PUBLIC KEY");
		return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
	}

	private byte[] decodePem(String pem, String type) throws IOException {
		String normalized = pem
				.replace("-----BEGIN " + type + "-----", "")
				.replace("-----END " + type + "-----", "")
				.replaceAll("\\s", "");
		if (!StringUtils.hasText(normalized)) {
			throw new IOException("PEM 내용이 비어 있습니다: " + type);
		}
		return Base64.getDecoder().decode(normalized);
	}
}
