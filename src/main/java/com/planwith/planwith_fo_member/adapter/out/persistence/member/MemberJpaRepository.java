package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<MemberJpaEntity> findByMemberUuid(String memberUuid);
}
