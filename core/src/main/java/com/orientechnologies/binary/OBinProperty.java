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

import com.orientechnologies.binary.old.Strings;
import com.orientechnologies.binary.util.BinUtils;
import com.orientechnologies.binary.util.IRecyclable;
import com.orientechnologies.orient.core.db.record.ORecordElement;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OPropertyImpl;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * 
 * @author Steve Coughlan
 *
 */
public class OBinProperty extends OPropertyImpl implements IBinHeaderEntry, IRecyclable, Cloneable {

	// private String name;

	private int nameId = -1;

	/**
	 * User defined order. Internally fields will be rearranged to group fixed
	 * dataLength fields first.
	 */
	private int userOrder;

	/**
	 * Internal order after fields have been rearranged to place fixed length fields first
	 */
	private int internalOrder;

	/**
	 * offset and length of the data within the parent version of the
	 * OClassVersion. This is only stored for fixed length fields.
	 */
	private int inDataOffset;
	private int dataLength;

	private boolean schemaless;
	private boolean mutable = true;
	
	public OBinProperty() {
		super(OClassIndex.SCHEMALESS);
	}

	public OBinProperty(OClassImpl iOwner, ODocument iDocument) {
		super(iOwner, iDocument);
		checkOwner(iOwner);
	}

	public OBinProperty(OClassImpl iOwner, String iName, OType iType) {
		super(iOwner, iName, iType);
		checkOwner(iOwner);
	}

	public OBinProperty(OClassImpl iOwner) {
		super(iOwner);
		checkOwner(iOwner);
	}
	
	private void checkOwner(OClassImpl iOwner) {
		if (iOwner instanceof OClassSet)
			return;
		throw new RuntimeException("owner class is not an INSTANCE of OClassVersion");
	}

	/**
	 * Only used to create IBinHeaderEntry.NO_ENTRY
	 */
	OBinProperty(boolean mutable, OClassImpl iOwner) {
		super(iOwner == null ? OClassIndex.SCHEMALESS : iOwner);
	}

	public void reset() {
		nameId = -1;
		schemaless = true;
		mutable = false;
		setType(null);
		// all other internal fields are overwritten.
		// TODO what supertype fields?
	}

	/**
	 * This should be retrieved from IPropertyIdProvider.nameFor(nameId) to
	 * ensure the same INSTANCE of each String is always used (faster map
	 * lookups)
	 * 
	 * @return the name
	 */
	public String getName() {
		return super.getName();
	}

	/**
	 * @param name
	 *            the name to set
	 * @return
	 */
	public OProperty setName(String name) {
		checkMutable();
		return super.setName(Strings.identity(name));
	}
	
	public void setNameInternal(String name) {
		checkMutable();
		super.setNameInternal(Strings.identity(name));
	}

	/**
	 * @return the nameId
	 */
	public int getNameId() {
		if (nameId == -1) {
			String name = getName();
			nameId = getClassSet().idFor(name);
		}
		return nameId;
	}
	
	private OClassSet getClassSet() {
		return (OClassSet) getOwnerClass();
	}

	/**
	 * @param nameId
	 *            the nameId to set
	 */
	public void setNameId(int nameId) {
		checkMutable();
		this.nameId = nameId;
	}

	/**
	 * @return the User defined userOrder. Internally fields will be rearranged to group fixed
	 * dataLength fields first.
	 */
	public int getUserOrder() {
		return userOrder;
	}

	/**
	 * @param userOrder
	 *            the User defined userOrder. Internally fields will be rearranged to group fixed
	 * dataLength fields first.
	 */
	public void setUserOrder(int order) {
		checkMutable();
		this.userOrder = order;
	}

	/**
	 * @return the Internal order after fields have been rearranged to place fixed length fields first
	 */
	public int getInternalOrder() {
		return internalOrder;
	}

	/**
	 * @param internalOrder
	 *            the Internal order after fields have been rearranged to place fixed length fields first
	 */
	public void setInternalOrder(int internalOrder) {
		checkMutable();
		this.internalOrder = internalOrder;
	}

	/**
	 * Offset of data within the data section of the record (excludes the header)
	 * @return the inDataOffset
	 */
	public int getInDataOffset() {
		return inDataOffset;
	}

	/**
	 * @param inDataOffset
	 *            the Offset of data within the data section of the record (excludes the header)
	 */
	public void setInDataOffset(int dataOffset) {
		checkMutable();
		this.inDataOffset = dataOffset;
	}

	/**
	 * @return the dataLength
	 */
	public int getDataLength() {
		return dataLength;
	}

	/**
	 * @param dataLength
	 *            the dataLength to set
	 */
	public void setDataLength(int dataLength) {
		checkMutable();
		this.dataLength = dataLength;
	}

	/**
	 * @return the fixedLength
	 */
	public boolean isFixedLength() {
		//return BinUtils.isFixedLength(getLinkedType());
		return BinUtils.isFixedLength(this);
	}

	/**
	 * @return the schemaless
	 */
	public boolean isSchemaless() {
		return schemaless;
	}

	/**
	 * @param schemaless
	 *            the schemaless to set
	 */
	public void setSchemaless(boolean schemaless) {
		checkMutable();
		this.schemaless = schemaless;
	}

	void setMutable(boolean mutable) {
		//allow set immutable even if already immutable
		if (mutable)
			checkMutable();
		this.mutable = mutable;
	}
	
	void setMutableInternal(boolean mutable) {
		this.mutable = mutable;
	}

	private void checkMutable() {
		if (!mutable)
			throw new RuntimeException("Attempt to modify immutable header entry");
	}

	public OBinProperty getMutableCopy() {
		try {
			// TODO do this manually do we can use the object pool
			OBinProperty clone = (OBinProperty) this.clone();
			clone.mutable = true;
			return clone;
		} catch (CloneNotSupportedException e) {
			// should never happen
			throw new RuntimeException(e);
		}
	}

	@Override
	public void fromStream() {
		super.fromStream();

		// marshall additional properties
		nameId = document.field("nameId");
		
		//OType is handled by super
		
		//FIXME one of these is not needed, work out which
		userOrder = document.field("userOrder");
		internalOrder = document.field("internalOrder");
		setSchemaless(false);
		setMutable(false);
	}

	@Override
	public ODocument toStream() {
		/*
		 * FIXME
		 * WARNING calling super.toStream sets status to UNMAsuperTRSHALLING then LOADED so
		 * we are repeating that cycle which means document is temporarily set LOADED
		 * even when not finished.
		 */
		document = super.toStream();
		
		document.setInternalStatus(ORecordElement.STATUS.UNMARSHALLING);

		try {

			document.field("nameId", nameId);
			
			//FIXME one of these is not needed, work out which
			document.field("userOrder", userOrder);
			document.field("internalOrder", internalOrder);
			
			//OType is handled by super
			
		} finally {
			document.setInternalStatus(ORecordElement.STATUS.LOADED);
		}
		return document;
	}
	
	public String toString() {
		return String.format("%s[%s] off: %s len: %s", getName(), getType(), inDataOffset, dataLength);
	}

	@Override
	public OBinProperty asProperty() {
		return this;
	}

}
