package at.ac.tuwien.kr.alpha.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.core.actions.AbstractActionImplementationProvider;

public class MockActionImplementationProvider extends AbstractActionImplementationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MockActionImplementationProvider.class);

	@Override
	protected OutputStream getStdoutStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected InputStream getStdinStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected OutputStream getFileOutputStream(String path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected InputStream getInputStream(String path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
