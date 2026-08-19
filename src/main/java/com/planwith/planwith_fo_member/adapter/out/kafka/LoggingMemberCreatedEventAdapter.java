package com.planwith.planwith_fo_member.adapter.out.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.event.MemberCreatedEvent;
import com.planwith.planwith_fo_member.application.port.out.MemberCreatedEventPort;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingMemberCreatedEventAdapter implements MemberCreatedEventPort {

	private static final Logger log = LoggerFactory.getLogger(LoggingMemberCreatedEventAdapter.class);

	@Override
	public void publish(MemberCreatedEvent event) {
		log.info("LoggingMemberCreatedEventAdapter : publish : Kafka 비활성 상태로 회원 생성 이벤트 발행을 생략 - eventUuid={}, memberUuid={}",
				event.eventUuid(), event.memberUuid());
	}
}
