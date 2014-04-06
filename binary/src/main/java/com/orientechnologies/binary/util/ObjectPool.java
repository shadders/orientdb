package com.orientechnologies.binary.util;

import com.orientechnologies.binary.ORecordHeader;
import com.orientechnologies.binary.OBinaryProperty;
import com.orientechnologies.binary.OClassVersion;
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
	
	public static OBinaryProperty newRecordHeaderEntry(OClassVersion clazz, String name, OType type) {
		return new OBinaryProperty(clazz, name, type);
	}
	
	public static void release(IRecyclable recyclable) {
		recyclable.reset();
		//determine type and put it back in the object pool.
	}

}

