package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileJpaRepository extends JpaRepository<MemberProfileJpaEntity, Long> {

	boolean existsByNickname(String nickname);

	boolean existsByNicknameAndMemberUuidNot(String nickname, String memberUuid);

	Optional<MemberProfileJpaEntity> findByMemberUuid(String memberUuid);
}
