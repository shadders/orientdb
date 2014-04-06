package com.orientechnologies.binary;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.orientechnologies.binary.util.ObjectPool;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;

/**
 * 
 * @author Steve Coughlan
 *
 */
public class OBinaryDocument extends ODocument {

	private ORecordHeader header;

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
		super(iClass);
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
		//if (header == null)
		//	header = ObjectPool.newRecordHeader();
		_recordFormat = ORecordSerializerFactory.instance().getFormat(BinaryDocumentSerializer.NAME);
	}

	ORecordHeader getHeader() {
		return header;
	}

}
