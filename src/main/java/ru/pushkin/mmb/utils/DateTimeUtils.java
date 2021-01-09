package ru.pushkin.mmb.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {

	private static final String DEFAULT_TIMESTAMP_FROMAT = "dd/MM/yyyy HH:mm:ss";

	public static String getCurrentTimestamp() {
		return new SimpleDateFormat(DEFAULT_TIMESTAMP_FROMAT).format(new Date());
	}

	public static String getCurrentTimestamp(String formatPattern) {
		return new SimpleDateFormat(formatPattern).format(new Date());
	}

	public static LocalDateTime toLocalDateTime(long utsDate) {
		return LocalDateTime.ofInstant(Instant.ofEpochSecond(utsDate), TimeZone.getDefault().toZoneId());
	}

	public static Date toDate(long utsDate) {
		return Date.from(Instant.ofEpochSecond(utsDate));
	}
}
