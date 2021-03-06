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

package com.orientechnologies.binary.util;

import com.orientechnologies.binary.OBinRecordHeader;
import com.orientechnologies.binary.OBinProperty;
import com.orientechnologies.binary.OClassVersion;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Class for providing reusable instances of Objects
 * 
 * @author Steve Coughlan
 *
 */
public class ObjectPool {


	public static OBinRecordHeader newRecordHeader() {
		return new OBinRecordHeader();
	}
	
	public static OBinProperty newRecordHeaderEntry(OClassVersion clazz, String name, OType type) {
		return new OBinProperty(clazz.getClassSet(), name, type);
	}
	
	public static void release(IRecyclable recyclable) {
		recyclable.reset();
		//determine type and put it back in the object pool.
	}

}

