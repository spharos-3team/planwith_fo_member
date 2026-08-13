package com.planwith.planwith_fo_member.application.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class LocalLoginService implements LocalLoginUseCase {

	private final MemberRepositoryPort memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthSessionService authSessionService;

	public LocalLoginService(
			MemberRepositoryPort memberRepository,
			PasswordEncoder passwordEncoder,
			AuthSessionService authSessionService
	) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
		this.authSessionService = authSessionService;
	}

	@Override
	public AuthTokenResult login(LocalLoginCommand command) {
		String email = command.email() == null ? "" : command.email().trim().toLowerCase();
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		if (member.getLoginType() != LoginType.LOCAL
				|| !StringUtils.hasText(member.getPasswordHash())
				|| member.getStatus() != MemberStatus.ACTIVE
				|| !passwordEncoder.matches(command.password(), member.getPasswordHash())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		return authSessionService.issueSession(member.getMemberUuid());
	}
}
