package at.ac.tuwien.kr.alpha.core.solver;

public class CumulativeStopWatch {
	private long startTime;
	private long runtime;
	private boolean running;

	public void start() {
		if (!running) {
			startTime = System.nanoTime();
			running = true;
		}
	}

	public void stop() {
		long currentTime = System.nanoTime();
		runtime += (currentTime - startTime);
		running = false;
	}

	public long getRuntime() {
		return runtime;
	}
}
