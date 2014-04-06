package com.orientechnologies.binary;

import com.orientechnologies.orient.core.metadata.schema.OType;

public interface IBinaryHeaderEntry {
	
	/**
	 * Indicates no entry was found as opposed to an entry with a null value.
	 */
	public static final IBinaryHeaderEntry NO_ENTRY = new OSchemaProperty(false, null);
	
	/**
	 * @return the nameId
	 */
	public int getNameId();

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

	public boolean isSchemaless();
	
	public boolean isFixedLength();

}