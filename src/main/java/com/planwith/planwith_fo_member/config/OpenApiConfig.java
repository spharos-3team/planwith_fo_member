package com.planwith.planwith_fo_member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	public static final String GATEWAY_USER_ID_SCHEME = "X-Auth-User-Id";

	@Bean
	public OpenAPI planwithFoMemberOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PlanWith planwith-fo-member API")
						.description("""
								Member API. Access Token 검증은 Gateway.
								로컬/Swagger에서 `/members/me/**` 호출 시 우측 상단 Authorize에
								memberUuid(로그인 응답 `user.userId`)를 넣으면 `X-Auth-User-Id` 헤더로 전달된다.
								""")
						.version("v1"))
				.components(new Components()
						.addSecuritySchemes(GATEWAY_USER_ID_SCHEME, new SecurityScheme()
								.name("X-Auth-User-Id")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.description("Gateway가 넘기는 사용자 UUID. 로컬에서는 로그인 응답의 user.userId를 그대로 입력")));
	}
}
