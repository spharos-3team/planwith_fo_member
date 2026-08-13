package com.planwith.planwith_fo_member.adapter.out.persistence.terms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermAgreementJpaRepository extends JpaRepository<MemberTermAgreementJpaEntity, Long> {

	List<MemberTermAgreementJpaEntity> findByMemberUuid(String memberUuid);
}
