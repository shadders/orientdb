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

package com.orientechnologies.binary.old;

import java.util.HashMap;

/**
 * 
 * @author Steve Coughlan
 *
 */
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
		if (interned == null && string != null) {
			INTERNS.put(string, string);
			return string;
		}
		return interned;
	}

}
