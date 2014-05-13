package com.orientechnologies.binary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.binary.util.CaselessString;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;

/**
 * Placeholder for all known schemas.
 * 
 * @author Steve Coughlan
 *
 */
public class OClassIndex {

	private static OClassIndex instance = new OClassIndex();
			
	private List<OClassSet> schemas = new ArrayList();
	private Map<CaselessString, OClassSet> schemaMap = new HashMap();
	
	public static final OClassSet SCHEMALESS = new OClassSet();
	
	private OClassIndex() {
	}
	
	public static OClassIndex get() {
		return instance;
	}
	
	public OClassSet getSchemaSetForId(int classId) {
		return schemas.get(classId);
	}
	
	public OClassVersion getCurrentSchemaForId(int classId) {
		OClassSet set = schemas.get(classId);
		return set == null ? null : set.currentSchema();
	}
	
	public OClassVersion getCurrentSchemaForName(String name) {
		OClassSet set = schemaMap.get(new CaselessString(name));
		return set == null ? null : set.currentSchema(); 
	}
	
	public OClassSet getSchemaSetForName(String name) {
		return schemaMap.get(new CaselessString(name));
	}
	
//	public static void addNewClass(OClassVersion clazz) {
//		OClassSet set = new OClassSet(clazz.getName(), Collections.singletonList(clazz));
//		addClass(set);
//	}
	
	/**
	 * Used when generating a new class
	 * @param clazz
	 */
	public void newClass(OClassSet clazz) {
		
		if (schemaMap.containsKey(clazz.getClassName()))
			throw new RuntimeException("Class cannot be added to full schema set twice: " + clazz);
		
		clazz.setClassId(schemas.size());
		schemas.add(clazz);
		schemaMap.put(clazz.getClassName(), clazz);
	}
	
	/**
	 * Used to register classes as they are deserialized from schema
	 * @param clazz
	 */
	void registerClass(OClassSet clazz) {
		
		//if (schemaMap.containsKey(clazz.getClassName()))
		//	OLogManager.instance().info(OClassIndex.class, "Class is being registered when already registered: " + clazz);
			
		
		/*
		 * We may not get classes back from schema in order so pad out the list
		 * to ensure it has capacity to set the registered class at the correct index
		 */
		while (schemas.size() <= clazz.getClassId())
			schemas.add(null);
		
		schemas.set(clazz.getClassId(), clazz);
		schemaMap.put(clazz.getClassName(), clazz);
	}
	
}
