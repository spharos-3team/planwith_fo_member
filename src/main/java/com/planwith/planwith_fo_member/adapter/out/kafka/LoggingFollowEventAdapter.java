package com.planwith.planwith_fo_member.adapter.out.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.event.FollowChangedEvent;
import com.planwith.planwith_fo_member.application.port.out.FollowEventPort;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingFollowEventAdapter implements FollowEventPort {

	private static final Logger log = LoggerFactory.getLogger(LoggingFollowEventAdapter.class);

	@Override
	public void publishCreated(FollowChangedEvent event) {
		log.info(
				"LoggingFollowEventAdapter : publishCreated : Kafka 비활성 상태로 팔로우 생성 이벤트 발행을 생략 - eventUuid={}, followeeUuid={}",
				event.eventUuid(),
				event.followeeUuid()
		);
	}

	@Override
	public void publishRemoved(FollowChangedEvent event) {
		log.info(
				"LoggingFollowEventAdapter : publishRemoved : Kafka 비활성 상태로 팔로우 해제 이벤트 발행을 생략 - eventUuid={}, followeeUuid={}",
				event.eventUuid(),
				event.followeeUuid()
		);
	}
}
