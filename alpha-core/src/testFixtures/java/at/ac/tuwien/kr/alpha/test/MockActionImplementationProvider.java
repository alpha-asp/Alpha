package at.ac.tuwien.kr.alpha.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.core.actions.AbstractActionImplementationProvider;

public class MockActionImplementationProvider extends AbstractActionImplementationProvider {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(MockActionImplementationProvider.class);

	private ByteArrayOutputStream stdoutMock = new ByteArrayOutputStream();
	private ByteArrayInputStream stdinMock;
	private Map<String, OutputStream> mockedFileOutputs;
	private Map<String, InputStream> mockedFileInputs;

	public MockActionImplementationProvider() {
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

	public void setMockInput(String input) {
		stdinMock = new ByteArrayInputStream(input.getBytes());
	}

	public void setMockedFileOutputs(Map<String, OutputStream> mockedFileOutputs) {
		this.mockedFileOutputs = mockedFileOutputs;
	}

	public void setMockedFileInputs(Map<String, InputStream> mockedFileInputs) {
		this.mockedFileInputs = mockedFileInputs;
	}

	@Override
	protected InputStream getStdinStream() {
		return stdinMock;
	}

	@Override
	protected OutputStream getFileOutputStream(String path) throws IOException {
		if (mockedFileOutputs.containsKey(path)) {
			return mockedFileOutputs.get(path);
		}
		throw new IOException("Path does not exist!");
	}

	@Override
	protected InputStream getInputStream(String path) throws IOException {
		if (mockedFileInputs.containsKey(path)) {
			return mockedFileInputs.get(path);
		}
		throw new IOException("Path does not exist!");
	}

}
