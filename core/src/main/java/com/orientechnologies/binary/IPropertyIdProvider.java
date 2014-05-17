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

//package com.orientechnologies.binary;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import com.orientechnologies.binary.util.CaselessString;
//
///**
// * The per class index of field names
// * 
// * TODO add proper synchronization to block reads during modify.
// * 
// * TODO Serialize the names to disk so we can consistently map nameId to name
// * across JVMs.
// * 
// * TODO handle case insensitivity, note that by calling toLowerCase we lose
// * identity but clients should be obtaining identity before using. Also note
// * that by storing a lowercase version as the identity we lose the client's
// * declared case when returning. Perhaps need to store this oClassInstance? If we allow
// * multiple cased versions of the same string then fieldName != FIELDNAME which
// * could cause all sorts of problems.
// * 
// * TODO handle tracking of field name uses so we know when we can delete one from the list
// * 
// * @author Steve Coughlan
// * 
// */
//public class IPropertyIdProvider {
//
//	/**
//	 * Global collection of names to ensure we use an identity INSTANCE.
//	 */
//	//private static final THashMap<Object, CaselessString> ALL_NAMES = new THashMap<Object, CaselessString>();
//	private static final HashMap<Object, String> ALL_NAMES = new HashMap<Object, String>();
//
//
//	/**
//	 * Per class provider map
//	 */
//	private static final Map<String, IPropertyIdProvider> PROVIDERS = new HashMap();
//
//	private final List<String> names = new ArrayList();
//
//	//private final TObjectIntMap<CaselessString> ids = new TObjectIntHashMap<CaselessString>(10, 0.5f, -1);
//	private final Map<String, Integer> ids = new HashMap<String, Integer>();
//
//	/**
//	 * To maintain index order any properties deleted from the OClassVersion are simply
//	 * marked as available space (a hole) and reused for new properties.
//	 */
//	//private TIntLinkedList holes = new TIntLinkedList();
//	private LinkedList<Integer> holes = new LinkedList();
//
//	
//	/**
//	 * Used for internal serialization
//	 */
//	List<String> getNames() {
//		return null;
//	}
//	
//	/**
//	 * Used for internal serialization
//	 */
//	List<String> getHoles() {
//		return null;
//	}
//	
//	/**
//	 * returns a singleton INSTANCE per OrientDB class
//	 * 
//	 * @param clazz
//	 *            the OrientDB class (case insensitive).
//	 * @return
//	 */
//	public static synchronized IPropertyIdProvider getForClass(String clazz) {
//		clazz = clazz.toLowerCase();
//		IPropertyIdProvider provider = PROVIDERS.get(clazz);
//		if (provider == null) {
//			provider = new IPropertyIdProvider();
//			PROVIDERS.put(clazz, provider);
//		}
//		return provider;
//	}
//
//	/**
//	 * Provides the identity copy of the name for this id.
//	 * 
//	 * @param id
//	 * @return
//	 */
//	public String nameFor(int id) {
//		String string = names.get(id);
//		return string;
//	}
//	
////	/**
////	 * Provides the original cased String for this id.
////	 * 
////	 * @param id
////	 * @return
////	 */
////	public String casedNameFor(int id) {
////		CaselessString string = names.get(id);
////		return string == null ? null : string.getCased();
////	}
//
//	/**
//	 * Provides the nameId for the given property name
//	 * 
//	 * @param name
//	 * @return
//	 */
//	public int idFor(String name) {
//		Integer id = ids.get(name);
////		if (id == null) {
////			id = ids.get(new CaselessString(name));
////		}
//		return id == null ? newName(name) : id;
//	}
//
//	public synchronized int newName(String name) {
//		int id = -1;
//		//CaselessString caseless = identityCaseless(name);
//
//		if (holes.isEmpty()) {
//			id = names.size();
//			names.add(name);
//		} else {
//			id = holes.remove(0);
//			names.set(id, name);
//		}
//		ids.put(name, id);
//
//		return id;
//	}
//
//	/**
//	 * returns the identity copy of the field name String.
//	 * 
//	 * @param name
//	 * @return
//	 */
//	public static synchronized String identity(String name) {
//		String identity = ALL_NAMES.get(name);
//		if (identity == null) {
////			CaselessString key = new CaselessString(name);
////			if (!key.isLowercase()) {
////				identity = ALL_NAMES.get(key);
////			}
//			
////			if (identity == null) {
//				ALL_NAMES.put(name, name);
//				identity = name;
////			}
//		}
//		return identity;
//	}
//	
////	/**
////	 * returns the identity copy of the field name String.
////	 * 
////	 * @param name
////	 * @return
////	 */
////	public static synchronized CaselessString identityCaseless(String name) {
////		CaselessString identity = ALL_NAMES.get(name);
////		if (identity == null) {
////			CaselessString key = new CaselessString(name);
////			if (!key.isLowercase()) {
////				identity = ALL_NAMES.get(key);
////			}
////			
////			if (identity == null) {
////				ALL_NAMES.put(key, key);
////				identity = key;
////			}
////		}
////		return identity;
////	}
//
//}
