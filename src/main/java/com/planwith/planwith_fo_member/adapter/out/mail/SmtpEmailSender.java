package com.planwith.planwith_fo_member.adapter.out.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.EmailSenderPort;
import com.planwith.planwith_fo_member.config.EmailProperties;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "stub-enabled", havingValue = "false")
public class SmtpEmailSender implements EmailSenderPort {

	private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

	private final JavaMailSender mailSender;
	private final EmailProperties emailProperties;

	public SmtpEmailSender(JavaMailSender mailSender, EmailProperties emailProperties) {
		this.mailSender = mailSender;
		this.emailProperties = emailProperties;
	}

	@Override
	public void sendVerificationCode(String email, String code) {
		send(email, "[PlanWith] 이메일 인증번호", code, "회원가입 이메일 인증번호");
	}

	@Override
	public void sendPasswordResetCode(String email, String code) {
		send(email, "[PlanWith] 비밀번호 재설정 인증번호", code, "비밀번호 재설정 인증번호");
	}

	private void send(String email, String subject, String code, String purpose) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(email);
			String from = StringUtils.hasText(emailProperties.from()) ? emailProperties.from().trim() : null;
			if (from != null) {
				message.setFrom(from);
			}
			message.setSubject(subject);
			message.setText("""
					%s입니다.

					인증번호: %s

					유효 시간 안에 인증해 주세요.
					""".formatted(purpose, code));
			mailSender.send(message);
			log.info("Sent {} email to {}", purpose, email);
		}
		catch (MailException exception) {
			log.warn("Failed to send {} email to {}", purpose, email);
			throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
		}
	}
}
