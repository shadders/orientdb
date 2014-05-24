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

package com.orientechnologies.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;

/**
 * 
 * @author Steve Coughlan
 * 
 */
public class OBinaryDocument extends ODocument {

	public static final byte RECORD_TYPE = 'c';

	private OBinRecordHeader header;
	
	public OBinaryDocument() {
	}

	public OBinaryDocument(byte[] iSource) {
		super(iSource);
	}

	public OBinaryDocument(InputStream iSource) throws IOException {
		super(iSource);
	}

	public OBinaryDocument(ORID iRID) {
		super(iRID);
	}

	public OBinaryDocument(String iClassName, ORID iRID) {
		super(iClassName, iRID);
	}

	public OBinaryDocument(String iClassName) {
		super(iClassName);
	}

	public OBinaryDocument(OClassVersion iClass) {
		super(iClass.getClassSet());
	}

	public OBinaryDocument(Object[] iFields) {
		super(iFields);
	}

	public OBinaryDocument(Map<? extends Object, Object> iFieldMap) {
		super(iFieldMap);
	}

	public OBinaryDocument(String iFieldName, Object iFieldValue, Object... iFields) {
		super(iFieldName, iFieldValue, iFields);
	}

	protected void setup() {
		super.setup();
		// if (header == null)
		// header = ObjectPool.newRecordHeader();
		_recordFormat = ORecordSerializerFactory.instance().getFormat(BinaryDocumentSerializer.NAME);
	}
	
	/**
	 * Internal
	 */
	void setClassInternal(OClass clazz) {
		setClass(clazz);
	}
	
	/**
	 * Internal.
	 */
	public byte getRecordType() {
		return RECORD_TYPE;
	}

	/**
	 * Part of internal API, DO NOT USE.
	 */
	public OBinRecordHeader getHeader() {
		if (isDirty())
			header = null;
		return header;
	}

//	/**
//	 * @return an immutable set of field names
//	 */
//	public Set<String> fieldNameSet() {
//		return Collections.unmodifiableSet(_fieldValues.keySet());
//	}
//
//	/**
//	 * @return an immutable collection of field values
//	 */
//	public Collection fieldValueCollection() {
//		return Collections.unmodifiableCollection(_fieldValues.values());
//	}
}
