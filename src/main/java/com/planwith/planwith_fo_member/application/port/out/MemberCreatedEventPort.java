package com.planwith.planwith_fo_member.application.port.out;

import com.planwith.planwith_fo_member.application.event.MemberCreatedEvent;

public interface MemberCreatedEventPort {

	void publish(MemberCreatedEvent event);
}
