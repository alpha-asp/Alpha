package at.ac.tuwien.kr.alpha.core.actions;

import java.io.BufferedReader;

public class InputStreamHandle implements Comparable<InputStreamHandle> {

	private final int id;
	private final BufferedReader stream;

	public InputStreamHandle(int id, BufferedReader stream) {
		this.id = id;
		this.stream = stream;
	}

	public int getId() {
		return id;
	}

	public BufferedReader getStream() {
		return stream;
	}

	@Override
	public String toString() {
		return "inputStream-" + id;
	}

	@Override
	public int compareTo(InputStreamHandle other) {
		return this.id - other.id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (!(o instanceof InputStreamHandle)) {
			return false;
		}
		InputStreamHandle other = (InputStreamHandle) o;
		return this.id == other.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

}
