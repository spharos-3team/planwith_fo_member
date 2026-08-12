package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileJpaRepository extends JpaRepository<MemberProfileJpaEntity, Long> {

	boolean existsByNickname(String nickname);
}
