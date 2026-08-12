package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
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
	public Member saveLocalMember(Member member, MemberProfile profile) {
		MemberJpaEntity memberEntity = new MemberJpaEntity();
		memberEntity.setMemberUuid(member.getMemberUuid().toString());
		memberEntity.setLoginType(member.getLoginType());
		memberEntity.setEmail(member.getEmail());
		memberEntity.setPassword(member.getPasswordHash());
		memberEntity.setPhoneNumber(member.getPhoneNumber());
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

	private Member toDomain(MemberJpaEntity entity) {
		return new Member(
				entity.getMemberId(),
				UUID.fromString(entity.getMemberUuid()),
				entity.getLoginType(),
				entity.getEmail(),
				entity.getPassword(),
				entity.getPhoneNumber(),
				entity.getStatus(),
				entity.getCreatedAt()
		);
	}
}
