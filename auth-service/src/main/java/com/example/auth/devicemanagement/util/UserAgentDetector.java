package com.example.auth.devicemanagement.util;

import com.example.auth.devicemanagement.device.entity.DeviceType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserAgentDetector {

    private static final Pattern TABLET_PATTERN =
            Pattern.compile("iPad|Tablet|(?=.*Android)(?!.*Mobile)", Pattern.CASE_INSENSITIVE);

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("Mobile|iPhone|Android.*Mobile|Windows Phone|BlackBerry", Pattern.CASE_INSENSITIVE);

    public DeviceType detectDeviceType(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return DeviceType.OTHER;
        }
        if (TABLET_PATTERN.matcher(userAgent).find()) {
            return DeviceType.TABLET;
        }
        if (MOBILE_PATTERN.matcher(userAgent).find()) {
            return DeviceType.MOBILE;
        }
        if (userAgent.toLowerCase().contains("windows")
                || userAgent.toLowerCase().contains("mac os")
                || userAgent.toLowerCase().contains("linux")) {
            return DeviceType.DESKTOP;
        }
        return DeviceType.OTHER;
    }

    public String detectOperatingSystem(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Unknown";
        }

        if (userAgent.contains("Windows NT 10.0")) return "Windows 10/11";
        if (userAgent.contains("Windows NT 6.3")) return "Windows 8.1";
        if (userAgent.contains("Windows NT 6.1")) return "Windows 7";
        if (userAgent.contains("Windows")) return "Windows";

        Matcher macMatcher = Pattern.compile("Mac OS X (\\d+[_.]\\d+)").matcher(userAgent);
        if (macMatcher.find()) return "macOS " + macMatcher.group(1).replace('_', '.');
        if (userAgent.contains("Macintosh")) return "macOS";

        Matcher iosMatcher = Pattern.compile("OS (\\d+[_.]\\d+) like Mac OS X").matcher(userAgent);
        if (iosMatcher.find()) return "iOS " + iosMatcher.group(1).replace('_', '.');

        Matcher androidMatcher = Pattern.compile("Android (\\d+(\\.\\d+)?)").matcher(userAgent);
        if (androidMatcher.find()) return "Android " + androidMatcher.group(1);

        if (userAgent.contains("Linux")) return "Linux";

        return "Unknown";
    }

    public String detectDeviceName(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Unknown Device";
        }
        return detectBrowser(userAgent) + " on " + detectOperatingSystem(userAgent);
    }

    private String detectBrowser(String userAgent) {
        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("OPR/") || userAgent.contains("Opera")) return "Opera";
        if (userAgent.contains("Chrome/") && !userAgent.contains("Edg/")) return "Chrome";
        if (userAgent.contains("Firefox/")) return "Firefox";
        if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) return "Safari";
        return "Unknown Browser";
    }
}