package com.planwith.planwith_fo_member.application.auth;

import java.security.SecureRandom;
import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.RequestPasswordResetUseCase;
import com.planwith.planwith_fo_member.application.port.in.ResetPasswordUseCase;
import com.planwith.planwith_fo_member.application.port.out.EmailSenderPort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.PasswordResetStorePort;
import com.planwith.planwith_fo_member.config.EmailVerificationProperties;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class PasswordResetService implements RequestPasswordResetUseCase, ResetPasswordUseCase {

	private final MemberRepositoryPort memberRepository;
	private final PasswordResetStorePort passwordResetStore;
	private final EmailSenderPort emailSender;
	private final PasswordEncoder passwordEncoder;
	private final EmailVerificationProperties properties;
	private final SecureRandom secureRandom = new SecureRandom();

	public PasswordResetService(
			MemberRepositoryPort memberRepository,
			PasswordResetStorePort passwordResetStore,
			EmailSenderPort emailSender,
			PasswordEncoder passwordEncoder,
			EmailVerificationProperties properties
	) {
		this.memberRepository = memberRepository;
		this.passwordResetStore = passwordResetStore;
		this.emailSender = emailSender;
		this.passwordEncoder = passwordEncoder;
		this.properties = properties;
	}

	@Override
	public RequestPasswordResetResult request(String email) {
		String normalized = normalize(email);
		Member member = memberRepository.findByEmail(normalized)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
		if (member.getLoginType() != LoginType.LOCAL || !StringUtils.hasText(member.getPasswordHash())) {
			throw new BusinessException(ErrorCode.PASSWORD_RESET_NOT_ALLOWED_FOR_SOCIAL);
		}

		String code = generateCode(properties.codeLength());
		Instant expiresAt = Instant.now().plusSeconds(properties.codeTtlSeconds());
		passwordResetStore.saveCode(normalized, code, expiresAt);
		emailSender.sendVerificationCode(normalized, code);

		return new RequestPasswordResetResult(
				normalized,
				properties.codeTtlSeconds(),
				"비밀번호 재설정 인증번호를 발송했습니다."
		);
	}

	@Override
	public void reset(String email, String code, String newPassword) {
		String normalized = normalize(email);
		if (!StringUtils.hasText(code) || !StringUtils.hasText(newPassword)) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}

		Member member = memberRepository.findByEmail(normalized)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (member.getLoginType() != LoginType.LOCAL || !StringUtils.hasText(member.getPasswordHash())) {
			throw new BusinessException(ErrorCode.PASSWORD_RESET_NOT_ALLOWED_FOR_SOCIAL);
		}

		PasswordResetStorePort.StoredCode stored = passwordResetStore.findCode(normalized)
				.orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED));
		if (stored.expiresAt().isBefore(Instant.now())) {
			passwordResetStore.clear(normalized);
			throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
		}
		if (!stored.code().equals(code.trim())) {
			throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_INVALID);
		}

		memberRepository.updatePassword(member.getMemberUuid(), passwordEncoder.encode(newPassword));
		passwordResetStore.clear(normalized);
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
