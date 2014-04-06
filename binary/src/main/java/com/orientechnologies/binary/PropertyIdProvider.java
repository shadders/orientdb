package com.orientechnologies.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.orientechnologies.binary.util.CaselessString;

/**
 * The per class index of field names
 * 
 * TODO add proper synchronization to block reads during modify.
 * 
 * TODO Serialize the names to disk so we can consistently map nameId to name
 * across JVMs.
 * 
 * TODO handle case insensitivity, note that by calling toLowerCase we lose
 * identity but clients should be obtaining identity before using. Also note
 * that by storing a lowercase version as the identity we lose the client's
 * declared case when returning. Perhaps need to store this oClassInstance? If we allow
 * multiple cased versions of the same string then fieldName != FIELDNAME which
 * could cause all sorts of problems.
 * 
 * @author Steve Coughlan
 * 
 */
public class PropertyIdProvider {

	/**
	 * Global collection of names to ensure we use an identity instance.
	 */
	//private static final THashMap<Object, CaselessString> ALL_NAMES = new THashMap<Object, CaselessString>();
	private static final HashMap<Object, CaselessString> ALL_NAMES = new HashMap<Object, CaselessString>();


	/**
	 * Per class provider map
	 */
	private static final Map<String, PropertyIdProvider> PROVIDERS = new HashMap();

	private final List<CaselessString> names = new ArrayList();

	//private final TObjectIntMap<CaselessString> ids = new TObjectIntHashMap<CaselessString>(10, 0.5f, -1);
	private final Map<CaselessString, Integer> ids = new HashMap<CaselessString, Integer>();

	/**
	 * To maintain index order any properties deleted from the OSchemaVersion are simply
	 * marked as available space (a hole) and reused for new properties.
	 */
	//private TIntLinkedList holes = new TIntLinkedList();
	private LinkedList<Integer> holes = new LinkedList();

	/**
	 * returns a singleton instance per OrientDB class
	 * 
	 * @param clazz
	 *            the OrientDB class (case insensitive).
	 * @return
	 */
	public static synchronized PropertyIdProvider getForClass(String clazz) {
		clazz = clazz.toLowerCase();
		PropertyIdProvider provider = PROVIDERS.get(clazz);
		if (provider == null) {
			provider = new PropertyIdProvider();
			PROVIDERS.put(clazz, provider);
		}
		return provider;
	}

	/**
	 * Provides the identity copy of the name for this id.
	 * 
	 * @param id
	 * @return
	 */
	public String nameFor(int id) {
		CaselessString string = names.get(id);
		return string == null ? null : string.getCaseless();
	}
	
	/**
	 * Provides the original cased String for this id.
	 * 
	 * @param id
	 * @return
	 */
	public String casedNameFor(int id) {
		CaselessString string = names.get(id);
		return string == null ? null : string.getCaseless();
	}

	/**
	 * Provides the nameId for the given property name
	 * 
	 * @param name
	 * @return
	 */
	public int idFor(String name) {
		Integer id = ids.get(name);
		if (id == null) {
			id = ids.get(new CaselessString(name));
		}
		return id == null ? newName(name) : id;
	}

	public synchronized int newName(String name) {
		int id = -1;
		CaselessString caseless = identityCaseless(name);

		if (holes.isEmpty()) {
			id = names.size();
			names.add(caseless);
		} else {
			id = holes.remove(0);
			names.set(id, caseless);
		}
		ids.put(caseless, id);

		return id;
	}

	/**
	 * returns the identity copy of the field name String.
	 * 
	 * @param name
	 * @return
	 */
	public static synchronized String identity(String name) {
		CaselessString identity = ALL_NAMES.get(name);
		if (identity == null) {
			CaselessString key = new CaselessString(name);
			if (!key.isLowercase()) {
				identity = ALL_NAMES.get(key);
			}
			
			if (identity == null) {
				ALL_NAMES.put(key, key);
				identity = key;
			}
		}
		return identity.getCaseless();
	}
	
	/**
	 * returns the identity copy of the field name String.
	 * 
	 * @param name
	 * @return
	 */
	public static synchronized CaselessString identityCaseless(String name) {
		CaselessString identity = ALL_NAMES.get(name);
		if (identity == null) {
			CaselessString key = new CaselessString(name);
			if (!key.isLowercase()) {
				identity = ALL_NAMES.get(key);
			}
			
			if (identity == null) {
				ALL_NAMES.put(key, key);
				identity = key;
			}
		}
		return identity;
	}

}
