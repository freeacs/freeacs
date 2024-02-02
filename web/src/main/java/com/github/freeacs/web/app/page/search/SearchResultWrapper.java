package com.github.freeacs.web.app.page.search;

import com.github.freeacs.dbi.Unit;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SearchResultWrapper {
  public Unit unit;
  public List<String> displayables = new ArrayList<>();

}
