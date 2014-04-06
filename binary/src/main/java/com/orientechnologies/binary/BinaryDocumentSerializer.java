package com.orientechnologies.binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.binary.util.ObjectPool;
import com.orientechnologies.binary.util.Varint;
import com.orientechnologies.common.serialization.types.OBinarySerializer;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecordFactoryManager.ORecordFactory;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.serialization.serializer.binary.OBinarySerializerFactory;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializer;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;

public class BinaryDocumentSerializer implements ORecordSerializer {

	public static final String NAME = "Binary Document Serializer";
	
	static {
		ORecordSerializerFactory.instance().register(BinaryDocumentSerializer.NAME, 
				new BinaryDocumentSerializer());
	}

	public ORecordInternal<?> fromStream(byte[] bytes, ORecordInternal<?> iRecord) {
		
		ORecordHeader header = ObjectPool.newRecordHeader();
		header.parseHead(bytes, null);
		
		OSchemaVersion clazz = header.getClazz();
		OBinaryDocument doc = new OBinaryDocument(clazz);
		PropertyIdProvider idProvider = PropertyIdProvider.getForClass(clazz.getName());		
		
		int index;
		
		for (int i = 0; i < header.getFieldCount(); i++) {
			
			IBinaryHeaderEntry entry = header.fieldHeader(i);
			
			String field = idProvider.casedNameFor(entry.getNameId());
			Object value;
			
			if (!entry.isSchemaless()) {
				
				OSchemaProperty property = clazz.getField(entry.getNameId());
				//validate.  TODO add more fields to error messages
				if (property.getType() != entry.getType())
					throw new RuntimeException("BinaryDocument header type does not match schema type for "
							+ "field: " + field);
				if (property.getNameId() != entry.getNameId())
					throw new RuntimeException("BinaryDocument header nameId does not match schema nameId for "
							+ "field: " + field);		
			}
			
			if (header.isNull(i))
				value = null;
			else
				value = readField(bytes, header.getDataOffset(), entry);
			
			
			doc.field(field, value, entry.getType());
			
		}
		
		return doc;
		
	}
		
	@Override
	public ORecordInternal<?> fromStream(byte[] bytes, ORecordInternal<?> iRecord, String[] iFields) {
		
		ORecordHeader header = ObjectPool.newRecordHeader();
		header.parseHead(bytes, iFields);
		
		OBinaryDocument doc;
		
		if (iRecord instanceof OBinaryDocument)
			doc = (OBinaryDocument) iRecord;
		else
			doc = new OBinaryDocument(header.getClazz());
				
		int index;
		
		if (iFields != null) {
			for (String field: iFields) {
				index = header.indexOf(field);
				
				//TODO handle embedded field i.e. myEmbedded.field1
				IBinaryHeaderEntry entry = header.fieldHeader(index);
				doc.field(field, readField(bytes, header.getDataOffset(), entry), entry.getType());
			}
		}
		
		return doc;
		
	}

