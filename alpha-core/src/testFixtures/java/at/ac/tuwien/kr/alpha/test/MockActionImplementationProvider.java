package at.ac.tuwien.kr.alpha.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.core.actions.AbstractActionImplementationProvider;

public class MockActionImplementationProvider extends AbstractActionImplementationProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(MockActionImplementationProvider.class);

	private final ByteArrayOutputStream stdoutMock = new ByteArrayOutputStream();
	private final InputStream stdinMock;
	private Map<String, OutputStream> mockedFileOutputs;
	private Map<String, InputStream> mockedFileInputs;

	public MockActionImplementationProvider(String inputBuffer) {
		stdinMock = IOUtils.toInputStream(inputBuffer, "UTF-8");
	}

	@Override
	protected OutputStream getStdoutStream() {
		return stdoutMock;
	}

	public String getStdoutContent() {
		return stdoutMock.toString();
	}

	public void resetStdoutContent() {
		stdoutMock.reset();
	}

	public void setMockedFileOutputs(Map<String, OutputStream> mockedFileOutputs) {
		this.mockedFileOutputs = mockedFileOutputs;
	}

	public void setMockedFileInputs(Map<String, InputStream> mockedFileInputs) {
		this.mockedFileInputs = mockedFileInputs;
	}

	public Map<String, OutputStream> getMockedFileOutputs() {
		return mockedFileOutputs;
	}

	public Map<String, InputStream> getMockedFileInputs() {
		return mockedFileInputs;
	}

	@Override
	protected InputStream getStdinStream() {
		return stdinMock;
	}

	@Override
	public OutputStream getFileOutputStream(String path) throws IOException {
		if (mockedFileOutputs.containsKey(path)) {
			return mockedFileOutputs.get(path);
		}
		throw new IOException("Path does not exist!");
	}

	@Override
	public InputStream getInputStream(String path) throws IOException {
		if (mockedFileInputs.containsKey(path)) {
			return mockedFileInputs.get(path);
		}
		throw new IOException("Path does not exist!");
	}

}
