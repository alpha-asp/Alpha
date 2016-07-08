package at.ac.tuwien.kr.alpha.grounder.parser;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ListOfParsedObjects extends CommonParsedObject {
	public ArrayList<CommonParsedObject> objects;

	public ListOfParsedObjects() {
		objects = new ArrayList<>();
	}
}
