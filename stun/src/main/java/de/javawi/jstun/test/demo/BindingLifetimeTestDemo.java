/*
 * This file is part of JSTUN. 
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 * 
 * This software is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in this distribution.
 */

package de.javawi.jstun.test.demo;

import de.javawi.jstun.test.BindingLifetimeTest;

public class BindingLifetimeTestDemo {

	public static void main(String args[]) {
		try {
			BindingLifetimeTest test = new BindingLifetimeTest("jstun.javawi.de", 3478);
			// iphone-stun.freenet.de:3478
			// larry.gloo.net:3478
			// stun.xten.net:3478
			test.test();
			boolean continueWhile = true;
			while (continueWhile) {
				Thread.sleep(5000);
				if (test.getLifetime() != -1) {
					System.out.println("Lifetime: " + test.getLifetime() + " Finished: " + test.isCompleted());
					if (test.isCompleted())
						continueWhile = false;
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
