package com.planwith.planwith_fo_member.domain.auth;

import java.util.Locale;

public final class DeviceInfo {

	public static final String MOBILE = "Mobile";
	public static final String DESKTOP = "Desktop";
	public static final String TABLET = "Tablet";
	public static final String UNKNOWN = "Unknown";

	private DeviceInfo() {
	}

	public static String fromUserAgent(String userAgent) {
		if (userAgent == null || userAgent.isBlank()) {
			return UNKNOWN;
		}
		String ua = userAgent.toLowerCase(Locale.ROOT);
		if (isTablet(ua)) {
			return TABLET;
		}
		if (isMobile(ua)) {
			return MOBILE;
		}
		if (isDesktop(ua)) {
			return DESKTOP;
		}
		return UNKNOWN;
	}

	private static boolean isTablet(String ua) {
		return ua.contains("ipad")
				|| ua.contains("tablet")
				|| (ua.contains("android") && !ua.contains("mobile"));
	}

	private static boolean isMobile(String ua) {
		return ua.contains("mobi")
				|| ua.contains("iphone")
				|| ua.contains("ipod")
				|| ua.contains("android")
				|| ua.contains("windows phone");
	}

	private static boolean isDesktop(String ua) {
		return ua.contains("windows")
				|| ua.contains("macintosh")
				|| ua.contains("linux")
				|| ua.contains("x11")
				|| ua.contains("cros");
	}
}
