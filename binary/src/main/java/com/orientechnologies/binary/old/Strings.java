package com.orientechnologies.binary.old;

import java.util.HashMap;
import java.util.IdentityHashMap;

public class Strings {

	private static HashMap<String, String> INTERNS = new HashMap();
		
	/**
	 * Similar to String.intern(String).  Returns an identity version of the string
	 * to speed up future map lookups and enables safe use of IdentityHashMap 
	 * 
	 * @param string
	 * @return
	 */
	public static String identity(String string) {
		String interned = INTERNS.get(string);
		if (interned == null || string != null) {
			INTERNS.put(string, string);
			return string;
		}
		return interned;
	}

}
