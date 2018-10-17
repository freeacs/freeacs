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
    return new DropDownSingleSelect<Unittype>(unittypeInput, unittype, items);
  }

  /**
   * Gets the trigger selection.
   *
   * @param triggerInput
   * @param unittype
   * @param acs
   * @return
   */
  public static DropDownSingleSelect<Trigger> getTriggerSelection(
      Input triggerInput, Unittype unittype, ACS acs) {
    Trigger trigger = null;
    if (unittype != null && triggerInput.notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
      trigger = unittype.getTriggers().getById(triggerInput.getInteger());
    }
    List<Trigger> items = Collections.emptyList();
    if (unittype != null) {
      items = Arrays.asList(unittype.getTriggers().getTriggers());
    }
    return new DropDownSingleSelect<Trigger>(triggerInput, trigger, items);
  }

  /**
   * Gets the group selection.
   *
   * @param groupInput the group input
   * @param unittype the unittype
   * @param acs the xaps
   * @return the group selection
   */
  public static DropDownSingleSelect<Group> getGroupSelection(
      Input groupInput, Unittype unittype, ACS acs) {
    Group group = null;
    if (unittype != null && groupInput.notNullNorValue(WebConstants.ALL_ITEMS_OR_DEFAULT)) {
      group = unittype.getGroups().getByName(groupInput.getString());
    }
    List<Group> items = Collections.emptyList();
    if (unittype != null) {
      items = Arrays.asList(unittype.getGroups().getGroups());
    }
    return new DropDownSingleSelect<Group>(groupInput, group, items);
  }

  /**
   * Gets the profile selection.
   *
   * @param profileInput the profile input
   * @param unittypeInput the unittype
   * @param acs the xaps
   * @return the profile selection
   */
  public static DropDownSingleSelect<Profile> getProfileSelection(
      Input profileInput, Input unittypeInput, ACS acs) {
    Profile profile = null;
    List<Profile> items = new ArrayList<>();
    Unittype unittype = acs.getUnittype(unittypeInput.getString());
    if (unittype != null) {
      return getProfileSelection(profileInput, unittype);
    } else {
      return new DropDownSingleSelect<Profile>(profileInput, profile, items);
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
    return new DropDownSingleSelect<Profile>(profileInput, profile, items);
  }

  /**
   * Gets the check box group.
   *
   * @param <T> the generic type
   * @param input the input
   * @param checked the checked
   * @param items the items
   * @return the check box group
   */
  public static <T> CheckBoxGroup<T> getCheckBoxGroup(Input input, List<T> checked, List<T> items) {
    return new CheckBoxGroup<T>(input, checked, items);
  }

  /**
   * Gets the drop down single select.
   *
   * @param <T> the generic type
   * @param input the input
   * @param selected the selected
   * @param items the items
   * @return the drop down single select
   */
  public static <T> DropDownSingleSelect<T> getDropDownSingleSelect(
      Input input, T selected, List<T> items) {
    return new DropDownSingleSelect<T>(input, selected, items);
  }

  /**
   * Gets the String drop down single select.<br>
   * Will automatically try to set the inputs incoming String value as the selected item.
   *
   * @param input the input
   * @param items the items
   * @return the drop down single select
   */
  public static DropDownSingleSelect<String> getDropDownSingleSelect(
      Input input, List<String> items) {
    return new DropDownSingleSelect<String>(input, input.getString(), items);
  }

  /**
   * Gets the drop down multi select.
   *
   * @param <T> the generic type
   * @param input the input
   * @param selected the selected
   * @param items the items
   * @return the drop down multi select
   */
  public static <T> DropDownMultiSelect<T> getDropDownMultiSelect(
      Input input, T[] selected, List<T> items) {
    return new DropDownMultiSelect<T>(input, selected, items);
  }
}
