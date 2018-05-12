package com.github.freeacs.web.app.page.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * The Class NotAllowedException.
 */
@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class NotAllowedException extends RuntimeException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new not allowed exception.
	 *
	 * @param s the s
	 */
	public NotAllowedException(String s){
		super(s);
	}
}

