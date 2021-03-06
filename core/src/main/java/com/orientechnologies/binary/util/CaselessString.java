/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orientechnologies.binary.util;

/**
 * Wrapper for String that with a case insensitive equals and hashcode for use as map keys.
 * 
 * @author Steve Coughlan
 *
 */
public class CaselessString {

	private final String cased;
	private final String caseless;
	private int hash = 0;
	
	public CaselessString(String cased) {
		this.cased = cased;
		this.caseless = makeCaseLess(cased);
	}
	
	/**
	 * @return the cased
	 */
	public String getCased() {
		return cased;
	}

	/**
	 * @return the caseless
	 */
	public String getCaseless() {
		return caseless;
	}
	
	/**
	 * @return true if the cased string is equal to the caseless version i.e. the original string
	 * was lowercase.
	 */
	public boolean isLowercase() {
		return cased == caseless;
	}

	@Override
	public int hashCode() {
		if (hash == 0)
			hash = caseless.hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other instanceof CaselessString) {
			CaselessString s = (CaselessString) other;
			return caseless.equals(s.caseless);
		}
		if (other instanceof String) {
			String s = (String) other;
			return caseless.equals(s);
		}
		return false;
	}
	
	private String makeCaseLess(String cased) {
		StringBuilder sb = null;
		char c;
		for (int i = 0; i < cased.length(); i++) {
			c = cased.charAt(i);
			if (Character.isUpperCase(c)) {
				sb = new StringBuilder(cased.length());
				sb.append(cased, 0, i);
				break;
			}
			hash = 31 * hash + c;
		}
		if (sb != null) {
			for (int i = sb.length(); i < cased.length(); i++) {
				c = Character.toLowerCase(cased.charAt(i));
				hash = 31 * hash + c;
				sb.append(c);
			}
			return sb.toString();
		}
		return cased;
	}
	
	public String toString() {
		return cased;
	}

}
