package com.planwith.planwith_fo_member.application.auth;

import java.security.SecureRandom;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.ConfirmEmailVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.SendEmailVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.out.EmailSenderPort;
import com.planwith.planwith_fo_member.application.port.out.EmailVerificationStorePort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.config.EmailVerificationProperties;

@Service
@Transactional
public class EmailVerificationService implements SendEmailVerificationUseCase, ConfirmEmailVerificationUseCase {

	private final EmailVerificationStorePort verificationStore;
	private final EmailSenderPort emailSender;
	private final MemberRepositoryPort memberRepository;
	private final EmailVerificationProperties properties;
	private final SecureRandom secureRandom = new SecureRandom();

	public EmailVerificationService(
			EmailVerificationStorePort verificationStore,
			EmailSenderPort emailSender,
			MemberRepositoryPort memberRepository,
			EmailVerificationProperties properties
	) {
		this.verificationStore = verificationStore;
		this.emailSender = emailSender;
		this.memberRepository = memberRepository;
		this.properties = properties;
	}

	@Override
	public SendEmailVerificationResult send(String email) {
		String normalizedEmail = normalize(email);
		if (memberRepository.existsByEmail(normalizedEmail)) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		String code = generateCode(properties.codeLength());
		Instant expiresAt = Instant.now().plusSeconds(properties.codeTtlSeconds());
		verificationStore.saveCode(normalizedEmail, code, expiresAt);
		emailSender.sendVerificationCode(normalizedEmail, code);
		return new SendEmailVerificationResult(normalizedEmail, properties.codeTtlSeconds());
	}

	@Override
	public ConfirmEmailVerificationResult confirm(String email, String code) {
		String normalizedEmail = normalize(email);
		EmailVerificationStorePort.StoredCode stored = verificationStore.findCode(normalizedEmail)
				.orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED));

		if (stored.expiresAt().isBefore(Instant.now())) {
			verificationStore.clear(normalizedEmail);
			throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
		}

		if (!stored.code().equals(code)) {
			throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_INVALID);
		}

		Instant verifiedUntil = Instant.now().plusSeconds(properties.verifiedTtlSeconds());
		verificationStore.markVerified(normalizedEmail, verifiedUntil);
		return new ConfirmEmailVerificationResult(normalizedEmail, true);
	}

	private String generateCode(int length) {
		int bound = (int) Math.pow(10, length);
		int number = secureRandom.nextInt(bound / 10, bound);
		return String.valueOf(number);
	}

	private String normalize(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}
}
