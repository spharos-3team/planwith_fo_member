package com.planwith.planwith_fo_member.adapter.out.persistence.auth;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_member.domain.auth.ActorType;

public interface LoginHistoryJpaRepository extends JpaRepository<LoginHistoryJpaEntity, Long> {

	List<LoginHistoryJpaEntity> findByActorTypeAndActorIdOrderByCreatedAtDesc(ActorType actorType, Long actorId);
}
