/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientechnologies.orient.core.db.record;

import com.orientechnologies.orient.core.db.ODataSegmentStrategy;
import com.orientechnologies.orient.core.db.ODatabaseComplex;
import com.orientechnologies.orient.core.db.record.ridbag.sbtree.OSBTreeCollectionManager;
import com.orientechnologies.orient.core.id.OClusterPosition;
import com.orientechnologies.orient.core.iterator.ORecordIteratorCluster;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.serialization.serializer.binary.OBinarySerializerFactory;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializer;

/**
 * Generic interface for record based Database implementations.
 * 
 * @author Luca Garulli
 */
public interface ODatabaseRecord extends ODatabaseComplex<ORecordInternal<?>> {

  /**
   * Browses all the records of the specified cluster.
   * 
   * @param iClusterName
   *          Cluster name to iterate
   * @return Iterator of ODocument instances
   */
  public <REC extends ORecordInternal<?>> ORecordIteratorCluster<REC> browseCluster(String iClusterName);

  public <REC extends ORecordInternal<?>> ORecordIteratorCluster<REC> browseCluster(String iClusterName,
      OClusterPosition startClusterPosition, OClusterPosition endClusterPosition, boolean loadTombstones);

  /**
   * Browses all the records of the specified cluster of the passed record type.
   * 
   * @param iClusterName
   *          Cluster name to iterate
   * @param iRecordClass
   *          The record class expected
   * @return Iterator of ODocument instances
   */
  public <REC extends ORecordInternal<?>> ORecordIteratorCluster<REC> browseCluster(String iClusterName, Class<REC> iRecordClass);

  public <REC extends ORecordInternal<?>> ORecordIteratorCluster<REC> browseCluster(String iClusterName, Class<REC> iRecordClass,
      OClusterPosition startClusterPosition, OClusterPosition endClusterPosition, boolean loadTombstones);

  /**
   * Returns the record for a OIdentifiable instance. If the argument received already is a ORecord instance, then it's returned as
   * is, otherwise a new ORecord is created with the identity received and returned.
   * 
   * @param iIdentifiable
   * @return A ORecord instance
   */
  public <RET extends ORecordInternal<?>> RET getRecord(OIdentifiable iIdentifiable);

  /**
   * Returns the default record type for this kind of database.
   */
  public byte getRecordType();

  /**
   * Returns true if current configuration retains objects, otherwise false
   * 
   * @see #setRetainRecords(boolean)
   */
  public boolean isRetainRecords();

  /**
   * Specifies if retain handled objects in memory or not. Setting it to false can improve performance on large inserts. Default is
   * enabled.
   * 
   * @param iValue
   *          True to enable, false to disable it.
   * @see #isRetainRecords()
   */
  public ODatabaseRecord setRetainRecords(boolean iValue);

  /**
   * Checks if the operation on a resource is allowed for the current user.
   * 
   * @param iResource
   *          Resource where to execute the operation
   * @param iOperation
   *          Operation to execute against the resource
   * @return The Database instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <DB extends ODatabaseRecord> DB checkSecurity(String iResource, int iOperation);

  /**
   * Checks if the operation on a resource is allowed for the current user. The check is made in two steps:
   * <ol>
   * <li>
   * Access to all the resource as *</li>
   * <li>
   * Access to the specific target resource</li>
   * </ol>
   * 
   * @param iResourceGeneric
   *          Resource where to execute the operation, i.e.: database.clusters
   * @param iOperation
   *          Operation to execute against the resource
   * @param iResourceSpecific
   *          Target resource, i.e.: "employee" to specify the cluster name.
   * @return The Database instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <DB extends ODatabaseRecord> DB checkSecurity(String iResourceGeneric, int iOperation, Object iResourceSpecific);

  /**
   * Checks if the operation against multiple resources is allowed for the current user. The check is made in two steps:
   * <ol>
   * <li>
   * Access to all the resource as *</li>
   * <li>
   * Access to the specific target resources</li>
   * </ol>
   * 
   * @param iResourceGeneric
   *          Resource where to execute the operation, i.e.: database.clusters
   * @param iOperation
   *          Operation to execute against the resource
   * @param iResourcesSpecific
   *          Target resources as an array of Objects, i.e.: ["employee", 2] to specify cluster name and id.
   * @return The Database instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <DB extends ODatabaseRecord> DB checkSecurity(String iResourceGeneric, int iOperation, Object... iResourcesSpecific);

  /**
   * Tells if validation of record is active. Default is true.
   * 
   * @return true if it's active, otherwise false.
   */
  public boolean isValidationEnabled();

  /**
   * Enables or disables the record validation.
   * 
   * @param iEnabled
   *          True to enable, false to disable
   * @return The Database instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <DB extends ODatabaseRecord> DB setValidationEnabled(boolean iEnabled);

  /**
   * @return strategy that is used to assign data segment id for new records in current database.
   */
  public ODataSegmentStrategy getDataSegmentStrategy();

  /**
   * Sets the {@link ODataSegmentStrategy} which will be used for records in this database.
   * 
   * @param dataSegmentStrategy
   *          instance to set
   */
  public void setDataSegmentStrategy(ODataSegmentStrategy dataSegmentStrategy);

  /**
   * Internal. Gets an instance of sb-tree collection manager for current database.
   */
  public OSBTreeCollectionManager getSbTreeCollectionManager();

  /**
   * Internal. Returns the factory that defines a set of components that current database should use to be compatible to current
   * version of storage. So if you open a database create with old version of OrientDB it defines a components that should be used
   * to provide backward compatibility with that version of database.
   */
  public OCurrentStorageComponentsFactory getStorageVersions();

  /**
   * @return the factory of binary serializers.
   */
  public OBinarySerializerFactory getSerializerFactory();

  /**
   * @return serializer which is used for document serialization.
   */
  public ORecordSerializer getSerializer();
}
