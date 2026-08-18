package com.planwith.planwith_fo_member.application.port.out;

/**
 * 포트원 스텁 전용. prepare에서 지정한 휴대폰·실명을 confirm id에 연결한다.
 * 실연동 빈은 등록하지 않는다.
 */
public interface StubIdentityDraftPort {

	void remember(String identityVerificationId, String phoneNumber, String name);
}
