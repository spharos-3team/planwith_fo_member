package com.planwith.planwith_fo_member.adapter.out.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.port.out.EmailSenderPort;

@Component
public class LoggingEmailSender implements EmailSenderPort {

	private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

	@Override
	public void sendVerificationCode(String email, String code) {
		log.info("Email verification code for {}: {}", email, code);
	}
}
