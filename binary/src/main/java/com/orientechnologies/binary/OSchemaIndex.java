package com.orientechnologies.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.binary.util.CaselessString;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;

/**
 * Placeholder for all known schemas.
 * 
 * @author Steve Coughlan
 *
 */
public class OSchemaIndex {

			
	private static List<OSchemaSet> schemas = new ArrayList();
	private static Map<CaselessString, OSchemaSet> schemaMap = new HashMap();
	
	public static final OSchemaSet SCHEMALESS_SET = OSchemaSet.newSchemaSet("");
	public static final OSchemaVersion SCHEMALESS = SCHEMALESS_SET.currentSchema();
	
	static {
		SCHEMALESS.makeImmutable();
	}
	
	public static OSchemaSet getSchemaSetForId(int classId) {
		return schemas.get(classId);
	}
	
	public static OSchemaVersion getCurrentSchemaForId(int classId) {
		OSchemaSet set = schemas.get(classId);
		return set == null ? null : set.currentSchema();
	}
	
	public static OSchemaVersion getCurrentSchemaForName(String name) {
		OSchemaSet set = schemaMap.get(new CaselessString(name));
		return set == null ? null : set.currentSchema(); 
	}
	
	public static OSchemaSet getSchemaSetForName(String name) {
		return schemaMap.get(new CaselessString(name));
	}
	
//	public static void addNewClass(OSchemaVersion clazz) {
//		OSchemaSet set = new OSchemaSet(clazz.getName(), Collections.singletonList(clazz));
//		addClass(set);
//	}
	
	/**
	 * Probably only used when deserializing schema from disk on db startup
	 * @param clazz
	 */
	public static void addClass(OSchemaSet clazz) {
		
		if (schemaMap.containsKey(clazz.getClassName()))
			throw new RuntimeException("Class cannot be to full schema set twice");
		
		clazz.setSchemaId(schemas.size());
		schemas.add(clazz);
		schemaMap.put(clazz.getClassName(), clazz);
	}
	
}
