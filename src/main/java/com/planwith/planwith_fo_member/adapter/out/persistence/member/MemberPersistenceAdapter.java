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
	public void updateLastLoginAt(UUID memberUuid, Instant lastLoginAt) {
		MemberJpaEntity entity = memberJpaRepository.findByMemberUuid(memberUuid.toString())
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		entity.setLastLoginAt(lastLoginAt);
		memberJpaRepository.save(entity);
	}

	private Member toDomain(MemberJpaEntity entity) {
		return new Member(
				entity.getMemberId(),
				UUID.fromString(entity.getMemberUuid()),
				entity.getLoginType(),
				entity.getEmail(),
				entity.getPassword(),
				entity.getPhoneNumber(),
				entity.getSocialId(),
				entity.getStatus(),
				entity.getCreatedAt()
		);
	}
}
