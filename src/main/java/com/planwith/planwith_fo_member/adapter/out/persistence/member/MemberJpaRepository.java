package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_member.domain.member.LoginType;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByLoginTypeAndSocialId(LoginType loginType, String socialId);

	Optional<MemberJpaEntity> findByMemberUuid(String memberUuid);
}
