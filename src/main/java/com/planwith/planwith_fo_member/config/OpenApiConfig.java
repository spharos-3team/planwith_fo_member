package com.planwith.planwith_fo_member.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	public static final String BEARER_SCHEME = "Bearer";
	public static final String GATEWAY_USER_ID_SCHEME = "X-Auth-User-Id";

	@Value("${app.gateway.public-url:/}")
	private String gatewayPublicUrl;

	@Bean
	public OpenAPI planwithFoMemberOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PlanWith planwith-fo-member API")
						.description("""
								Member API. Access Token 검증은 Gateway.
								브라우저·다른 PC는 Gateway `:8000`만 호출한다.
								Gateway Swagger에서 팔로우/`me` API는 Authorize → **Bearer**에
								로그인 응답 `accessToken`을 넣는다. (`Authorization: Bearer ...`)
								Gateway는 클라이언트가 보낸 `X-Auth-User-Id`를 제거한다.
								Member를 `:8082`로 직접 칠 때만 `X-Auth-User-Id`에 memberUuid를 넣는다.
								""")
						.version("v1"))
				.servers(List.of(new Server()
						.url(gatewayPublicUrl)
						.description("API Gateway")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
				.addSecurityItem(new SecurityRequirement().addList(GATEWAY_USER_ID_SCHEME))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Gateway `:8000` 호출용. 로그인 응답 accessToken"))
						.addSecuritySchemes(GATEWAY_USER_ID_SCHEME, new SecurityScheme()
								.name("X-Auth-User-Id")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.description("Member 직접 호출(`:8082`)용. 로그인 응답 user.userId. Gateway 경유 시 이 헤더는 버려진다")));
	}
}
