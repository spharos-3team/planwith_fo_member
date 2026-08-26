package com.planwith.planwith_fo_member.application.member;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.GetProfileImageUseCase;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.ProfileImageStoragePort;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
public class GetProfileImageService implements GetProfileImageUseCase {

	private final MemberRepositoryPort memberRepository;
	private final ProfileImageStoragePort profileImageStoragePort;

	public GetProfileImageService(
			MemberRepositoryPort memberRepository,
			ProfileImageStoragePort profileImageStoragePort
	) {
		this.memberRepository = memberRepository;
		this.profileImageStoragePort = profileImageStoragePort;
	}

	@Override
	@Transactional(readOnly = true)
	public Result get(UUID memberUuid) {
		memberRepository.findByUuid(memberUuid)
				.filter(member -> member.getStatus() == MemberStatus.ACTIVE)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		ProfileImageStoragePort.StoredProfileImage stored = profileImageStoragePort.find(memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_IMAGE_NOT_FOUND));
		return new Result(stored.contentType(), stored.bytes());
	}
}
