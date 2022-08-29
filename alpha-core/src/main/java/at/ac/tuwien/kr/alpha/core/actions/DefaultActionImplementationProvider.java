package at.ac.tuwien.kr.alpha.core.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DefaultActionImplementationProvider extends AbstractActionImplementationProvider {

	@Override
	protected OutputStream getStdoutStream() {
		return System.out;
	}

	@Override
	protected InputStream getStdinStream() {
		return System.in;
	}

	@Override
	protected OutputStream getFileOutputStream(String path) throws IOException {
		return Files.newOutputStream(Paths.get(path), StandardOpenOption.APPEND);
	}

	@Override
	protected InputStream getInputStream(String path) throws IOException {
		return Files.newInputStream(Paths.get(path));
	}
	
}
