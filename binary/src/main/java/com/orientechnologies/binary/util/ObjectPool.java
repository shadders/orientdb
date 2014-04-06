package com.orientechnologies.binary.util;

import com.orientechnologies.binary.ORecordHeader;
import com.orientechnologies.binary.OSchemaProperty;
import com.orientechnologies.binary.OSchemaVersion;
import com.orientechnologies.binary.old.OHeaderEntry;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Class for providing reusable instances of Objects
 * 
 * @author Steve Coughlan
 *
 */
public class ObjectPool {


	public static ORecordHeader newRecordHeader() {
		return new ORecordHeader();
	}
	
	public static OSchemaProperty newRecordHeaderEntry(OSchemaVersion clazz, String name, OType type) {
		return new OSchemaProperty(clazz, name, type);
	}
	
	public static void release(IRecyclable recyclable) {
		recyclable.reset();
		//determine type and put it back in the object pool.
	}

}

