package at.ac.tuwien.kr.alpha.config;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

@FunctionalInterface
public interface IOptionHandler<T> {

	void handleOption(Option opt, T dest) throws ParseException;

}
