package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.out.ProfileImageStoragePort;

@Component
@Transactional
public class MemberProfileImagePersistenceAdapter implements ProfileImageStoragePort {

	private final MemberProfileImageJpaRepository memberProfileImageJpaRepository;

	public MemberProfileImagePersistenceAdapter(
			MemberProfileImageJpaRepository memberProfileImageJpaRepository
	) {
		this.memberProfileImageJpaRepository = memberProfileImageJpaRepository;
	}

	@Override
	public void save(UUID memberUuid, String contentType, byte[] bytes) {
		MemberProfileImageJpaEntity entity = memberProfileImageJpaRepository.findById(memberUuid.toString())
				.orElseGet(MemberProfileImageJpaEntity::new);
		entity.setMemberUuid(memberUuid.toString());
		entity.setContentType(contentType);
		entity.setImageBytes(bytes);
		memberProfileImageJpaRepository.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoredProfileImage> find(UUID memberUuid) {
		return memberProfileImageJpaRepository.findById(memberUuid.toString())
				.map(entity -> new StoredProfileImage(entity.getContentType(), entity.getImageBytes()));
	}
}
