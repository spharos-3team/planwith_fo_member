package com.planwith.planwith_fo_member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_member.config.AuthProperties;
import com.planwith.planwith_fo_member.config.DeployProperties;
import com.planwith.planwith_fo_member.config.EmailProperties;
import com.planwith.planwith_fo_member.config.EmailVerificationProperties;
import com.planwith.planwith_fo_member.config.GatewayTrustProperties;
import com.planwith.planwith_fo_member.config.JwtProperties;
import com.planwith.planwith_fo_member.config.MemberKafkaProperties;
import com.planwith.planwith_fo_member.config.PortOneProperties;
import com.planwith.planwith_fo_member.config.RefreshCookieProperties;
import com.planwith.planwith_fo_member.config.SocialOAuthProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		EmailProperties.class,
		EmailVerificationProperties.class,
		PortOneProperties.class,
		SocialOAuthProperties.class,
		JwtProperties.class,
		MemberKafkaProperties.class,
		RefreshCookieProperties.class,
		GatewayTrustProperties.class
})
public class PlanwithFoMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoMemberApplication.class, args);
	}
}
