package at.ac.tuwien.kr.alpha.config;

/**
 * Wrapper type for AlphaConfig and InputConfig.
 */
public class AlphaContext {

	private AlphaConfig alphaConfig;
	private InputConfig inputConfig;

	public AlphaConfig getAlphaConfig() {
		return this.alphaConfig;
	}

	public void setAlphaConfig(AlphaConfig alphaConfig) {
		this.alphaConfig = alphaConfig;
	}

	public InputConfig getInputConfig() {
		return this.inputConfig;
	}

	public void setInputConfig(InputConfig inputConfig) {
		this.inputConfig = inputConfig;
	}

}
