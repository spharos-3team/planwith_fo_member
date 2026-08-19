package com.planwith.planwith_fo_member.adapter.out.persistence.follow;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.planwith.planwith_fo_member.adapter.out.persistence.member.MemberProfileJpaEntity;

public interface FollowJpaRepository extends JpaRepository<FollowJpaEntity, Long> {

	Optional<FollowJpaEntity> findByFollowerMemberUuidAndFolloweeMemberUuid(
			String followerMemberUuid,
			String followeeMemberUuid
	);

	boolean existsByFollowerMemberUuidAndFolloweeMemberUuidAndActiveTrue(
			String followerMemberUuid,
			String followeeMemberUuid
	);

	@Query("""
			select p from FollowJpaEntity f
			join MemberJpaEntity m on m.memberUuid = f.followerMemberUuid
			join MemberProfileJpaEntity p on p.memberUuid = f.followerMemberUuid
			where f.followeeMemberUuid = :memberUuid
			  and f.active = true
			  and m.status = com.planwith.planwith_fo_member.domain.member.MemberStatus.ACTIVE
			order by f.followId desc
			""")
	Page<MemberProfileJpaEntity> findActiveFollowerProfiles(
			@Param("memberUuid") String memberUuid,
			Pageable pageable
	);

	@Query("""
			select p from FollowJpaEntity f
			join MemberJpaEntity m on m.memberUuid = f.followeeMemberUuid
			join MemberProfileJpaEntity p on p.memberUuid = f.followeeMemberUuid
			where f.followerMemberUuid = :memberUuid
			  and f.active = true
			  and m.status = com.planwith.planwith_fo_member.domain.member.MemberStatus.ACTIVE
			order by f.followId desc
			""")
	Page<MemberProfileJpaEntity> findActiveFollowingProfiles(
			@Param("memberUuid") String memberUuid,
			Pageable pageable
	);

	@Query("""
			select count(f) from FollowJpaEntity f
			join MemberJpaEntity m on m.memberUuid = f.followerMemberUuid
			where f.followeeMemberUuid = :memberUuid
			  and f.active = true
			  and m.status = com.planwith.planwith_fo_member.domain.member.MemberStatus.ACTIVE
			""")
	long countActiveFollowers(@Param("memberUuid") String memberUuid);

	@Query("""
			select count(f) from FollowJpaEntity f
			join MemberJpaEntity m on m.memberUuid = f.followeeMemberUuid
			where f.followerMemberUuid = :memberUuid
			  and f.active = true
			  and m.status = com.planwith.planwith_fo_member.domain.member.MemberStatus.ACTIVE
			""")
	long countActiveFollowings(@Param("memberUuid") String memberUuid);
}
