package com.planwith.planwith_fo_member.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.FindEmailUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.PhoneVerificationStorePort;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional(readOnly = true)
public class FindEmailService implements FindEmailUseCase {

	private final PhoneVerificationStorePort phoneVerificationStore;
	private final MemberRepositoryPort memberRepository;

	public FindEmailService(
			PhoneVerificationStorePort phoneVerificationStore,
			MemberRepositoryPort memberRepository
	) {
		this.phoneVerificationStore = phoneVerificationStore;
		this.memberRepository = memberRepository;
	}

	@Override
	@Transactional
	public FindEmailResult findByVerifiedPhone(String phoneNumber) {
		String normalized = PhoneVerificationService.normalizePhone(phoneNumber);
		if (!StringUtils.hasText(normalized)) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "휴대폰 번호는 필수입니다.");
		}
		if (!phoneVerificationStore.isVerified(normalized)) {
			throw new BusinessException(ErrorCode.PHONE_NOT_VERIFIED);
		}

		Member member = memberRepository.findByPhoneNumber(normalized)
				.filter(item -> item.getStatus() == MemberStatus.ACTIVE)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		phoneVerificationStore.clear(normalized);
		return new FindEmailResult(
				member.getEmail(),
				maskEmail(member.getEmail()),
				member.getLoginType()
		);
	}

	private String maskEmail(String email) {
		int at = email.indexOf('@');
		if (at <= 1) {
			return "***" + email.substring(Math.max(at, 0));
		}
		return email.charAt(0) + "***" + email.substring(at);
	}
}
