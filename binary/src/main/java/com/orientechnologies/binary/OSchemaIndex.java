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

			
	private static List<OBinaryClassSet> schemas = new ArrayList();
	private static Map<CaselessString, OBinaryClassSet> schemaMap = new HashMap();
	
	public static final OBinaryClassSet SCHEMALESS_SET = OBinaryClassSet.newSchemaSet("");
	public static final OClassVersion SCHEMALESS = SCHEMALESS_SET.currentSchema();
	
	static {
		SCHEMALESS.makeImmutable();
	}
	
	public static OBinaryClassSet getSchemaSetForId(int classId) {
		return schemas.get(classId);
	}
	
	public static OClassVersion getCurrentSchemaForId(int classId) {
		OBinaryClassSet set = schemas.get(classId);
		return set == null ? null : set.currentSchema();
	}
	
	public static OClassVersion getCurrentSchemaForName(String name) {
		OBinaryClassSet set = schemaMap.get(new CaselessString(name));
		return set == null ? null : set.currentSchema(); 
	}
	
	public static OBinaryClassSet getSchemaSetForName(String name) {
		return schemaMap.get(new CaselessString(name));
	}
	
//	public static void addNewClass(OClassVersion clazz) {
//		OBinaryClassSet set = new OBinaryClassSet(clazz.getName(), Collections.singletonList(clazz));
//		addClass(set);
//	}
	
	/**
	 * Probably only used when deserializing schema from disk on db startup
	 * @param clazz
	 */
	public static void addClass(OBinaryClassSet clazz) {
		
		if (schemaMap.containsKey(clazz.getClassName()))
			throw new RuntimeException("Class cannot be to full schema set twice");
		
		clazz.setSchemaId(schemas.size());
		schemas.add(clazz);
		schemaMap.put(clazz.getClassName(), clazz);
	}
	
}
