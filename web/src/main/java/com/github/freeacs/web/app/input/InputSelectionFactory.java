package com.github.freeacs.web.app.input;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.Group;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.Trigger;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Static factory methods for creating instances that extends InputSelectionModel.
 *
 * @author Jarl Andre Hubenthal
 */
public class InputSelectionFactory {
  /**
   * Gets the unittype selection.
   *
   * @param unittypeInput the unittype input
   * @param acs the xaps
   * @return the unittype selection
   */
  public static DropDownSingleSelect<Unittype> getUnittypeSelection(Input unittypeInput, ACS acs) {
    Unittype unittype = null;
    if (unittypeInput.notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
      unittype = acs.getUnittype(unittypeInput.getString());
    }
    List<Unittype> items = Arrays.asList(acs.getUnittypes().getUnittypes());
    return new DropDownSingleSelect<>(unittypeInput, unittype, items);
  }

  public static DropDownSingleSelect<Trigger> getTriggerSelection(
      Input triggerInput, Unittype unittype) {
    Trigger trigger = null;
    if (unittype != null && triggerInput.notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
      trigger = unittype.getTriggers().getById(triggerInput.getInteger());
    }
    List<Trigger> items = Collections.emptyList();
    if (unittype != null) {
      items = Arrays.asList(unittype.getTriggers().getTriggers());
    }
    return new DropDownSingleSelect<>(triggerInput, trigger, items);
  }

  public static DropDownSingleSelect<Group> getGroupSelection(
      Input groupInput, Unittype unittype) {
    Group group = null;
    if (unittype != null && groupInput.notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
      group = unittype.getGroups().getByName(groupInput.getString());
    }
    List<Group> items = Collections.emptyList();
    if (unittype != null) {
      items = Arrays.asList(unittype.getGroups().getGroups());
    }
    return new DropDownSingleSelect<>(groupInput, group, items);
  }

  public static DropDownSingleSelect<Profile> getProfileSelection(
      Input profileInput, Input unittypeInput, ACS acs) {
    List<Profile> items = new ArrayList<>();
    Unittype unittype = acs.getUnittype(unittypeInput.getString());
    if (unittype != null) {
      return getProfileSelection(profileInput, unittype);
    } else {
      return new DropDownSingleSelect<>(profileInput, null, items);
    }
  }

  public static DropDownSingleSelect<Profile> getProfileSelection(
      Input profileInput, Unittype unittype) {
    Profile profile = null;
    List<Profile> items = new ArrayList<>();
    if (unittype != null) {
      items = Arrays.asList(unittype.getProfiles().getProfiles());
      profile = unittype.getProfiles().getByName(profileInput.getString());
    }
    return new DropDownSingleSelect<>(profileInput, profile, items);
  }

  public static <T> CheckBoxGroup<T> getCheckBoxGroup(Input input, List<T> checked, List<T> items) {
    return new CheckBoxGroup<>(input, checked, items);
  }

  public static <T> DropDownSingleSelect<T> getDropDownSingleSelect(
      Input input, T selected, List<T> items) {
    return new DropDownSingleSelect<>(input, selected, items);
  }

  public static DropDownSingleSelect<String> getDropDownSingleSelect(
      Input input, List<String> items) {
    return new DropDownSingleSelect<>(input, input.getString(), items);
  }

  public static <T> DropDownMultiSelect<T> getDropDownMultiSelect(
      Input input, T[] selected, List<T> items) {
    return new DropDownMultiSelect<>(input, selected, items);
  }
}
