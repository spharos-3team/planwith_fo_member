package com.planwith.planwith_fo_member.application.auth;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.ConfirmPhoneVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.PreparePhoneVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.out.IdentityVerificationClientPort;
import com.planwith.planwith_fo_member.application.port.out.PhoneVerificationStorePort;
import com.planwith.planwith_fo_member.config.PortOneProperties;

@Service
public class PhoneVerificationService implements PreparePhoneVerificationUseCase, ConfirmPhoneVerificationUseCase {

	private final IdentityVerificationClientPort identityVerificationClient;
	private final PhoneVerificationStorePort phoneVerificationStore;
	private final PortOneProperties portOneProperties;

	public PhoneVerificationService(
			IdentityVerificationClientPort identityVerificationClient,
			PhoneVerificationStorePort phoneVerificationStore,
			PortOneProperties portOneProperties
	) {
		this.identityVerificationClient = identityVerificationClient;
		this.phoneVerificationStore = phoneVerificationStore;
		this.portOneProperties = portOneProperties;
	}

	@Override
	public PreparePhoneVerificationResult prepare() {
		if (!StringUtils.hasText(portOneProperties.storeId()) || !StringUtils.hasText(portOneProperties.channelKey())) {
			throw new BusinessException(ErrorCode.IDENTITY_VERIFICATION_CONFIG_MISSING);
		}
		String identityVerificationId = "identity-verification-" + UUID.randomUUID();
		return new PreparePhoneVerificationResult(
				portOneProperties.storeId(),
				portOneProperties.channelKey(),
				identityVerificationId
		);
	}

	@Override
	public ConfirmPhoneVerificationResult confirm(String identityVerificationId) {
		if (!StringUtils.hasText(identityVerificationId)) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "identityVerificationId는 필수입니다.");
		}

		IdentityVerificationClientPort.VerifiedIdentity verified =
				identityVerificationClient.fetchVerified(identityVerificationId.trim());

		if (!verified.isVerified()) {
			throw new BusinessException(ErrorCode.PHONE_VERIFICATION_FAILED);
		}
		if (!StringUtils.hasText(verified.phoneNumber())) {
			throw new BusinessException(ErrorCode.PHONE_VERIFICATION_FAILED, "본인인증 결과에 휴대폰 번호가 없습니다.");
		}

		String phoneNumber = normalizePhone(verified.phoneNumber());
		Instant verifiedUntil = Instant.now().plusSeconds(portOneProperties.verifiedTtlSeconds());
		phoneVerificationStore.markVerified(phoneNumber, verifiedUntil);

		return new ConfirmPhoneVerificationResult(true, phoneNumber, maskPhone(phoneNumber));
	}

	public static String normalizePhone(String phoneNumber) {
		return phoneNumber == null ? null : phoneNumber.replaceAll("[^0-9]", "");
	}

	public static String maskPhone(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.length() < 7) {
			return "****";
		}
		return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
	}
}
