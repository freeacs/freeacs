package com.github.freeacs.web.app.page.search;

import com.github.freeacs.dbi.Unit;
import java.util.ArrayList;
import java.util.List;

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