	@Override
	public byte[] toStream(ORecordInternal<?> iSource, boolean iOnlyDelta) {
		
		OBinaryDocument doc = (OBinaryDocument) iSource;
		
		ORecordHeader header = doc.getHeader();
		OSchemaVersion clazz = null;
		
		if (header == null) {
			//might be a new record
			String className = doc.getClassName();
			if (className != null) {
				clazz = OSchemaIndex.getCurrentSchemaForName(className);
			} else {
				clazz = OSchemaIndex.SCHEMALESS;
			}
			header = new ORecordHeader(doc, clazz, !iOnlyDelta);
			header.setClazz(clazz);
			header.setClassId(clazz.getSchemaId());
			header.setVersion(clazz.getVersion());
		} else {
			clazz = header.getClazz();
			if (clazz == null)
				//If the header exists it should already have class set.
				throw new RuntimeException("Could not determine class");
		}
		//set header fields from class
		
		
		
		//TODO guess length
		//ByteArrayOutputStream hout = new ByteArrayOutputStream(); //header
		ByteArrayOutputStream dout = new ByteArrayOutputStream(); //data
		Set<String> unwrittenFields = new HashSet(Arrays.asList(doc.fieldNames()));
		
		OSchemaProperty property;
		
		int offset = 0;
		
		if (clazz.getSchemaId() > 0) {
			//write fixed length first
			for (OSchemaProperty fixedProperty: clazz.getFixedLengthProperties()) {
				String fieldName = fixedProperty.getName();
				Object value = doc.rawField(fieldName);
				if (value != null)
					offset += writeField(dout, fixedProperty, value);
				else  {
					offset += fixedProperty.getDataLength();
					try {
						//pad it out
						dout.write(new byte[fixedProperty.getDataLength()]);
					} catch (IOException e) {
						// should never happen
						throw new RuntimeException(e);
					}
				}
				unwrittenFields.remove(fieldName);
			}
			
			
			//write variable length fields second
			//for (OSchemaProperty property: clazz.getVariableLengthProperties()) {
			for (int i = 0; i < clazz.variableLengthPropertyCount(); i++) {
				property = (OSchemaProperty) header.fieldHeaderVariable(i);
				String fieldName = property.getName();
				Object value = doc.rawField(fieldName);
				int dataLength = 0;
				if (value != null)
					dataLength = writeField(dout, property, value);
				property.setDataLength(dataLength);
				property.setInDataOffset(offset);
				offset += dataLength;
				//do something with the length... update the header entry
				//but will need to clone it first.
				unwrittenFields.remove(fieldName);
			}
		}
		
		//any unwritten fields are schemaless
		int i = 0;
		for (String field: unwrittenFields) {
//			OType type = doc.fieldType(field);
//			if (type == null) {
//				//TODO guess type
//				type = OType.ANY;
//			}
			Object value = doc.rawField(field);
			//property = new OSchemaProperty(clazz, field, type);
			property = (OSchemaProperty) header.fieldHeaderSchemaless(i);
			int dataLength = 0;
			if (value != null)
				dataLength = writeField(dout, property, value);
			property.setInDataOffset(offset);
			offset += dataLength;
			property.setDataLength(dataLength);
			i++;
		}
		
		int headerPadding = 0;
		int dataPadding = 0;
		
		//finalise header
		header.setDataLength(dout.size() + dataPadding);
		
		byte[] headerBytes = header.writeHeader(headerPadding);
		
		
		
		//concat data with header
		byte[] bytes = new byte[headerBytes.length + dout.size()];
		System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
		System.arraycopy(dout.toByteArray(), 0, bytes, headerBytes.length, dout.size());
		
		return bytes;
	}
	
	private Object readField(byte[] bytes, int offset, IBinaryHeaderEntry entry) {
		OBinarySerializer serializer = OBinarySerializerFactory.getInstance().getObjectSerializer(entry.getType());
		if (serializer != null)
			/**
			 * Note the current implementations embed length in data when necessary.  
			 * We don't need this because it's stored in the header.  This allows us
			 * to calculate data holes without having the scan the entire data area.
			 */
			return serializer.deserialize(bytes, offset + entry.getInDataOffset());
		
		throw new RuntimeException("No serializer registered for type: " + entry.getType());
	}
	
	/**
	 * @param out
	 * @param entry
	 * @param value
	 * @return number of bytes written
	 */
	private int writeField(OutputStream out, IBinaryHeaderEntry entry, Object value) {
		OBinarySerializer serializer = OBinarySerializerFactory.getInstance().getObjectSerializer(entry.getType());
		if (serializer != null) {
			/**
			 * Note the current implementations embed length in data when necessary.  
			 * We don't need this because it's stored in the header.  This allows us
			 * to calculate data holes without having the scan the entire data area.
			 */
			byte[] stream = new byte[serializer.getObjectSize(value)];
			serializer.serialize(value, stream, 0);
			try {
				out.write(stream);
			} catch (IOException e) {
				//should never happen with a ByteArrayOutputStream
				throw new RuntimeException(e);
			}
			//return lengh so header entry can be updated
			return stream.length;
		}
		
		throw new RuntimeException("No binary serializer registered for type: " + entry.getType());
	}

}
