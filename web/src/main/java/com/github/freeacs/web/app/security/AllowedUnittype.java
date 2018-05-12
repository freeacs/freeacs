package com.github.freeacs.web.app.security;

/**
 * This class is mutable!
 * 
 * Represents an allowed unittype.
 * 
 * @author Jarl Andre Hubenthal
 */
public class AllowedUnittype {
	
	/** The id. */
	private Integer id;

	/** OR */
	
	/** The name. */
	private String name;
	
	/** AND */
	
	/** The profile. */
	private AllowedProfile profile;
	
	/**
	 * Instantiates a new allowed unittype.
	 *
	 * @param id the id of the unittype
	 */
	public AllowedUnittype(Integer id){
		this.id=id;
	}
	
	/**
	 * Instantiates a new allowed unittype.
	 *
	 * @param name the name of the unittype
	 */
	public AllowedUnittype(String name){
		this.name=name;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id of the unittype
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name of the unittype
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the profile.
	 *
	 * @return the allowed profile
	 */
	public AllowedProfile getProfile() {
		return profile;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the id of the unittype
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Sets the profile.
	 *
	 * @param profileId the profile id
	 */
	public void setProfile(Integer profileId) {
		this.profile = new AllowedProfile(profileId);
	}
}