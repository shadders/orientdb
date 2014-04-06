//package com.orientechnologies.binary.old;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import com.orientechnologies.binary.OSchemaVersion;
//import com.orientechnologies.binary.OSchemaSet;
//import com.orientechnologies.binary.OSchemaProperty;
//import com.orientechnologies.binary.PropertyIdProvider;
//import com.orientechnologies.binary.VarInt8;
//import com.orientechnologies.orient.core.metadata.schema.OType;
//
//
//
///**
// * Record format:
// * version:varint8|header_length:varint8|variable_length_declared_field_headers|undeclared_field_headers
// * |data
// * 
// * version: oClassInstance version
// * header_length: used when scanning schemaless header entries to ensure we don't scan the data
// * part of the record.
// * 
// * - Fixed length declared fields do not need a header entry as they are stored in oClassInstance.
// * - Variable length declared fields need only a length (offset is calculated as we scan through
// * the header and the rest of the field meta data is determined by the order of fields within the oClassInstance
// * - Undeclared (oClassInstance-less) fields require addition header data:
// * nameId:varint16|dataType?|offset:varint8|length:varint8
// * 
// * Suggest objects of this class should be reusable for a pool.
// * 
// * @author Steve Coughlan
// *
// */
//public class Record {
//
//	private final PropertyIdProvider idProvider;
//	byte[] bytes = null;
//	OSchemaVersion oClassInstance;
//	
//	int headerStart;
//	int headerLength;
//	
//	private List<OSchemaProperty> schemaLessFields = new ArrayList();
//	//use a trove IntObjectMap here
//	private Map<Integer, OSchemaProperty> schemaLessFieldMap = new HashMap();
//	
//	public Record(OSchemaSet schemas, byte[] bytes) {
//		this.bytes = bytes;
//		VarInt8 version = VarInt8.read(bytes, 0);
//		oClassInstance = schemas.getSchema(version.getValue());
//		idProvider = schemas.getIdProvider();
//		VarInt8 headerLen = VarInt8.read(bytes, version.getBytesLength());
//		headerLength = headerLen.getValue();
//		headerStart = version.getBytesLength() + headerLen.getBytesLength();
//	}
//	
//	public Field read(int nameId) {
//		OSchemaProperty field = oClassInstance.getField(nameId);
//		if (field == null)
//			return readSchemaLessField(nameId);
//		if (field.getType().isFixedLength())
//			return readFixedLength(field);
//		return readVariableLength(field);
//	}
//	
//	private Field readFixedLength(OSchemaProperty field) {
//		return new Field(field, copyRange(field.getDataOffset(), field.getDataLength()));
//	}
//
//	private Field readVariableLength(OSchemaProperty field) {
//		int headerOffset = field.getHeaderOffset();
//		VarInt8 offset = VarInt8.read(bytes, headerOffset);
//		headerOffset += offset.getBytesLength();
//		VarInt8 length = VarInt8.read(bytes, headerOffset);
//		
//		return new Field(field, copyRange(offset.getValue(), length.getValue()));
//	}
//
//	private Field readSchemaLessField(int nameId) {
//		scanSchemaLessFields();
//		OSchemaProperty field = schemaLessFieldMap.get(nameId);
//		return new Field(field, copyRange(field.getDataOffset(), field.getDataLength()));
//	}
//	
//	private void scanSchemaLessFields() {
//		/**
//		 * Improve this by only scanning up to the requested field and marking the last scanned offset
//		 * then if later fields are needed we can resume the scan where we left off.
//		 */
//		int hoffset = oClassInstance.schemaLessHeaderOffset() + headerStart;
//		while (hoffset < headerLength) {
//			OSchemaProperty field = new OSchemaProperty();
//			// nameId:varint16|dataType?|offset:varint8|length:varint8
//			VarInt8 nameId = VarInt8.read(bytes, hoffset);
//			hoffset += nameId.getBytesLength();
//			VarInt8 dataType = VarInt8.read(bytes, hoffset);
//			hoffset += dataType.getBytesLength();
//			VarInt8 offset = VarInt8.read(bytes, hoffset);
//			hoffset += offset.getBytesLength();
//			VarInt8 length = VarInt8.read(bytes, hoffset);
//			field.setName(idProvider.nameFor(nameId.getValue()));
//			field.setType(OType.values()[dataType.getValue()]);
//			field.setDataOffset(offset.getValue());
//			field.setDataLength(length.getValue());
//			schemaLessFields.add(field);
//			schemaLessFieldMap.put(field.getNameId(), field);
//		}
//	}
//	
//	private byte[] copyRange(int offset, int length) {
//		byte[] result = new byte[length];
//		System.arraycopy(bytes, offset, result, 0, length);
//		return result;
//	}
//
//}
