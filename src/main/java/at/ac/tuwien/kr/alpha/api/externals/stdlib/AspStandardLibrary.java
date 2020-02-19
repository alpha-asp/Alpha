package at.ac.tuwien.kr.alpha.api.externals.stdlib;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.externals.ExternalUtils;
import at.ac.tuwien.kr.alpha.api.externals.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Terms;

/**
 * Collection of methods that can be used as external atoms from ASP programs.
 * Provides commonly used functionality such as basic string operations,
 * datetime handling etc.
 * 
 * All functions exposed by this class are guaranteed to be stateless and
 * side-effect free.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public final class AspStandardLibrary {

	private AspStandardLibrary() {

	}

	/**
	 * Parses a string representing a datetime without time-zone and returns the
	 * year, month, day, hours, minutes and seconds as separate symbolic integer
	 * terms.
	 * Example:
	 * 
	 * <pre>
	 * 		A valid ground instance of the atom &stdlib_datetime_parse[DTSTR, "dd.mm.yyyy hh:MM:ss"](YEAR, MONTH, DAY, HOUR, MIN, SEC)
	 * 		would be: &stdlib_datetime_parse["20.05.2020 01:19:13", "dd.mm.yyyy hh:MM:ss"](2020, 5, 20, 1, 19, 13)
	 * </pre>
	 * 
	 * Timezones are not supported by this function. Datetime values are parsed
	 * using {@link LocalDateTime.parse}.
	 * 
	 * @param datetime a string representing a datetime without time zone
	 *                 information
	 * @param format   a format string that is accepted by {@link DateTimeFormatter}
	 * @return a 6-value integer tuple of format (YEAR, MONTH, DAY, HOUR, MIN, SEC)
	 */
	@Predicate
	public static Set<List<ConstantTerm<Integer>>> stdlib_datetime_parse(String dtstr, String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		LocalDateTime datetime = LocalDateTime.parse(dtstr, formatter);
		List<ConstantTerm<Integer>> terms = Terms.asTermList(
				datetime.getYear(), datetime.getMonth().getValue(), datetime.getDayOfMonth(),
				datetime.getHour(), datetime.getMinute(), datetime.getSecond());
		return ExternalUtils.wrapAsSet(terms);
	}

	@Predicate
	public static boolean stdlib_datetime_is_before(int dt1Year, int dt1Month, int dt1Day, int dt1Hour, int dt1Minute, int dt1Second,
			int dt2Year, int dt2Month, int dt2Day, int dt2Hour, int dt2Minute, int dt2Second) {
		LocalDateTime dt1 = LocalDateTime.of(dt1Year, dt1Month, dt1Day, dt1Hour, dt1Minute, dt1Second);
		LocalDateTime dt2 = LocalDateTime.of(dt2Year, dt2Month, dt2Day, dt2Hour, dt2Minute, dt2Second);
		return dt1.isBefore(dt2);
	}

	@Predicate
	public static Set<List<ConstantTerm<String>>> stdlib_datetime_to_string(int year, int month, int day, int hours, int minutes, int seconds, String format) {
		LocalDateTime datetime = LocalDateTime.of(year, month, day, hours, minutes, seconds);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return ExternalUtils.wrapAsSet(Terms.asTermList(formatter.format(datetime)));
	}

	@Predicate
	public static boolean stdlib_string_matches_regex(String str, String regex) {
		return false;
	}

	@Predicate
	public static Set<List<ConstantTerm<Integer>>> stdlib_string_length(String str) {
		return null;
	}

	@Predicate
	public static Set<List<ConstantTerm<String>>> stdlib_string_concat(String s1, String s2) {
		return null;
	}

}
