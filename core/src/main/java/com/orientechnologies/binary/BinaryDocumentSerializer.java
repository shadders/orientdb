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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.binary.serializer.FieldSerializerStrategy;
import com.orientechnologies.binary.serializer.IFieldSerializer;
import com.orientechnologies.binary.util.ObjectPool;
import com.orientechnologies.common.io.UnsafeByteArrayOutputStream;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializer;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;

/**
 * 
 * @author Steve Coughlan
 * 
 */
public class BinaryDocumentSerializer implements ORecordSerializer {

	public static final String	NAME	= "Binary Document Serializer";

	static {
		ORecordSerializerFactory.instance().register(BinaryDocumentSerializer.NAME, new BinaryDocumentSerializer());
	}

	public ORecordInternal<?> fromStream(byte[] bytes, ORecordInternal<?> iRecord) {

		OBinRecordHeader header = ObjectPool.newRecordHeader();
		header.parseHead(bytes, null);

		OClassVersion clazz = header.getClazz();
		OBinaryDocument doc;// = new OBinaryDocument(clazz);

		if (iRecord instanceof OBinaryDocument) {
			doc = (OBinaryDocument) iRecord;
			doc.setClassInternal(clazz.classSet);
		} else
			doc = new OBinaryDocument(header.getClazz());

		int index;

		for (int i = 0; i < header.getFieldCount(); i++) {

			IBinHeaderEntry entry = header.fieldHeader(i);

			String field = clazz.getClassSet().nameFor(entry.getNameId());
			if (field.isEmpty()) {
				// embedded field name
				field = entry.getName();
			}
			Object value;

			if (!entry.isSchemaless()) {

				OBinProperty property = clazz.getField(entry.getNameId());
				// validate. TODO add more fields to error messages
				if (property.getType() != entry.getType())
					throw new RuntimeException("BinaryDocument header type does not match schema type for " + "field: " + field);
				if (property.getNameId() != entry.getNameId())
					throw new RuntimeException("BinaryDocument header nameId does not match schema nameId for " + "field: " + field);
			}

			if (header.isNull(i))
				// value = null;
				value = readField(header, bytes, header.getDataOffset(), entry.asProperty());
			else
				value = readField(header, bytes, header.getDataOffset(), entry.asProperty());

			doc.field(field, value, entry.getType());

		}

		return doc;

	}

	@Override
	public ORecordInternal<?> fromStream(byte[] bytes, ORecordInternal<?> iRecord, String[] iFields) {

		if (iFields == null || iFields.length == 0)
			return fromStream(bytes, iRecord);

		OBinRecordHeader header = ObjectPool.newRecordHeader();
		header.parseHead(bytes, iFields);

		OClassVersion clazz = header.getClazz();
		OBinaryDocument doc;// = new OBinaryDocument(clazz);

		if (iRecord instanceof OBinaryDocument) {
			doc = (OBinaryDocument) iRecord;
			doc.setClassInternal(clazz.classSet);
		} else
			doc = new OBinaryDocument(header.getClazz());

		int index;

		if (iFields != null) {
			for (String field : iFields) {
				index = header.indexOf(field);

				// TODO handle embedded field i.e. myEmbedded.field1
				IBinHeaderEntry entry = header.fieldHeader(index);
				Object value = null;
				if (!header.isNull(index))
					value = readField(header, bytes, header.getDataOffset(), entry.asProperty());
				doc.field(field, value, entry.getType());
			}
		}

		return doc;

	}

