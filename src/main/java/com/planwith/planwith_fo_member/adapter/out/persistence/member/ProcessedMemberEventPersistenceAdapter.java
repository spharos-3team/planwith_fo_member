package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.out.ProcessedMemberEventPort;

@Component
@Transactional
public class ProcessedMemberEventPersistenceAdapter implements ProcessedMemberEventPort {

	private final ProcessedMemberEventJpaRepository processedMemberEventJpaRepository;

	public ProcessedMemberEventPersistenceAdapter(
			ProcessedMemberEventJpaRepository processedMemberEventJpaRepository
	) {
		this.processedMemberEventJpaRepository = processedMemberEventJpaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEventUuid(UUID eventUuid) {
		return processedMemberEventJpaRepository.existsByEventUuid(eventUuid.toString());
	}

	@Override
	public void save(UUID eventUuid, UUID memberUuid, Instant processedAt) {
		ProcessedMemberEventJpaEntity entity = new ProcessedMemberEventJpaEntity();
		entity.setEventUuid(eventUuid.toString());
		entity.setMemberUuid(memberUuid.toString());
		entity.setProcessedAt(processedAt);
		processedMemberEventJpaRepository.save(entity);
	}
}
