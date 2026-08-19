package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberProfileJpaRepository extends JpaRepository<MemberProfileJpaEntity, Long> {

	boolean existsByNickname(String nickname);

	boolean existsByNicknameAndMemberUuidNot(String nickname, String memberUuid);

	Optional<MemberProfileJpaEntity> findByMemberUuid(String memberUuid);

	@Query("""
			select p from MemberProfileJpaEntity p
			join MemberJpaEntity m on m.memberUuid = p.memberUuid
			where m.status = com.planwith.planwith_fo_member.domain.member.MemberStatus.ACTIVE
			  and (:nickname is null or lower(p.nickname) like lower(concat('%', :nickname, '%')))
			  and (:excludeMemberUuid is null or p.memberUuid <> :excludeMemberUuid)
			order by p.nickname asc
			""")
	Page<MemberProfileJpaEntity> findActiveProfiles(
			@Param("nickname") String nickname,
			@Param("excludeMemberUuid") String excludeMemberUuid,
			Pageable pageable
	);
}
