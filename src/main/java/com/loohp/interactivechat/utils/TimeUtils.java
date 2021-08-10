package com.loohp.interactivechat.utils;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
	
	public static final DecimalFormat FORMAT = new DecimalFormat("00");
	
	public static String getReadableTimeBetween(long from, long to) {
		LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(from), ZoneId.systemDefault());
		LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(to), ZoneId.systemDefault());
		long hrs = ChronoUnit.HOURS.between(start, now);
		long mins = ChronoUnit.MINUTES.between(start, now);
		long secs = ChronoUnit.SECONDS.between(start, now);
		return (hrs == 0 ? "" : (hrs + ":")) + FORMAT.format(mins % 60) + ":" + FORMAT.format(secs % 60);
	}

}
