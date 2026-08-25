package com.planwith.planwith_fo_member.application.port.out;

public interface ClientRequestInfoPort {

	record ClientRequestInfo(String ipAddress, String userAgent) {
	}

	ClientRequestInfo current();
}
