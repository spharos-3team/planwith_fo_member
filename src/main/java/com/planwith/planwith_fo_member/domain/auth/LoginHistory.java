package com.planwith.planwith_fo_member.domain.auth;

import java.time.Instant;

public class LoginHistory {

	private final Long id;
	private final Long actorId;
	private final ActorType actorType;
	private final String ipAddress;
	private final String userAgent;
	private final String deviceInfo;
	private final Instant createdAt;

	public LoginHistory(
			Long id,
			Long actorId,
			ActorType actorType,
			String ipAddress,
			String userAgent,
			String deviceInfo,
			Instant createdAt
	) {
		if (actorId == null) {
			throw new IllegalArgumentException("actorId is required");
		}
		if (actorType == null) {
			throw new IllegalArgumentException("actorType is required");
		}
		this.id = id;
		this.actorId = actorId;
		this.actorType = actorType;
		this.ipAddress = ipAddress;
		this.userAgent = userAgent;
		this.deviceInfo = deviceInfo;
		this.createdAt = createdAt == null ? Instant.now() : createdAt;
	}

	public static LoginHistory user(Long actorId, String ipAddress, String userAgent) {
		return new LoginHistory(
				null,
				actorId,
				ActorType.USER,
				ipAddress,
				userAgent,
				DeviceInfo.fromUserAgent(userAgent),
				Instant.now()
		);
	}

	public Long getId() {
		return id;
	}

	public Long getActorId() {
		return actorId;
	}

	public ActorType getActorType() {
		return actorType;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
