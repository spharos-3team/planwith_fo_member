package com.planwith.planwith_fo_member.application.member;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.event.MemberCreatedEvent;
import com.planwith.planwith_fo_member.application.port.out.MemberCreatedEventPort;

@Component
public class MemberCreatedEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(MemberCreatedEventPublisher.class);

	private final MemberCreatedEventPort memberCreatedEventPort;

	public MemberCreatedEventPublisher(MemberCreatedEventPort memberCreatedEventPort) {
		this.memberCreatedEventPort = memberCreatedEventPort;
	}

	public void publish(UUID memberUuid) {
		Objects.requireNonNull(memberUuid, "Member UUID is required.");
		MemberCreatedEvent event = MemberCreatedEvent.of(UUID.randomUUID().toString(), memberUuid.toString());
		log.info("MemberCreatedEventPublisher : publish : 회원 생성 이벤트 발행 요청 - memberUuid={}, eventUuid={}",
				event.memberUuid(), event.eventUuid());
		memberCreatedEventPort.publish(event);
	}
}
