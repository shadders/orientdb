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

import java.util.HashMap;
import java.util.Map;

import com.orientechnologies.binary.OBinProperty;
import com.orientechnologies.binary.serializer.FieldSerializerStrategy;
import com.orientechnologies.binary.serializer.IFieldSerializer;
import com.orientechnologies.common.serialization.types.OShortSerializer;
import com.orientechnologies.orient.core.id.OClusterPositionFactory;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Temporary utility class for extracting functionality that would otherwise have to change core code.
 * 
 * @author Steve Coughlan
 *
 */
public class BinUtils {

	/**
	 * Field length value is intended to become a property of OType in final implementation.
	 */
	private static Map<Enum, Integer> FIELD_LENGTHS = new HashMap();
	
	static {
		FIELD_LENGTHS.put(OType.BOOLEAN, 1);
		FIELD_LENGTHS.put(OType.INTEGER, 4);
		FIELD_LENGTHS.put(OType.SHORT, 2);
		FIELD_LENGTHS.put(OType.LONG, 8);
		FIELD_LENGTHS.put(OType.FLOAT, 4);
		FIELD_LENGTHS.put(OType.DOUBLE, 8);
		FIELD_LENGTHS.put(OType.DATETIME, 8);
		FIELD_LENGTHS.put(OType.STRING, -1);
		FIELD_LENGTHS.put(OType.BINARY, -1);
		FIELD_LENGTHS.put(OType.EMBEDDED, -1);
		FIELD_LENGTHS.put(OType.EMBEDDEDLIST, -1);
		FIELD_LENGTHS.put(OType.EMBEDDEDSET, -1);
		FIELD_LENGTHS.put(OType.EMBEDDEDMAP, -1);
		int CLUSTER_POS_SIZE = OClusterPositionFactory.INSTANCE.getSerializedSize();
		//FIELD_LENGTHS.put(OType.LINK, 10); //ORID using short version of OClusterPosition
		FIELD_LENGTHS.put(OType.LINK, OShortSerializer.SHORT_SIZE + CLUSTER_POS_SIZE);
		FIELD_LENGTHS.put(OType.LINKLIST, -1);
		FIELD_LENGTHS.put(OType.LINKSET, -1);
		FIELD_LENGTHS.put(OType.LINKMAP, -1);
		FIELD_LENGTHS.put(OType.BYTE, 1);
		FIELD_LENGTHS.put(OType.TRANSIENT, -1); //unsure?
		FIELD_LENGTHS.put(OType.DATE, 8);
		FIELD_LENGTHS.put(OType.CUSTOM, -1);
		FIELD_LENGTHS.put(OType.DECIMAL, -1);
		FIELD_LENGTHS.put(OType.LINKBAG, -1);
		FIELD_LENGTHS.put(OType.ANY, -1);
	}
	
	/**
	 * IN A REAL IMPLEMENTATION this will become a method of OType
	 * 
	 * @return true if the field type is fixed length.
	 */
	public static boolean isFixedLength(OBinProperty property) {
		IFieldSerializer serializer = FieldSerializerStrategy.get().serializerFor(property);
		return serializer.isFixedLength();
	}

	/**
	 * IN A REAL IMPLEMENTATION this will become a method of OType
	 * 
	 * @return the length of the field in bytes or -1 if this field type is
	 *         variable length.
	 */
	public static int fieldLength(OBinProperty property) {
		IFieldSerializer serializer = FieldSerializerStrategy.get().serializerFor(property);
		if (serializer == null)
			System.currentTimeMillis();
		return serializer.isFixedLength() ? serializer.getFixedLength() : -1;
		//return FIELD_LENGTHS.get(type);
	}

	/**
	 * IN A REAL IMPLEMENTATION this will replace the method in OType
	 * 
	 * Return the type by ID.
	 * 
	 * 
	 * @param iId
	 *            The id to search
	 * @return The type if any, otherwise null
	 */
	public static OType getById(final byte iId) {
		/**
		 * Replacement for OType.getById when fieldLength field is added to the type.
		 */
//		OType ordinal = values()[iId];
//		if (iId == ordinal.id)
//			return ordinal;
//		// fallback in case ordinality gets messed up in the future.
//		for (OType t : TYPES) {
//			if (iId == t.id)
//				return t;
//		}
//		return null;
		
		throw new RuntimeException("not implemented, just a placeholder for the code");
	}

}
