package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@Component
@Transactional
public class MemberPersistenceAdapter implements MemberRepositoryPort {

	private final MemberJpaRepository memberJpaRepository;
	private final MemberProfileJpaRepository memberProfileJpaRepository;

	public MemberPersistenceAdapter(
			MemberJpaRepository memberJpaRepository,
			MemberProfileJpaRepository memberProfileJpaRepository
	) {
		this.memberJpaRepository = memberJpaRepository;
		this.memberProfileJpaRepository = memberProfileJpaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return memberJpaRepository.existsByEmailIgnoreCase(email);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByNickname(String nickname) {
		return memberProfileJpaRepository.existsByNickname(nickname);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByNicknameExcludingMember(String nickname, UUID memberUuid) {
		return memberProfileJpaRepository.existsByNicknameAndMemberUuidNot(nickname, memberUuid.toString());
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByLoginTypeAndSocialId(LoginType loginType, String socialId) {
		return memberJpaRepository.existsByLoginTypeAndSocialId(loginType, socialId);
	}

	@Override
	public Member saveMember(Member member, MemberProfile profile) {
		MemberJpaEntity memberEntity = new MemberJpaEntity();
		memberEntity.setMemberUuid(member.getMemberUuid().toString());
		memberEntity.setLoginType(member.getLoginType());
		memberEntity.setEmail(member.getEmail());
		memberEntity.setPassword(member.getPasswordHash());
		memberEntity.setPhoneNumber(member.getPhoneNumber());
		memberEntity.setName(member.getName());
		memberEntity.setSocialId(member.getSocialId());
		memberEntity.setStatus(member.getStatus());
		memberEntity.setCreatedAt(member.getCreatedAt());

		MemberJpaEntity savedMember = memberJpaRepository.save(memberEntity);

		MemberProfileJpaEntity profileEntity = new MemberProfileJpaEntity();
		profileEntity.setMemberId(savedMember.getMemberId());
		profileEntity.setMemberUuid(savedMember.getMemberUuid());
		profileEntity.setNickname(profile.getNickname());
		profileEntity.setProfileImage(profile.getProfileImage());
		profileEntity.setProfileIntro(profile.getProfileIntro());
		profileEntity.setGrade(profile.getGrade());
		memberProfileJpaRepository.save(profileEntity);

		return toDomain(savedMember);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Member> findByUuid(UUID memberUuid) {
		return memberJpaRepository.findByMemberUuid(memberUuid.toString()).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Member> findByEmail(String email) {
		return memberJpaRepository.findByEmailIgnoreCase(email).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Member> findByLoginTypeAndSocialId(LoginType loginType, String socialId) {
		return memberJpaRepository.findByLoginTypeAndSocialId(loginType, socialId).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Member> findByPhoneNumber(String phoneNumber) {
		return memberJpaRepository.findFirstByPhoneNumber(phoneNumber).map(this::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<MemberProfile> findProfileByMemberUuid(UUID memberUuid) {
		return memberProfileJpaRepository.findByMemberUuid(memberUuid.toString()).map(this::toProfileDomain);
	}

	@Override
	public void updateLastLoginAt(UUID memberUuid, Instant lastLoginAt) {
		MemberJpaEntity entity = requireMember(memberUuid);
		entity.setLastLoginAt(lastLoginAt);
		memberJpaRepository.save(entity);
	}

	@Override
	public void updatePassword(UUID memberUuid, String passwordHash) {
		MemberJpaEntity entity = requireMember(memberUuid);
		entity.setPassword(passwordHash);
		memberJpaRepository.save(entity);
	}

	@Override
	public void updatePhoneNumber(UUID memberUuid, String phoneNumber) {
		updatePhoneIdentity(memberUuid, phoneNumber, null);
	}

	@Override
	public void updatePhoneIdentity(UUID memberUuid, String phoneNumber, String name) {
		MemberJpaEntity entity = requireMember(memberUuid);
		entity.setPhoneNumber(phoneNumber);
		if (name != null) {
			entity.setName(name);
		}
		memberJpaRepository.save(entity);
	}

	@Override
	public void updateProfile(UUID memberUuid, String nickname, String profileImage, String profileIntro) {
		MemberProfileJpaEntity entity = memberProfileJpaRepository.findByMemberUuid(memberUuid.toString())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		if (nickname != null) {
			entity.setNickname(nickname);
		}
		if (profileImage != null) {
			entity.setProfileImage(profileImage.isBlank() ? null : profileImage);
		}
		if (profileIntro != null) {
			entity.setProfileIntro(profileIntro.isBlank() ? null : profileIntro);
		}
		memberProfileJpaRepository.save(entity);
	}

	@Override
	public void updateProfileImage(UUID memberUuid, String profileImageUrl) {
		MemberProfileJpaEntity entity = memberProfileJpaRepository.findByMemberUuid(memberUuid.toString())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		entity.setProfileImage(profileImageUrl);
		memberProfileJpaRepository.save(entity);
	}

	@Override
	public void softDelete(UUID memberUuid, MemberStatus status, Instant deletedAt) {
		MemberJpaEntity entity = requireMember(memberUuid);
		entity.setStatus(status);
		entity.setDeletedAt(deletedAt);
		memberJpaRepository.save(entity);
	}

	private MemberJpaEntity requireMember(UUID memberUuid) {
		return memberJpaRepository.findByMemberUuid(memberUuid.toString())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
	}

	private Member toDomain(MemberJpaEntity entity) {
		return new Member(
				entity.getMemberId(),
				UUID.fromString(entity.getMemberUuid()),
				entity.getLoginType(),
				entity.getEmail(),
				entity.getPassword(),
				entity.getPhoneNumber(),
				entity.getName(),
				entity.getSocialId(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getLastLoginAt()
		);
	}

	private MemberProfile toProfileDomain(MemberProfileJpaEntity entity) {
		return new MemberProfile(
				entity.getMemberId(),
				UUID.fromString(entity.getMemberUuid()),
				entity.getNickname(),
				entity.getProfileImage(),
				entity.getProfileIntro(),
				entity.getGrade()
		);
	}
}
