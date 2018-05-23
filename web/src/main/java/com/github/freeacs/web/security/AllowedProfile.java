package com.github.freeacs.web.security;

/**
 * Always connected to an AllowedUnittype.
 * 
 * This class is mutable!
 * 
 * Represents an allowed profile.
 * 
 * @author Jarl Andre Hubenthal
 */
public class AllowedProfile{
	/** The id. */
	private Integer id;
	
	/** OR */
	
	/** The name. */
	private String name;
	
	/**
	 * Instantiates a new allowed profile.
	 *
	 * @param id the id
	 */
	public AllowedProfile(Integer id){
		this.id=id;
	}
	
	/**
	 * Instantiates a new allowed profile.
	 *
	 * @param name the name
	 */
	public AllowedProfile(String name){
		this.name=name;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
}