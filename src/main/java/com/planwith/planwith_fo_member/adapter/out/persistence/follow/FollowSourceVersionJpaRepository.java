package com.planwith.planwith_fo_member.adapter.out.persistence.follow;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface FollowSourceVersionJpaRepository extends JpaRepository<FollowSourceVersionJpaEntity, String> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select v from FollowSourceVersionJpaEntity v where v.followeeMemberUuid = :followeeMemberUuid")
	Optional<FollowSourceVersionJpaEntity> findByFolloweeMemberUuidForUpdate(
			@Param("followeeMemberUuid") String followeeMemberUuid
	);
}
