package com.planwith.planwith_fo_member.adapter.out.persistence.terms;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_member.application.port.out.MemberTermAgreementPort;

@Component
@Transactional
public class MemberTermAgreementPersistenceAdapter implements MemberTermAgreementPort {

	private final MemberTermAgreementJpaRepository repository;

	public MemberTermAgreementPersistenceAdapter(MemberTermAgreementJpaRepository repository) {
		this.repository = repository;
	}

	@Override
	public void saveAgreements(UUID memberUuid, List<AgreementCommand> agreements) {
		String uuid = memberUuid.toString();
		List<MemberTermAgreementJpaEntity> entities = agreements.stream().map(command -> {
			MemberTermAgreementJpaEntity entity = new MemberTermAgreementJpaEntity();
			entity.setTermId(command.termId());
			entity.setMemberUuid(uuid);
			entity.setAgreed(command.agreed());
			entity.setAgreedAt(command.agreedAt());
			return entity;
		}).toList();
		repository.saveAll(entities);
	}
}
