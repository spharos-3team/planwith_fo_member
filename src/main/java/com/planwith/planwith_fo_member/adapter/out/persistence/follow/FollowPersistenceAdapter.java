package com.planwith.planwith_fo_member.adapter.out.persistence.follow;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.adapter.out.persistence.member.MemberProfileJpaEntity;
import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.FollowRepositoryPort;
import com.planwith.planwith_fo_member.domain.follow.Follow;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;

@Component
@Transactional
public class FollowPersistenceAdapter implements FollowRepositoryPort {

	private final FollowJpaRepository followJpaRepository;
	private final FollowSourceVersionJpaRepository followSourceVersionJpaRepository;

	public FollowPersistenceAdapter(
			FollowJpaRepository followJpaRepository,
			FollowSourceVersionJpaRepository followSourceVersionJpaRepository
	) {
		this.followJpaRepository = followJpaRepository;
		this.followSourceVersionJpaRepository = followSourceVersionJpaRepository;
	}

	@Override
	public Follow save(Follow follow) {
		FollowJpaEntity entity = new FollowJpaEntity();
		entity.setFollowUuid(follow.getFollowUuid().toString());
		entity.setFollowerMemberUuid(follow.getFollowerMemberUuid().toString());
		entity.setFolloweeMemberUuid(follow.getFolloweeMemberUuid().toString());
		entity.setActive(follow.isActive());
		return toDomain(followJpaRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Follow> findByPair(UUID followerMemberUuid, UUID followeeMemberUuid) {
		return followJpaRepository.findByFollowerMemberUuidAndFolloweeMemberUuid(
				followerMemberUuid.toString(),
				followeeMemberUuid.toString()
		).map(this::toDomain);
	}

	@Override
	public Follow updateActive(Long followId, boolean active) {
		FollowJpaEntity entity = followJpaRepository.findById(followId)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		entity.setActive(active);
		return toDomain(followJpaRepository.save(entity));
	}

	@Override
	public long nextSourceVersion(UUID followeeMemberUuid) {
		String followeeUuid = followeeMemberUuid.toString();
		FollowSourceVersionJpaEntity entity = followSourceVersionJpaRepository
				.findByFolloweeMemberUuidForUpdate(followeeUuid)
				.orElse(null);
		if (entity == null) {
			try {
				FollowSourceVersionJpaEntity created = new FollowSourceVersionJpaEntity();
				created.setFolloweeMemberUuid(followeeUuid);
				created.setSourceVersion(1L);
				followSourceVersionJpaRepository.saveAndFlush(created);
				return 1L;
			}
			catch (DataIntegrityViolationException exception) {
				entity = followSourceVersionJpaRepository.findByFolloweeMemberUuidForUpdate(followeeUuid)
						.orElseThrow(() -> exception);
			}
		}
		long next = entity.getSourceVersion() + 1;
		entity.setSourceVersion(next);
		followSourceVersionJpaRepository.save(entity);
		return next;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsActive(UUID followerMemberUuid, UUID followeeMemberUuid) {
		return followJpaRepository.existsByFollowerMemberUuidAndFolloweeMemberUuidAndActiveTrue(
				followerMemberUuid.toString(),
				followeeMemberUuid.toString()
		);
	}

	@Override
	@Transactional(readOnly = true)
	public long countActiveFollowers(UUID followeeMemberUuid) {
		return followJpaRepository.countActiveFollowers(followeeMemberUuid.toString());
	}

	@Override
	@Transactional(readOnly = true)
	public long countActiveFollowings(UUID followerMemberUuid) {
		return followJpaRepository.countActiveFollowings(followerMemberUuid.toString());
	}

	@Override
	@Transactional(readOnly = true)
	public PagedProfiles findActiveFollowerProfiles(UUID followeeMemberUuid, int page, int size) {
		return toPagedProfiles(followJpaRepository.findActiveFollowerProfiles(
				followeeMemberUuid.toString(),
				PageRequest.of(page, size)
		));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedProfiles findActiveFollowingProfiles(UUID followerMemberUuid, int page, int size) {
		return toPagedProfiles(followJpaRepository.findActiveFollowingProfiles(
				followerMemberUuid.toString(),
				PageRequest.of(page, size)
		));
	}

	private PagedProfiles toPagedProfiles(Page<MemberProfileJpaEntity> result) {
		return new PagedProfiles(
				result.getContent().stream().map(this::toProfileDomain).toList(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages()
		);
	}

	private Follow toDomain(FollowJpaEntity entity) {
		return new Follow(
				entity.getFollowId(),
				UUID.fromString(entity.getFollowUuid()),
				UUID.fromString(entity.getFollowerMemberUuid()),
				UUID.fromString(entity.getFolloweeMemberUuid()),
				entity.isActive()
		);
	}

	private MemberProfile toProfileDomain(MemberProfileJpaEntity entity) {
		return new MemberProfile(
				entity.getMemberId(),
				UUID.fromString(entity.getMemberUuid()),
				entity.getNickname(),
				entity.getProfileImage(),
				entity.getProfileIntro(),
				entity.getGrade(),
				entity.isProfileBadge(),
				entity.isProfileSpecialBorder()
		);
	}
}
