package com.planwith.planwith_fo_member.application.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.in.CheckNicknameAvailabilityUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;

@Service
@Transactional(readOnly = true)
public class NicknameAvailabilityService implements CheckNicknameAvailabilityUseCase {

	private final MemberRepositoryPort memberRepository;

	public NicknameAvailabilityService(MemberRepositoryPort memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override
	public NicknameAvailabilityResult check(String nickname) {
		String normalized = nickname.trim();
		boolean available = !memberRepository.existsByNickname(normalized);
		return new NicknameAvailabilityResult(normalized, available);
	}
}
