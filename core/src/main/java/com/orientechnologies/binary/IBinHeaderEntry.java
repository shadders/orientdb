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
	
	/**
	 * @return a representation of the header entry as a property.  In most cases it will be 'this'
	 */
	public OBinProperty asProperty();

}