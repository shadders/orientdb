package com.orientechnologies.binary.old;

import com.orientechnologies.binary.IBinaryHeaderEntry;
import com.orientechnologies.binary.util.IRecyclable;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * 
 * @author Steve Coughlan
 *
 */
public class OHeaderEntry implements IRecyclable, IBinaryHeaderEntry {
	
	private int nameId;
	private OType type;
	private int dataOffset;
	private int dataLength;
	
	private boolean schemaless;
	
	private boolean mutable = true;

	@Override
	public void reset() {
		nameId = -1;
		schemaless = true;
		mutable = false;
		//all other fields are overwritten.
	}

	/* (non-Javadoc)
	 * @see com.orientechnologies.binary.IBinaryHeaderEntry#getNameId()
	 */
	@Override
	public int getNameId() {
		return nameId;
	}

	/**
	 * @param nameId the nameId to set
	 */
	public void setNameId(int nameId) {
		checkMutable();
		this.nameId = nameId;
	}

	@Override
	public OType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(OType type) {
		checkMutable();
		this.type = type;
	}

	@Override
	public int getInDataOffset() {
		return dataOffset;
	}

	/**
	 * @param dataOffset the dataOffset to set
	 */
	public void setDataOffset(int dataOffset) {
		checkMutable();
		this.dataOffset = dataOffset;
	}

	@Override
	public int getDataLength() {
		return dataLength;
	}

	/**
	 * @param dataLength the dataLength to set
	 */
	public void setDataLength(int dataLength) {
		checkMutable();
		this.dataLength = dataLength;
	}

	/**
	 * @return the schemaless
	 */
	@Override
	public boolean isSchemaless() {
		return schemaless;
	}

	/**
	 * @param schemaless the schemaless to set
	 */
	public void setSchemaless(boolean schemaless) {
		checkMutable();
		this.schemaless = schemaless;
	}
	
	private void checkMutable() {
		if (!mutable)
			throw new RuntimeException("Attempt to modify immutable header entry");
	}
	
	void setMutable(boolean mutable) {
		checkMutable();
		this.mutable = mutable;
	}
	
	
	public OHeaderEntry getMutableCopy() {
		try {
			//TODO do this manually do we can use the object pool
			OHeaderEntry clone = (OHeaderEntry) this.clone();
			clone.setMutable(true);
			return clone;
		} catch (CloneNotSupportedException e) {
			//should never happen
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isFixedLength() {
		// TODO Auto-generated method stub
		return false;
	}
	
}