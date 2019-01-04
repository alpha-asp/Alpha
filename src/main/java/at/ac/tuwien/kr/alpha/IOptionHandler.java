package at.ac.tuwien.kr.alpha;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

@FunctionalInterface
public interface IOptionHandler {

	void handleOption(Option opt, AlphaConfig dest) throws ParseException;

}