	@Override
	public byte[] toStream(ORecordInternal<?> iSource, boolean iOnlyDelta) {

		ODocument doc = (ODocument) iSource;
		OBinRecordHeader header = null;
		if (iSource instanceof OBinaryDocument) {
			header = ((OBinaryDocument) iSource).getHeader();
		}
		OClassVersion clazz = null;

		if (header == null) {
			// might be a new record
			String className = doc.getClassName();
			if (className != null) {
				clazz = OClassIndex.get().getCurrentSchemaForName(className);
			} else {
				clazz = OClassIndex.SCHEMALESS.currentSchema();
			}
			header = new OBinRecordHeader(doc, clazz, !iOnlyDelta);
			header.setClazz(clazz);
			header.setClassId(clazz.getClassId());
			header.setVersion(clazz.getVersion());
		} else {
			clazz = header.getClazz();
			if (clazz == null)
				// If the header exists it should already have class set.
				throw new RuntimeException("Could not determine class");
		}
		// set header fields from class

		// TODO guess length
		// ByteArrayOutputStream hout = new ByteArrayOutputStream(); //header
		UnsafeByteArrayOutputStream dout = new UnsafeByteArrayOutputStream(); // data
		Set<String> unwrittenFields = new HashSet(Arrays.asList(doc.fieldNames()));

		OBinProperty property;

		int offset = 0;

		if (clazz.getClassId() > 0) {
			// write fixed length first
			for (OBinProperty fixedProperty : clazz.getFixedLengthProperties()) {
				String fieldName = fixedProperty.getName();
				Object value = doc.rawField(fieldName);
				if (value != null)
					offset += writeField(header, dout, fixedProperty, value);
				else {
					offset += fixedProperty.getDataLength();
					try {
						// pad it out
						dout.write(new byte[fixedProperty.getDataLength()]);
					} catch (IOException e) {
						// should never happen
						throw new RuntimeException(e);
					}
				}
				unwrittenFields.remove(fieldName);
			}

			// write variable length fields second
			// for (OBinProperty property: clazz.getVariableLengthProperties()) {
			for (int i = 0; i < clazz.variableLengthPropertyCount(); i++) {
				property = (OBinProperty) header.fieldHeaderVariable(i);
				String fieldName = property.getName();
				Object value = doc.rawField(fieldName);
				int dataLength = 0;
				if (value != null)
					dataLength = writeField(header, dout, property, value);
				property.setDataLength(dataLength);
				property.setInDataOffset(offset);
				offset += dataLength;
				// do something with the length... update the header entry
				// but will need to clone it first.
				unwrittenFields.remove(fieldName);
			}
		}

		// any unwritten fields are schemaless
		int i = 0;
		for (String field : unwrittenFields) {
			// OType type = doc.fieldType(field);
			// if (type == null) {
			// //TODO guess type
			// type = OType.ANY;
			// }
			Object value = doc.rawField(field);
			// property = new OBinProperty(clazz, field, type);
			property = (OBinProperty) header.fieldHeaderSchemaless(i);
			int dataLength = 0;
			if (value != null)
				dataLength = writeField(header, dout, property, value);
			property.setInDataOffset(offset);
			offset += dataLength;
			property.setDataLength(dataLength);
			i++;
		}

		int headerPadding = 0;
		int dataPadding = 0;

		// finalise header
		header.setDataLength(dout.size() + dataPadding);

		byte[] headerBytes = header.writeHeader(headerPadding);

		// concat data with header
		byte[] bytes = new byte[headerBytes.length + dout.size()];
		System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
		System.arraycopy(dout.toByteArrayUnsafe(), 0, bytes, headerBytes.length, dout.size());

		return bytes;
	}

	private Object readField(OBinRecordHeader header, byte[] bytes, int offset, OBinProperty entry) {
		// OBinarySerializer serializer = OBinarySerializerFactory.getInstance().getObjectSerializer(entry.getType());
		IFieldSerializer serializer = FieldSerializerStrategy.get().serializerFor(entry);
		if (serializer != null)
			/**
			 * Note the current implementations embed length in data when necessary. We don't need this because it's stored in the header.
			 * This allows us to calculate data holes without having the scan the entire data area.
			 */
			// return serializer.deserialize(bytes, offset + entry.getInDataOffset());
			return serializer.deserialize(entry.getName(), bytes, offset + entry.getInDataOffset(), entry.getDataLength());

		throw new RuntimeException("No serializer registered for type: " + entry.getType());
	}

	/**
	 * @param out
	 * @param entry
	 * @param value
	 * @return number of bytes written
	 */
	private int writeField(OBinRecordHeader header, UnsafeByteArrayOutputStream out, OBinProperty entry, Object value) {
		// OBinarySerializer serializer = OBinarySerializerFactory.getInstance().getObjectSerializer(entry.getType());
		IFieldSerializer serializer = FieldSerializerStrategy.get().serializerFor(entry, value);
		if (serializer != null) {
			/**
			 * Note the current implementations embeds length in data when necessary. We don't need this because it's stored in the
			 * header. This allows us to calculate data holes without having the scan the entire data area.
			 */
			// byte[] stream = new byte[serializer.getObjectSize(value)];
			try {
				int size = out.size();
				int written = serializer.serialize(out, entry.getName(), value);
				if (out.size() - size != written)
					throw new RuntimeException(serializer.getClass() + " has returned incorrect number of bytes written.");
				return written;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}

		throw new RuntimeException("No binary serializer registered for type: " + entry.getType());
	}

}
