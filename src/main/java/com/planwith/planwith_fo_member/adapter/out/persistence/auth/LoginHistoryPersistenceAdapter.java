package com.planwith.planwith_fo_member.adapter.out.persistence.auth;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.out.LoginHistoryRepositoryPort;
import com.planwith.planwith_fo_member.domain.auth.ActorType;
import com.planwith.planwith_fo_member.domain.auth.LoginHistory;

@Component
@Transactional
public class LoginHistoryPersistenceAdapter implements LoginHistoryRepositoryPort {

	private final LoginHistoryJpaRepository loginHistoryJpaRepository;

	public LoginHistoryPersistenceAdapter(LoginHistoryJpaRepository loginHistoryJpaRepository) {
		this.loginHistoryJpaRepository = loginHistoryJpaRepository;
	}

	@Override
	public LoginHistory save(LoginHistory loginHistory) {
		LoginHistoryJpaEntity entity = new LoginHistoryJpaEntity();
		entity.setActorId(loginHistory.getActorId());
		entity.setActorType(loginHistory.getActorType());
		entity.setIpAddress(loginHistory.getIpAddress());
		entity.setUserAgent(loginHistory.getUserAgent());
		entity.setDeviceInfo(loginHistory.getDeviceInfo());
		entity.setCreatedAt(loginHistory.getCreatedAt());
		return toDomain(loginHistoryJpaRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<LoginHistory> findByActor(ActorType actorType, Long actorId) {
		return loginHistoryJpaRepository.findByActorTypeAndActorIdOrderByCreatedAtDesc(actorType, actorId)
				.stream()
				.map(this::toDomain)
				.toList();
	}

	private LoginHistory toDomain(LoginHistoryJpaEntity entity) {
		return new LoginHistory(
				entity.getId(),
				entity.getActorId(),
				entity.getActorType(),
				entity.getIpAddress(),
				entity.getUserAgent(),
				entity.getDeviceInfo(),
				entity.getCreatedAt()
		);
	}
}
