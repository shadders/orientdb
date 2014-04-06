package com.orientechnologies.binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.orientechnologies.binary.old.OHeaderEntry;
import com.orientechnologies.binary.util.Bits;
import com.orientechnologies.binary.util.IRecyclable;
import com.orientechnologies.binary.util.ObjectPool;
import com.orientechnologies.binary.util.Varint;
import com.orientechnologies.common.serialization.types.OIntegerSerializer;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.binary.OBinarySerializerFactory;

/**
 * Record header for OBinary Document.
 * <p>
 * The header is made of 3 portions
 * <p>
 * HEAD
 * classId:varint - index of the Class which resolves to OSchemaSet
 * version:varint - version number of schema resolves to OSchemaVersion
 * headerLength:varint - length in bytes of the complete header.  This excludes dataOffset and
 * dataLength but includes any padding that may be added to accommodate additional header entries
 * being added in the future.
 * <p>
 * These first 3 fields are padded to HEADER_HEADER_PADDING bytes if shorter
 * <p>
 * fieldCount:varint - number of fields in header (this doesn't include fixed length declared fields)
 * nullbitsLength:varint - number of bytes for nullbits
 * nullbites:byte[] - bitset indicated nulled fields
 * <p>
 * SCHEMA DECLARED FIXED LENGTH HEADER ENTRIES
 * <p>
 * There are no entries for these fields as all needed information is in the schema.  Null entries
 * have their data space reserved so offsets and length for subsequent entries are not dependent on
 * nullability of previous fields.
 * <p>
 * SCHEMA DECLARED VARIABLE LENGTH HEADER ENTRIES
 * 
 * These entries do not require nameId or dataType as they are declared in the schema.
 * 
 * offset:varint - the offset within the data portion of the record
 * length:varint - length of serialized field
 * 
 * SCHEMALESS ENTRIES
 * 
 * Entries not declared in schema at all require a nameId and dataType
 * 
 * nameId:varint - index of actual string name within the class
 * dataType:byte - equivalent to OType.id
 * offset:varint - the offset within the data portion of the record
 * length:varint - length of serialized field
 * 
 * Following the last entry and beginning at headerByte[headerLength]:
 * 
 * dataLength:uint32 - length of data, this may included reserved space.
 * 
 * 
 * @author Steve Coughlan
 *
 */
public class ORecordHeader implements IRecyclable {
	
	/**
	 * Estimate of space that needs to be reserved for the 1st 3 fields of the header.
	 * Header length cannot be known until the entire header is written so we reserve
	 * 2 bytes for it to cover majority of use cases.  On rare cases where it doesn't fit 
	 * then the entire header byte array will be rewritten to accommodate the header_length varint.
	 * 
	 * The setting of 5 assumes a database is unlikely to have more than 16k classes and a class
	 * is unlikely to have more than 127 schema versions and that the record is unlikely to have
	 * a header longer than 16kb.
	 * 
	 * This could potentially be a configurable property.  The most likely way this limit would be
	 * exceeded is by classes in excess of of 16k.  If the user is aware of this and expects headers
	 * to exceed 127 bytes (likely) they could set the padding to 6 or more bytes.
	 * 
	 */
	private static final int HEADER_HEADER_PADDING = 5;
	
	private byte[] bytes;
	private int offset;
	
	private PropertyIdProvider idProvider;
	private OSchemaVersion clazz;
	private int classId;
	private int version;
	private int headerLength;
	private int fieldCount;
	private byte[] nullBits;
	
	private int dataLength;
	/*
	 * we can't write absolute data offsets (i.e. including header bytes) into the header
	 * because we don't know the header length until it's fully written.  So offsets are
	 * stored as the offset from the beginning of the data portion.  dataOffset is the
	 * index where the data portion begins.  It doesn't appear in the header as it is
	 * calculated from the headerLength + 4 bytes for the dataLength field.
	 */
	private int dataOffset;
	
	
	private int parsedProperties = 0;
	private int parsedOffset = 0;
	
