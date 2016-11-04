package at.ac.tuwien.kr.alpha;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Util {
	public static <K, V> Map.Entry<K, V> entry(K key, V value) {
		return new AbstractMap.SimpleEntry<>(key, value);
	}

	public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
		return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
	}

	public static <E> void appendDelimited(StringBuilder sb, Iterable<E> iterable) {
		for (Iterator<E> iterator = iterable.iterator(); iterator.hasNext();) {
			sb.append(iterator.next());
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
	}

	public static <E> void appendDelimitedPrefix(StringBuilder sb, String prefix, Iterable<E> iterable) {
		for (Iterator<E> iterator = iterable.iterator(); iterator.hasNext();) {
			sb.append(prefix);
			sb.append(iterator.next());
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
	}
}
