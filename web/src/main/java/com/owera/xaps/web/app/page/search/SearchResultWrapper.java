package com.owera.xaps.web.app.page.search;

import java.util.*;
import com.owera.xaps.dbi.*;

public class SearchResultWrapper {

	public Unit unit;
	public List<String> displayables = new ArrayList<>();

	
	public Unit getUnit() {
		return unit;
	}
	
	public List<String> getDisplayables() {
		return displayables;
	}
}

