package at.ac.tuwien.kr.alpha.config;

/**
 * Wrapper type for AlphaConfig and InputConfig.
 */
public class AlphaConfig {

	private SystemConfig alphaConfig;
	private InputConfig inputConfig;

	public SystemConfig getAlphaConfig() {
		return this.alphaConfig;
	}

	public void setAlphaConfig(SystemConfig alphaConfig) {
		this.alphaConfig = alphaConfig;
	}

	public InputConfig getInputConfig() {
		return this.inputConfig;
	}

	public void setInputConfig(InputConfig inputConfig) {
		this.inputConfig = inputConfig;
	}

}