	/**
	 * Should match the ordering of the original schema class.  This is important
	 * to ensure nullBits are ordered the same as header entries.
	 */
	private List<OSchemaProperty> propertyEntries = new ArrayList();
	
	public ORecordHeader() {
		
	}
	
	/**
	 * Build a header from a document.  This constructor is for schemaless documents
	 * @param doc
	 */
	ORecordHeader(OBinaryDocument doc, OSchemaVersion clazz, boolean updateSchema) {
		if (clazz == null)
			clazz = OSchemaIndex.SCHEMALESS;
		else if (updateSchema) {
			/*
			 * If the record is being rewritten and the schema has changed since
			 * it was last writter it will have an outdated schema so we can take
			 * this opportunity to rewrite it with the latest schema.
			 */
			clazz = clazz.getSchemaSet().currentSchema();
		}
		clazz = clazz.getMutableCopy();
		this.clazz = clazz;
		
		String[] fields = doc.fieldNames();
		Set<String> schemalessFields = new HashSet(Arrays.asList(fields));
		List<Boolean> nulls = new ArrayList(schemalessFields.size());
		//add schema related properties to the header
		for (OSchemaProperty fixed: clazz.getFixedLengthProperties()) {
			//don't add fixed properties as they aren't part of the header
			//only need to calculate nullBits.
			nulls.add(doc.field(fixed.getName()) == null);
			schemalessFields.remove(fixed.getName());
		}
		for (OSchemaProperty variable: clazz.getVariableLengthProperties()) {
			propertyEntries.add(variable);
			nulls.add(doc.field(variable.getName()) == null);
			schemalessFields.remove(variable.getName());
		}
		
		//add any additional fields
		for (String field: schemalessFields) {
			OSchemaProperty property = clazz.getField(field);
			if (property == null) {
				//it's not declared to we need to add an entry
				//property = new OSchemaProperty(true, null);
				
				OType type = doc.fieldType(field);
				if (type == null)
					type = OType.ANY;
				//TODO is there a way to determine type dynamically?
				property = new OSchemaProperty(clazz, field, type);
				
				
				//setName seems to trigger an attempt to update schema in DB
				property.setSchemaless(true);
				property.setInternalOrder(propertyEntries.size() - clazz.fixedLengthPropertyCount());
				propertyEntries.add(property);
			} else {
				//sanity check
				throw new RuntimeException("schemalessFields contains a field that is in schema");
			}
			nulls.add(doc.field(field) == null);
		}
		
		nullBits = Bits.toBits(nulls);
		//mark the header as fully parsed to prevent attempts to parse it
		//when retrieving via fieldHeader(int)
		parsedProperties = propertyEntries.size() + clazz.fixedLengthPropertyCount();
	}
	
	/**
	 * 
	 * @param out
	 * @return number of bytes written
	 */
	byte[] writeHeader(int headerPadding) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int headerLengthoffset = 0;
		
		Varint.writeUnsignedVarInt(classId, out);
		headerLengthoffset += Varint.bytesLength(classId);
		
		Varint.writeUnsignedVarInt(version, out);
		headerLengthoffset += Varint.bytesLength(version);
		
		//write zeros to the area where headerSize is supposed to be
		//we will return later and fill it in
		while (out.size() < HEADER_HEADER_PADDING)
			out.write(0);
		
		Varint.writeUnsignedVarInt(propertyEntries.size() + clazz.fixedLengthPropertyCount(), out);
		
		Varint.writeUnsignedVarInt(nullBits.length, out);
		
		try {
			out.write(nullBits);
		} catch (IOException e) {
			// never happens with ByteArrayOutputStream
			throw new RuntimeException(e);
		}
		
