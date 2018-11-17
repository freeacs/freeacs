package com.github.freeacs.web.app.page.user;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** The Class UserGroupController. */
// @Controller
// @RequestMapping("/app/group")
public class UserGroupController extends PermissionController {

  /** The name map. */
  private Map<String, UserGroupModel> nameMap = new HashMap<>();

  /** The id map. */
  private Map<Integer, UserGroupModel> idMap = new HashMap<>();

  /** The max id. */
  private Integer maxId = 0;

  /**
   * Gets the name map.
   *
   * @return the name map
   */
  Map<String, UserGroupModel> getNameMap() {
    return Collections.unmodifiableMap(nameMap);
  }

  /**
   * Gets the id map.
   *
   * @return the id map
   */
  Map<Integer, UserGroupModel> getIdMap() {
    return Collections.unmodifiableMap(idMap);
  }

  /** Instantiates a new user group controller. */
  public UserGroupController() {
    create(new UserGroupModel("Special"));
    create(new UserGroupModel("Support"));
    create(new UserGroupModel("NotAdmin"));
  }

  /**
   * Details.
   *
   * @param name the name
   * @return the user group model
   */
  // @RequestMapping(value = "/{name}", method = RequestMethod.GET)
  // @ResponseBody
  public UserGroupModel details(/*@PathVariable*/ String name) {
    if (nameMap.get(name) == null) {
      throw new ResourceNotFoundException();
    }
    return nameMap.get(name);
  }

  /**
   * Delete.
   *
   * @param name the name
   */
  // @RequestMapping(value = "/{name}", method = RequestMethod.DELETE)
  // @ResponseBody
  public void delete(/*@PathVariable*/ String name) {
    if (nameMap.get(name) == null) {
      throw new ResourceNotFoundException();
    }
    UserGroupModel toRemove = nameMap.get(name);
    nameMap.remove(toRemove.getName());
    idMap.remove(toRemove.getId());
  }

  /**
   * List.
   *
   * @return the map
   */
  // @RequestMapping(value = "/list", method = RequestMethod.GET)
  // @ResponseBody
  public Map<String, Object> list() {
    Map<String, Object> map = new HashMap<>();
    map.put("groups", nameMap.values().toArray(new UserGroupModel[] {}));
    return map;
  }

  /**
   * Creates the.
   *
   * @param details the details
   * @return the user group model
   */
  // @RequestMapping(method = RequestMethod.POST)
  // @ResponseBody
  public UserGroupModel create(/*@RequestBody*/ UserGroupModel details) {
    if (nameMap.containsKey(details.getName())) {
      throw new NotAllowedException("UserGroup exists with that name");
    }
    details.setId(++maxId);
    idMap.put(details.getId(), details);
    nameMap.put(details.getName(), details);
    return details;
  }

  /**
   * Update.
   *
   * @param details the details
   * @return the user group model
   */
  // @RequestMapping(method = RequestMethod.PUT)
  // @ResponseBody
  public UserGroupModel update(/*@RequestBody */ UserGroupModel details) {
    if (details.getId() == null || idMap.get(details.getId()) == null) {
      throw new ResourceNotFoundException();
    }
    idMap.put(details.getId(), details);
    nameMap.put(details.getName(), details);
    return details;
  }
}
