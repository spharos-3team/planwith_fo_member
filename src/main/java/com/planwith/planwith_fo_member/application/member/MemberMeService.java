package com.planwith.planwith_fo_member.application.member;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.ChangePasswordUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMemberInternalUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMyMemberUseCase;
import com.planwith.planwith_fo_member.application.port.in.WithdrawMemberUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.RefreshTokenStorePort;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class MemberMeService implements
		GetMyMemberUseCase,
		ChangePasswordUseCase,
		WithdrawMemberUseCase,
		GetMemberInternalUseCase {

	private final MemberRepositoryPort memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenStorePort refreshTokenStore;

	public MemberMeService(
			MemberRepositoryPort memberRepository,
			PasswordEncoder passwordEncoder,
			RefreshTokenStorePort refreshTokenStore
	) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.refreshTokenStore = refreshTokenStore;
	}

	@Override
	@Transactional(readOnly = true)
	public MyMemberResult get(UUID memberUuid) {
		return toResult(requireActiveMember(memberUuid));
	}

	@Override
	public void change(UUID memberUuid, String currentPassword, String newPassword) {
		Member member = requireActiveMember(memberUuid);
		if (member.getLoginType() != LoginType.LOCAL || !StringUtils.hasText(member.getPasswordHash())) {
			throw new BusinessException(ErrorCode.PASSWORD_CHANGE_NOT_ALLOWED_FOR_SOCIAL);
		}
		if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}
		if (!passwordEncoder.matches(currentPassword, member.getPasswordHash())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "현재 비밀번호가 올바르지 않습니다.");
		}
		memberRepository.updatePassword(memberUuid, passwordEncoder.encode(newPassword));
	}

	@Override
	public void withdraw(UUID memberUuid) {
		requireActiveMember(memberUuid);
		memberRepository.softDelete(memberUuid, MemberStatus.DELETED, Instant.now());
		refreshTokenStore.revokeAllForMember(memberUuid);
	}

	@Override
	@Transactional(readOnly = true)
	public InternalMemberResult getInternal(UUID memberUuid) {
		Member member = memberRepository.findByUuid(memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (member.getStatus() == MemberStatus.DELETED) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
		return new InternalMemberResult(
				member.getMemberUuid(),
				member.getEmail(),
				member.getPhoneNumber(),
				member.getName(),
				member.getLoginType(),
				member.getStatus(),
				member.getCreatedAt()
		);
	}

	private Member requireActiveMember(UUID memberUuid) {
		Member member = memberRepository.findByUuid(memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
		return member;
	}

	private MyMemberResult toResult(Member member) {
		return new MyMemberResult(
				member.getMemberUuid(),
				member.getEmail(),
				member.getPhoneNumber(),
				member.getName(),
				member.getLoginType(),
				member.getStatus(),
				member.getCreatedAt(),
				member.getLastLoginAt()
		);
	}
}