		//add entries for variable length fields and schemaless fields
		boolean finishedSchemaDeclared = false;
		for (int i = 0; i < propertyEntries.size(); i++) {
			OSchemaProperty property = propertyEntries.get(i);
			//sanity check
			if (!property.isSchemaless()) {
				if (finishedSchemaDeclared)
					throw new RuntimeException("Schema declared properties found after schemaless properties.  "
							+ "This indicates propertyEntries is not sorted properly.");
				
				//another sanity check - make sure a fixed length schema declared property
				//snuck in.
				if (property.isFixedLength())
					throw new RuntimeException("ORecordHeader has an entry for a fixed length schema declared "
							+ "property.  This should never happen.");
				
				//if this isn't true then we can't check nullBits correctly.
				if (i + clazz.fixedLengthPropertyCount() != property.getInternalOrder())
					throw new RuntimeException("Schema declared property is not in the correct position in propertyEntries.");
			
			} else {
				finishedSchemaDeclared = true;
				
				Varint.writeUnsignedVarInt(property.getNameId(), out);
				//FIXME currently OType doesn't garuntee that ordinal == id
				//although that's currently the case for all type declared
				//at the present time.
				out.write(property.getType().ordinal());
			}
			//both types of property need these fields but only write then
			//if the null bit is not set
			if (!isNull(i + clazz.fixedLengthPropertyCount())) {
				Varint.writeUnsignedVarInt(property.getInDataOffset(), out);
				Varint.writeUnsignedVarInt(property.getDataLength(), out);
			}
			
		}
		
		headerLength = out.size();
		
		//add 4 blank bytes so we can write to dataLength field later
		byte[] lenBytes = new byte[4];
		try {
			out.write(lenBytes);
		} catch (IOException e) {
			// should never happen with ByteArrayOutputStream
			throw new RuntimeException(e);
		}
		
		byte[] bytes = out.toByteArray();
		
		int headerLengthSize = Varint.bytesLength(headerLength);
		int excess = HEADER_HEADER_PADDING - headerLengthoffset - headerLengthSize;
		if (excess < 0 ) {
			/*
			 * Not enough space so recopy to a new array.  This should happen rarely.
			 * Assuming schema versions are rarely more than 128 per class it would
			 * required the databases has more than 16k classes in which case we can fit
			 * a header length of 16kb
			 */
			
			{
				headerLength -= excess; //excess is negative
				headerLengthSize = Varint.bytesLength(headerLength);
				excess = HEADER_HEADER_PADDING - headerLengthoffset - headerLengthSize;
				/*
				 * Just in case increasing headerLength rolls over the size of the
				 * Varint to another byte we have to repeat this
				 */
				
			} while (excess < 0);
			
			byte[] temp = new byte[headerLength];
			System.arraycopy(bytes, 0, temp, 0, headerLengthoffset);
			System.arraycopy(bytes, HEADER_HEADER_PADDING, temp, headerLengthoffset + headerLengthSize
					, headerLengthSize);
			bytes = temp;
		} else {
			
		}
		Varint.writeUnsignedVarInt(headerLength, bytes, headerLengthoffset);
		
		/**
		 * Fix dataOffset, it's subject to the same problem as headerLength
		 * where calculating it might change it's value.
		 * 
		 * Look at the whether SchemaSet should inherit from OClassImpl
		 * instead of or in addition to SchemaVersion.  This would allow
		 * schema versions and nameIds to be properties and enable easier
		 * integration with ODB's native serialization.
		 */
		//Varint.writeUnsignedVarInt(headerLength + headerPadding, out);
		//Varint.writeUnsignedVarInt(dataLength, out);
		
