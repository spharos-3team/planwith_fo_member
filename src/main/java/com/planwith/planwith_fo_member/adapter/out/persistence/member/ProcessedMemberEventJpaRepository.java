package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMemberEventJpaRepository extends JpaRepository<ProcessedMemberEventJpaEntity, Long> {

	boolean existsByEventUuid(String eventUuid);
}
