package at.ac.tuwien.kr.alpha.core.actions;

import java.io.OutputStream;

public class OutputStreamHandle implements Comparable<OutputStreamHandle> {

	private final int id;
	private final OutputStream stream;

	public OutputStreamHandle(int id, OutputStream stream) {
		this.id = id;
		this.stream = stream;
	}

	public int getId() {
		return id;
	}

	public OutputStream getStream() {
		return stream;
	}

	@Override
	public String toString() {
		return "outputStream-" + id;
	}

	@Override
	public int compareTo(OutputStreamHandle other) {
		return this.id - other.id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (!(o instanceof OutputStreamHandle)) {
			return false;
		}
		OutputStreamHandle other = (OutputStreamHandle) o;
		return this.id == other.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

}