		//append dataLength.  We padded the last bytes earlier as a placeholder.
		OIntegerSerializer.INSTANCE.serialize(dataLength, bytes, bytes.length - 4);

		
		return bytes;
	}
	
	/**
	 * Parses the header of the header (i.e. doesn't parse variable length or schema-less property
	 * headers).
	 * @param bytes
	 * @param iFields
	 * @return the offset after parsing the head of the header
	 */
	int parseHead(byte[] bytes, String[] iFields) {
		
		this.bytes = bytes;
		int offset = this.offset;
		
		classId = Varint.readUnsignedVarInt(bytes, 0);
		offset += Varint.bytesLength(classId);
		
		version = Varint.readUnsignedVarInt(bytes, offset);
		offset += Varint.bytesLength(version);
		
		clazz = OSchemaIndex.getSchemaSetForId(classId).getSchema(version);
		idProvider = OSchemaIndex.getSchemaSetForId(classId).getIdProvider();
		
		headerLength = Varint.readUnsignedVarInt(bytes, offset);
		offset += Varint.bytesLength(headerLength);
		
		if (offset < HEADER_HEADER_PADDING)
			offset = HEADER_HEADER_PADDING;
		
		fieldCount = Varint.readUnsignedVarInt(bytes, offset);
		offset += Varint.bytesLength(fieldCount);
		
		int nullbitsLength = Varint.readUnsignedVarInt(bytes, offset);
		offset += Varint.bytesLength(nullbitsLength);
		
		nullBits = Arrays.copyOfRange(bytes, offset, offset + nullbitsLength);
		offset += nullBits.length;
		
		parsedOffset = offset;
		
		//dataOffset = Varint.readUnsignedVarInt(bytes, headerLength);
		//dataLength = Varint.readUnsignedVarInt(bytes, headerLength + Varint.bytesLength(dataOffset));
		dataOffset = headerLength + 4; //4 bytes for dataLength
		dataLength = OIntegerSerializer.INSTANCE.deserialize(bytes, headerLength);
		//dataOffset = headerLength + Varint.bytesLength(dataLength);
		
		return offset;
	}

	
	/**
	 * Scan header fields up to the specified property index.  If -1 then scan all headers
	 * @param internalIndex
	 * 
	 */
	private void scanHeader(int internalIndex) {
		if (internalIndex < clazz.fixedLengthPropertyCount())
			//fixed length property, no meta-data to read from header
			return;
		if (parsedProperties > internalIndex)
			//already scanned
			return;
		int fixed = clazz.fixedLengthPropertyCount();
		int limit = internalIndex == -1 ? fieldCount - fixed : internalIndex - fixed + 1;
		
		OSchemaProperty entry;
		
		for (int i = parsedProperties; i < limit; i++) {

			OType type;
			String name;
			int nameId;
			boolean schemaless = true;
			
			if (i >= clazz.variableLengthPropertyCount()) {
				//schemaless: extra meta data to read
				nameId = Varint.readUnsignedVarInt(bytes, parsedOffset);
				name = clazz.getIdProvider().nameFor(nameId);
				parsedOffset += Varint.bytesLength(nameId);
				type = OType.getById(bytes[parsedOffset]);
				parsedOffset++;
			} else {
				//declared variable length field
				OSchemaProperty property = clazz.getVariableLengthProperties().get(i);
				name = property.getName();
				type = property.getType();
				nameId = property.getNameId();
				schemaless = false;
			}
			
			entry = ObjectPool.newRecordHeaderEntry(clazz, name, type);
			entry.setNameId(nameId);
			entry.setSchemaless(schemaless);
			
			/*
			 * nulled values do not store offset or length 
			 */
			if (!isNull(i + fixed)) {
				entry.setInDataOffset(Varint.readUnsignedVarInt(bytes, parsedOffset));
				parsedOffset += Varint.bytesLength(entry.getInDataOffset());
			
				entry.setDataLength(Varint.readUnsignedVarInt(bytes, parsedOffset));
				parsedOffset += Varint.bytesLength(entry.getDataLength());
			}
			
			propertyEntries.add(entry);
			parsedProperties++;
		}
	}
	
	/**
	 * Returns the binary header for the field.  If the field has a null value returns null.
	 * If the field doesn't exist returns IBinaryHeaderEntry.NO_HEADER
	 * @param internalIndex the internal index
	 */
	public IBinaryHeaderEntry fieldHeader(int internalIndex) {
		
		if (internalIndex == -1)
			return IBinaryHeaderEntry.NO_ENTRY;
		
		if (internalIndex < clazz.fixedLengthPropertyCount())
			return clazz.getFixedLengthProperties().get(internalIndex);
		
		//ensure we have read the entry
		scanHeader(internalIndex);
		
		internalIndex -= clazz.fixedLengthPropertyCount();
		return internalIndex  < 0 ? IBinaryHeaderEntry.NO_ENTRY : propertyEntries.get(internalIndex);		
	}
	
	/**
	 * Returns a variable length field header based on it's index within the list
	 * of variable length properties.
	 * 
	 * If no class is set throws a NullPointerException
	 * 
	 * @param index
	 * @return
	 */
	public IBinaryHeaderEntry fieldHeaderVariable(int index) {
		return fieldHeader(index + clazz.fixedLengthPropertyCount());
	}
	
	/**
	 * Returns a schemaless field header based on it's index within the list
	 * of schemaless properties.
	 * 
	 * If no class is set throws a NullPointerException
	 * 
	 * @param index
	 * @return
	 */
	public IBinaryHeaderEntry fieldHeaderSchemaless(int index) {
		return fieldHeader(index + clazz.fixedLengthPropertyCount() + clazz.variableLengthPropertyCount());
	}
	
	/**
	 * @return the total record length including header + data in bytes.
	 */
	public int getRecordLength() {
		return headerLength + Varint.bytesLength(dataLength) + dataLength;
	}
	
	/**
	 * returns the internal index of the property represented by nameId
	 * @param nameId
	 * @return
	 */
	public int indexOf(int nameId) {
		OSchemaProperty property = clazz.getField(nameId);
		if (property != null)
			return property.getInternalOrder();
		//not a schema declared field
		//scan all headers to find it in non-declared fields
		scanHeader(-1);
		OSchemaProperty entry;
		for (int i = 0; i < fieldCount - clazz.fixedLengthPropertyCount(); i++) {
			entry = propertyEntries.get(i);
			if (entry.getNameId() == nameId)
				return i + clazz.fixedLengthPropertyCount();
		}
		return -1;
	}
	
	public int indexOf(String property) {
		int nameId = idProvider.idFor(property);
		return indexOf(nameId);
	}
	
	/**
	 * @param internalIndex the index of property with respect to it's internal order within the record.
	 * @return
	 */
	public boolean isNull(int internalIndex) {
		return Bits.isSet(nullBits, internalIndex);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		parsedProperties = 0;
		
		/**
		 * Tradeoff to think about here.  Recreate new list each use or keep the list and
		 * potentially end up with a large allocated internal array that is never used?
		 * Perhaps clear the list if below a certain size, if larger then discard and make a new
		 * one.  
		 */
		for (OSchemaProperty entry: propertyEntries)
			entry.reset();
		propertyEntries.clear();
		
	}

	/**
	 * @return the clazz
	 */
	public OSchemaVersion getClazz() {
		return clazz;
	}

	/**
	 * @param clazz the clazz to set
	 */
	public void setClazz(OSchemaVersion clazz) {
		this.clazz = clazz;
	}

	/**
	 * @return the classId
	 */
	public int getClassId() {
		return classId;
	}

	/**
	 * @param classId the classId to set
	 */
	public void setClassId(int classId) {
		this.classId = classId;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return the headerLength
	 */
	public int getHeaderLength() {
		return headerLength;
	}

	/**
	 * @param headerLength the headerLength to set
	 */
	public void setHeaderLength(int headerLength) {
		this.headerLength = headerLength;
	}

	/**
	 * @return the fieldCount
	 */
	public int getFieldCount() {
		return fieldCount;
	}

	/**
	 * @param fieldCount the fieldCount to set
	 */
	public void setFieldCount(int fieldCount) {
		this.fieldCount = fieldCount;
	}

	/**
	 * @return the nullBits
	 */
	public byte[] getNullBits() {
		return nullBits;
	}

	/**
	 * @param nullBits the nullBits to set
	 */
	public void setNullBits(byte[] nullBits) {
		this.nullBits = nullBits;
	}

	/**
	 * @return the dataLength
	 */
	public int getDataLength() {
		return dataLength;
	}

	/**
	 * @param dataLength the dataLength to set
	 */
	void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	/**
	 * @return the dataOffset
	 */
	public int getDataOffset() {
		return dataOffset;
	}

	/**
	 * @param dataOffset the dataOffset to set
	 */
	void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}
	

}
