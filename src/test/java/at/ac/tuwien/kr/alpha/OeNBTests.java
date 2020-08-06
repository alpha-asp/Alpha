package at.ac.tuwien.kr.alpha;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static at.ac.tuwien.kr.alpha.Main.main;

/**
 * Copyright (c) 2020, the Alpha Team.
 */
public class OeNBTests {

	@Test
	@Ignore
	public void stratifiedEvaluation() throws InterruptedException {
		Logger logger = (Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		logger.setLevel(Level.INFO);
		//Thread.sleep(10000);
		String fileDir = "/home/as/projects/bankenabwicklung/source/";
		main(new String[]{//"--stats",
			//"-n", "10",
			"-f", "knotenFOLTF", "-f", "knotenOK",
			"-i", fileDir + "feldRegeln.txt",
			"-i", fileDir + "netz.txt",
			"-i", fileDir + "randGraph.dl"
		});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}
}
