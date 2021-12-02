package at.ac.tuwien.kr.alpha.api.config;

/**
 * Wrapper type for {@link SystemConfig} and {@link InputConfig}.
 */
public class AlphaConfig {

	private SystemConfig systemConfig;
	private InputConfig inputConfig;

	public SystemConfig getSystemConfig() {
		return this.systemConfig;
	}

	public void setSystemConfig(SystemConfig alphaConfig) {
		this.systemConfig = alphaConfig;
	}

	public InputConfig getInputConfig() {
		return this.inputConfig;
	}

	public void setInputConfig(InputConfig inputConfig) {
		this.inputConfig = inputConfig;
	}

}
