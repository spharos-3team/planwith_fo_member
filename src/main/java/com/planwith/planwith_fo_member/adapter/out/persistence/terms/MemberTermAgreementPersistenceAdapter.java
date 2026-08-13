package com.planwith.planwith_fo_member.adapter.out.persistence.terms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
		upsertAgreements(memberUuid, agreements);
	}

	@Override
	public void upsertAgreements(UUID memberUuid, List<AgreementCommand> agreements) {
		String uuid = memberUuid.toString();
		Map<Long, MemberTermAgreementJpaEntity> existing = repository.findByMemberUuid(uuid).stream()
				.collect(Collectors.toMap(MemberTermAgreementJpaEntity::getTermId, Function.identity()));

		List<MemberTermAgreementJpaEntity> toSave = new ArrayList<>();
		for (AgreementCommand command : agreements) {
			MemberTermAgreementJpaEntity entity = existing.getOrDefault(command.termId(), new MemberTermAgreementJpaEntity());
			entity.setTermId(command.termId());
			entity.setMemberUuid(uuid);
			entity.setAgreed(command.agreed());
			entity.setAgreedAt(command.agreedAt());
			toSave.add(entity);
		}
		repository.saveAll(toSave);
	}

	@Override
	@Transactional(readOnly = true)
	public List<StoredAgreement> findByMemberUuid(UUID memberUuid) {
		return repository.findByMemberUuid(memberUuid.toString()).stream()
				.map(entity -> new StoredAgreement(entity.getTermId(), entity.isAgreed(), entity.getAgreedAt()))
				.toList();
	}
}
