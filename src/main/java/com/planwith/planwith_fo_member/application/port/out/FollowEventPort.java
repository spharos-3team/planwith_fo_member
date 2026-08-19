package com.planwith.planwith_fo_member.application.port.out;

import com.planwith.planwith_fo_member.application.event.FollowChangedEvent;

public interface FollowEventPort {

	void publishCreated(FollowChangedEvent event);

	void publishRemoved(FollowChangedEvent event);
}
