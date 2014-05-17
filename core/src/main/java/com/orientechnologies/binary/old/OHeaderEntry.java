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

//package com.orientechnologies.binary.old;
//
//import com.orientechnologies.binary.IBinHeaderEntry;
//import com.orientechnologies.binary.util.BinUtils;
//import com.orientechnologies.binary.util.IRecyclable;
//import com.orientechnologies.orient.core.metadata.schema.OType;
//
///**
// * 
// * @author Steve Coughlan
// * 
// */
//public class OHeaderEntry implements IRecyclable, IBinHeaderEntry {
//
//	private int nameId;
//	private String name;
//	private OType type;
//	private int dataOffset;
//	private int dataLength;
//
//	private boolean schemaless;
//
//	private boolean mutable = true;
//
//	@Override
//	public void reset() {
//		nameId = -1;
//		schemaless = true;
//		mutable = false;
//		// all other fields are overwritten.
//	}
//
//	@Override
//	public int getNameId() {
//		return nameId;
//	}
//
//	/**
//	 * @param nameId
//	 *            the nameId to set
//	 */
//	public void setNameId(int nameId) {
//		checkMutable();
//		this.nameId = nameId;
//	}
//
//	/**
//	 * @return the name
//	 */
//	public String getName() {
//		return name;
//	}
//
//	/**
//	 * @param name
//	 *            the name to set
//	 */
//	public void setName(String name) {
//		checkMutable();
//		this.name = name;
//	}
//
//	@Override
//	public OType getType() {
//		return type;
//	}
//
//	/**
//	 * @param type
//	 *            the type to set
//	 */
//	public void setType(OType type) {
//		checkMutable();
//		this.type = type;
//	}
//
//	@Override
//	public int getInDataOffset() {
//		return dataOffset;
//	}
//
//	/**
//	 * @param dataOffset
//	 *            the dataOffset to set
//	 */
//	public void setDataOffset(int dataOffset) {
//		checkMutable();
//		this.dataOffset = dataOffset;
//	}
//
//	@Override
//	public int getDataLength() {
//		return dataLength;
//	}
//
//	/**
//	 * @param dataLength
//	 *            the dataLength to set
//	 */
//	public void setDataLength(int dataLength) {
//		checkMutable();
//		this.dataLength = dataLength;
//	}
//
//	/**
//	 * @return the schemaless
//	 */
//	@Override
//	public boolean isSchemaless() {
//		return schemaless;
//	}
//
//	/**
//	 * @param schemaless
//	 *            the schemaless to set
//	 */
//	public void setSchemaless(boolean schemaless) {
//		checkMutable();
//		this.schemaless = schemaless;
//	}
//
//	private void checkMutable() {
//		if (!mutable)
//			throw new RuntimeException("Attempt to modify immutable header entry");
//	}
//
//	void setMutable(boolean mutable) {
//		checkMutable();
//		this.mutable = mutable;
//	}
//	
//	@Override
//	public boolean isFixedLength() {
//		return type == null ? false : BinUtils.isFixedLength(type);
//	}
//
//	public OHeaderEntry getMutableCopy() {
//		// TODO use the object pool
//		OHeaderEntry clone = new OHeaderEntry();
//		clone.nameId = nameId;
//		clone.name = name;
//		clone.type = type;
//		clone.dataOffset = dataOffset;
//		clone.schemaless = schemaless;
//		clone.mutable = true;
//		clone.setMutable(true);
//		return clone;
//	}
//
//	public String toString() {
//		return String.format("%s[%s]:%s %s", nameId, name, type, schemaless ? "schemaless" : "declared");
//	}
//	
//}