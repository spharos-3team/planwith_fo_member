package com.planwith.planwith_fo_member.application.follow;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.in.FollowMemberUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetFollowStatusUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMyProfileUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListFollowersUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListFollowingsUseCase;
import com.planwith.planwith_fo_member.application.port.in.UnfollowMemberUseCase;
import com.planwith.planwith_fo_member.application.port.out.FollowRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.domain.follow.Follow;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Service
@Transactional
public class FollowService implements
		FollowMemberUseCase,
		UnfollowMemberUseCase,
		GetFollowStatusUseCase,
		ListFollowersUseCase,
		ListFollowingsUseCase {

	private static final int DEFAULT_PAGE = 0;
	private static final int MAX_SIZE = 50;

	private final FollowRepositoryPort followRepository;
	private final MemberRepositoryPort memberRepository;

	public FollowService(FollowRepositoryPort followRepository, MemberRepositoryPort memberRepository) {
		this.followRepository = followRepository;
		this.memberRepository = memberRepository;
	}

	@Override
	public FollowResult follow(UUID followerMemberUuid, UUID followeeMemberUuid) {
		if (followerMemberUuid.equals(followeeMemberUuid)) {
			throw new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
		}
		requireActiveMember(followerMemberUuid);
		requireActiveMember(followeeMemberUuid);

		return followRepository.findByPair(followerMemberUuid, followeeMemberUuid)
				.map(existing -> existing.isActive()
						? toResult(existing)
						: toResult(followRepository.updateActive(existing.getFollowId(), true)))
				.orElseGet(() -> createFollow(followerMemberUuid, followeeMemberUuid));
	}

	@Override
	public void unfollow(UUID followerMemberUuid, UUID followeeMemberUuid) {
		requireActiveMember(followerMemberUuid);
		requireActiveMember(followeeMemberUuid);
		followRepository.findByPair(followerMemberUuid, followeeMemberUuid)
				.filter(Follow::isActive)
				.ifPresent(existing -> followRepository.updateActive(existing.getFollowId(), false));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isFollowing(UUID followerMemberUuid, UUID followeeMemberUuid) {
		requireActiveMember(followeeMemberUuid);
		if (followerMemberUuid.equals(followeeMemberUuid)) {
			return false;
		}
		return followRepository.existsActive(followerMemberUuid, followeeMemberUuid);
	}

	@Override
	@Transactional(readOnly = true)
	public PagedProfiles listFollowers(UUID memberUuid, int page, int size) {
		requireActiveMember(memberUuid);
		return toPagedProfiles(followRepository.findActiveFollowerProfiles(memberUuid, normalizePage(page), normalizeSize(size)));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedProfiles listFollowings(UUID memberUuid, int page, int size) {
		requireActiveMember(memberUuid);
		return toPagedProfiles(followRepository.findActiveFollowingProfiles(memberUuid, normalizePage(page), normalizeSize(size)));
	}

	private FollowResult createFollow(UUID followerMemberUuid, UUID followeeMemberUuid) {
		try {
			return toResult(followRepository.save(Follow.create(followerMemberUuid, followeeMemberUuid)));
		}
		catch (DataIntegrityViolationException exception) {
			Follow existing = followRepository.findByPair(followerMemberUuid, followeeMemberUuid)
					.orElseThrow(() -> exception);
			if (existing.isActive()) {
				return toResult(existing);
			}
			return toResult(followRepository.updateActive(existing.getFollowId(), true));
		}
	}

	private void requireActiveMember(UUID memberUuid) {
		Member member = memberRepository.findByUuid(memberUuid)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
	}

	private FollowResult toResult(Follow follow) {
		return new FollowResult(follow.getFollowUuid(), follow.isActive());
	}

	private PagedProfiles toPagedProfiles(FollowRepositoryPort.PagedProfiles page) {
		return new PagedProfiles(
				page.profiles().stream().map(this::toProfileResult).toList(),
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages()
		);
	}

	private GetMyProfileUseCase.ProfileResult toProfileResult(MemberProfile profile) {
		return new GetMyProfileUseCase.ProfileResult(
				profile.getMemberUuid(),
				profile.getNickname(),
				profile.getProfileImage(),
				profile.getProfileIntro(),
				profile.getGrade()
		);
	}

	private int normalizePage(int page) {
		if (page < DEFAULT_PAGE) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "page는 0 이상이어야 합니다.");
		}
		return page;
	}

	private int normalizeSize(int size) {
		if (size < 1 || size > MAX_SIZE) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "size는 1 이상 " + MAX_SIZE + " 이하여야 합니다.");
		}
		return size;
	}
}
