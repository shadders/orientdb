package com.orientechnologies.binary;

import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * 
 * An interface for header entries contained within an OBinRecordHeader.  Each entry
 * represents a property of the record.  
 * 
 * @author Steve Coughlan
 *
 */
public interface IBinHeaderEntry {
	
	/**
	 * Indicates no entry was found as opposed to an entry with a null value.
	 */
	public static final IBinHeaderEntry NO_ENTRY = new OBinProperty(false, null);
	
	/**
	 * @return the nameId
	 */
	public int getNameId();
	
	/**
	 * The field name of the property.  This is known at all times for schema declared
	 * properties.  For schemaless properties it should be known when a record is being written.
	 * However when a record is being read it cannot be known until nameId is set (read from the
	 * header).
	 */
	public String getName();

	/**
	 * @return the type
	 */
	public OType getType();

	/**
	 * The offset relative to beginning of the data portion of record.  The data portion
	 * begins with a varint8 for total data length so the offset for field 0
	 * would be the length of the data length varint.
	 * @return the dataOffset
	 */
	public int getInDataOffset();

	/**
	 * @return the dataLength in bytes
	 */
	public int getDataLength();

	/**
	 * Indicates the property is ad-hoc and not part of the schema.
	 * @return
	 */
	public boolean isSchemaless();

	/**
	 * Whether the property is a fixed length OType
	 * @return
	 */
	public boolean isFixedLength();

}