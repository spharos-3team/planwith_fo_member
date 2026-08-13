package com.planwith.planwith_fo_member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_member.config.AuthProperties;
import com.planwith.planwith_fo_member.config.DeployProperties;
import com.planwith.planwith_fo_member.config.EmailVerificationProperties;
import com.planwith.planwith_fo_member.config.PortOneProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		EmailVerificationProperties.class,
		PortOneProperties.class
})
public class PlanwithFoMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoMemberApplication.class, args);
	}
}
