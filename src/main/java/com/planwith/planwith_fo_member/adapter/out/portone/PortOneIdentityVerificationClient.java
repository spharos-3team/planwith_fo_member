package com.planwith.planwith_fo_member.adapter.out.portone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.IdentityVerificationClientPort;
import com.planwith.planwith_fo_member.config.PortOneProperties;

@Component
@ConditionalOnProperty(prefix = "app.portone", name = "stub-enabled", havingValue = "false", matchIfMissing = true)
public class PortOneIdentityVerificationClient implements IdentityVerificationClientPort {

	private static final Logger log = LoggerFactory.getLogger(PortOneIdentityVerificationClient.class);

	private final RestClient restClient;

	public PortOneIdentityVerificationClient(PortOneProperties properties) {
		this.restClient = RestClient.builder()
				.baseUrl(properties.apiBaseUrl())
				.defaultHeader("Authorization", "PortOne " + properties.apiSecret())
				.build();
	}

	@Override
	public VerifiedIdentity fetchVerified(String identityVerificationId) {
		try {
			PortOneIdentityVerificationResponse response = restClient.get()
					.uri("/identity-verifications/{id}", identityVerificationId)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(PortOneIdentityVerificationResponse.class);

			if (response == null) {
				throw new BusinessException(ErrorCode.PHONE_VERIFICATION_FAILED);
			}

			String phoneNumber = response.verifiedCustomer() == null ? null : response.verifiedCustomer().phoneNumber();
			String name = response.verifiedCustomer() == null ? null : response.verifiedCustomer().name();
			return new VerifiedIdentity(identityVerificationId, response.status(), phoneNumber, name);
		}
		catch (RestClientResponseException exception) {
			log.warn("PortOne identity verification lookup failed. status={}", exception.getStatusCode().value());
			throw new BusinessException(ErrorCode.PHONE_VERIFICATION_FAILED);
		}
		catch (BusinessException exception) {
			throw exception;
		}
		catch (Exception exception) {
			log.warn("PortOne identity verification lookup unexpected failure", exception);
			throw new BusinessException(ErrorCode.PHONE_VERIFICATION_FAILED);
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record PortOneIdentityVerificationResponse(
			String status,
			VerifiedCustomer verifiedCustomer
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record VerifiedCustomer(
			String name,
			String phoneNumber
	) {
	}
}
