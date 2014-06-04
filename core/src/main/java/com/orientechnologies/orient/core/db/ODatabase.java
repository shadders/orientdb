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
package com.orientechnologies.orient.core.db;

import com.orientechnologies.orient.core.cache.OLevel1RecordCache;
import com.orientechnologies.orient.core.cache.OLevel2RecordCache;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.intent.OIntent;
import com.orientechnologies.orient.core.storage.ORecordMetadata;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.core.storage.OStorage.CLUSTER_TYPE;
import com.orientechnologies.orient.core.util.OBackupable;

import java.io.Closeable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Generic Database interface. Represents the lower level of the Database providing raw API to access to the raw records.<br/>
 * Limits:
 * <ul>
 * <li>Maximum records per cluster/class = <b>9.223.372.036 Billions</b>: 2^63 = 9.223.372.036.854.775.808 records</li>
 * <li>Maximum records per database = <b>302.231.454.903.657 Billions</b>: 2^15 clusters x 2^63 records = (2^78) 32.768 *
 * 9,223.372.036.854.775.808 = 302.231,454.903.657.293.676.544 records</li>
 * <li>Maximum storage per data-segment = <b>9.223.372 Terabytes</b>: 2^63 bytes = 9,223.372.036.854.775.808 Exabytes</li>
 * <li>Maximum storage per database = <b>19.807.040.628.566.084 Terabytes</b>: 2^31 data-segments x 2^63 bytes = (2^94)
 * 2.147.483.648 x 9,223.372.036.854.775.808 Exabytes = 19.807,040.628.566.084.398.385.987.584 Yottabytes</li>
 * </ul>
 * 
 * @author Luca Garulli
 * 
 */
public interface ODatabase extends OBackupable, Closeable {
  public static enum OPTIONS {
    SECURITY
  }

  public static enum STATUS {
    OPEN, CLOSED, IMPORTING
  }

  public static enum ATTRIBUTES {
    TYPE, STATUS, DEFAULTCLUSTERID, DATEFORMAT, DATETIMEFORMAT, TIMEZONE, LOCALECOUNTRY, LOCALELANGUAGE, CHARSET, CUSTOM, CLUSTERSELECTION, MINIMUMCLUSTERS
  }

  /**
   * Opens a database using the user and password received as arguments.
   * 
   * @param iUserName
   *          Username to login
   * @param iUserPassword
   *          Password associated to the user
   * @return The Database instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <DB extends ODatabase> DB open(final String iUserName, final String iUserPassword);

  /**
   * Creates a new database.
   * 
   * @return The Database instance itself giving a "fluent interface". Useful to call multiple methods in chain.
   */
  public <DB extends ODatabase> DB create();

  /**
   * Reloads the database information like the cluster list.
   */
  public void reload();

  /**
   * Drops a database.
   * 
   * @throws ODatabaseException
   *           if database is closed.
   */
  public void drop();

  /**
   * Declares an intent to the database. Intents aim to optimize common use cases.
   * 
   * @param iIntent
   *          The intent
   */
  public boolean declareIntent(final OIntent iIntent);

  /**
   * Checks if the database exists.
   * 
   * @return True if already exists, otherwise false.
   */
  public boolean exists();

  /**
   * Closes an opened database.
   */
  public void close();

  /**
   * Returns the current status of database.
   */
  public STATUS getStatus();

  /**
   * Returns the total size of database as the real used space.
   */
  public long getSize();

  /**
   * Returns the current status of database.
   */
  public <DB extends ODatabase> DB setStatus(STATUS iStatus);

  /**
   * Returns the database name.
   * 
   * @return Name of the database
   */
  public String getName();

  /**
   * Returns the database URL.
   * 
   * @return URL of the database
   */
  public String getURL();

  /**
   * Returns the underlying storage implementation.
   * 
   * @return The underlying storage implementation
   * @see OStorage
   */
  public OStorage getStorage();

  /**
   * Internal only: replace the storage with a new one.
   * 
   * @param iNewStorage
   *          The new storage to use. Usually it's a wrapped instance of the current cluster.
   */
  public void replaceStorage(OStorage iNewStorage);

  /**
   * Returns the level1 cache. Cannot be null.
   * 
   * @return Current cache.
   */
  public OLevel1RecordCache getLevel1Cache();

  /**
   * Returns the level1 cache. Cannot be null.
   * 
   * @return Current cache.
   */
  public OLevel2RecordCache getLevel2Cache();

  /**
   * Returns the data segment id by name.
   * 
   * @param iDataSegmentName
   *          Data segment name
   * @return The id of searched data segment.
   */
  public int getDataSegmentIdByName(String iDataSegmentName);

  public String getDataSegmentNameById(int dataSegmentId);

  /**
   * Returns the default cluster id. If not specified all the new entities will be stored in the default cluster.
   * 
   * @return The default cluster id
   */
  public int getDefaultClusterId();

  /**
   * Returns the number of clusters.
   * 
   * @return Number of the clusters
   */
  public int getClusters();

  /**
   * Returns true if the cluster exists, otherwise false.
   * 
   * @param iClusterName
   *          Cluster name
   * @return true if the cluster exists, otherwise false
   */
  public boolean existsCluster(String iClusterName);

  /**
   * Returns all the names of the clusters.
   * 
   * @return Collection of cluster names.
   */
  public Collection<String> getClusterNames();

  /**
   * Returns the cluster id by name.
   * 
   * @param iClusterName
   *          Cluster name
   * @return The id of searched cluster.
   */
  public int getClusterIdByName(String iClusterName);

  /**
   * Returns the cluster type.
   * 
   * @param iClusterName
   *          Cluster name
   * @return The cluster type as string
   */
  public String getClusterType(String iClusterName);

  /**
   * Returns the cluster name by id.
   * 
   * @param iClusterId
   *          Cluster id
   * @return The name of searched cluster.
   */
  public String getClusterNameById(int iClusterId);

  /**
   * Returns the total size of records contained in the cluster defined by its name.
   * 
   * @param iClusterName
   *          Cluster name
   * @return Total size of records contained.
   */
  public long getClusterRecordSizeByName(String iClusterName);

  /**
   * Returns the total size of records contained in the cluster defined by its id.
   * 
   * @param iClusterId
   *          Cluster id
   * @return The name of searched cluster.
   */
  public long getClusterRecordSizeById(int iClusterId);

  /**
   * Checks if the database is closed.
   * 
   * @return true if is closed, otherwise false.
   */
  public boolean isClosed();

  /**
   * Counts all the entities in the specified cluster id.
   * 
   * @param iCurrentClusterId
   *          Cluster id
   * @return Total number of entities contained in the specified cluster
   */
  public long countClusterElements(int iCurrentClusterId);

  public long countClusterElements(int iCurrentClusterId, boolean countTombstones);

  /**
   * Counts all the entities in the specified cluster ids.
   * 
   * @param iClusterIds
   *          Array of cluster ids Cluster id
   * @return Total number of entities contained in the specified clusters
   */
  public long countClusterElements(int[] iClusterIds);

  public long countClusterElements(int[] iClusterIds, boolean countTombstones);

  /**
   * Counts all the entities in the specified cluster name.
   * 
   * @param iClusterName
   *          Cluster name
   * @return Total number of entities contained in the specified cluster
   */
  public long countClusterElements(String iClusterName);

  /**
   * Adds a new cluster.
   * 
   * @param iClusterName
   *          Cluster name
   * @param iType
   *          Cluster type between the defined ones
   * @param iParameters
   *          Additional parameters to pass to the factories
   * @return Cluster id
   */
  public int addCluster(String iClusterName, CLUSTER_TYPE iType, Object... iParameters);

  /**
   * Adds a new cluster.
   * 
   * @param iType
   *          Cluster type between the defined ones
   * @param iClusterName
   *          Cluster name
   * @param iDataSegmentName
   *          Data segment where to store record of this cluster. null means 'default'
   * @param iParameters
   *          Additional parameters to pass to the factories
   * 
   * @return Cluster id
   */
  public int addCluster(String iType, String iClusterName, String iLocation, final String iDataSegmentName, Object... iParameters);

  /**
   * Adds a new cluster.
   * 
   * @param iType
   *          Cluster type between the defined ones
   * @param iClusterName
   *          Cluster name
   * @param iRequestedId
   *          requested id of the cluster
   * @param iDataSegmentName
   *          Data segment where to store record of this cluster. null means 'default'
   * @param iParameters
   *          Additional parameters to pass to the factories
   * 
   * @return Cluster id
   */
  public int addCluster(String iType, String iClusterName, int iRequestedId, String iLocation, final String iDataSegmentName,
      Object... iParameters);

  /**
   * Drops a cluster by its name. Physical clusters will be completely deleted
   * 
   * @param iClusterName
   *          the name of the cluster
   * @return true if has been removed, otherwise false
   */
  public boolean dropCluster(String iClusterName, final boolean iTruncate);

  /**
   * Drops a cluster by its id. Physical clusters will be completely deleted.
   * 
   * @param iClusterId
   *          id of cluster to delete
   * @return true if has been removed, otherwise false
   */
  public boolean dropCluster(int iClusterId, final boolean iTruncate);

  /**
   * Adds a data segment where to store record content. Data segments contain the content of records. Cluster segments contain the
   * pointer to them.
   */
  public int addDataSegment(String iSegmentName, String iLocation);

  /**
   * Drop a data segment and all the contained data.
   * 
   * @param name
   *          segment name
   * @return true if the segment has been removed, otherwise false
   */
  public boolean dropDataSegment(String name);

  /**
   * Sets a property value
   * 
   * @param iName
   *          Property name
   * @param iValue
   *          new value to set
   * @return The previous value if any, otherwise null
   */
  public Object setProperty(String iName, Object iValue);

  /**
   * Gets the property value.
   * 
   * @param iName
   *          Property name
   * @return The previous value if any, otherwise null
   */
  public Object getProperty(String iName);

  /**
   * Returns an iterator of the property entries
   */
  public Iterator<Map.Entry<String, Object>> getProperties();

  /**
   * Returns a database attribute value
   * 
   * @param iAttribute
   *          Attributes between #ATTRIBUTES enum
   * @return The attribute value
   */
  public Object get(ATTRIBUTES iAttribute);

  /**
   * Sets a database attribute value
   * 
   * @param iAttribute
   *          Attributes between #ATTRIBUTES enum
   * @param iValue
   *          Value to set
   * @return underlying
   */
  public <DB extends ODatabase> DB set(ATTRIBUTES iAttribute, Object iValue);

  /**
   * Registers a listener to the database events.
   * 
   * @param iListener
   *          the listener to register
   */
  public void registerListener(ODatabaseListener iListener);

  /**
   * Unregisters a listener to the database events.
   * 
   * @param iListener
   *          the listener to unregister
   */
  public void unregisterListener(ODatabaseListener iListener);

  public <V> V callInLock(Callable<V> iCallable, boolean iExclusiveLock);

  public <V> V callInRecordLock(Callable<V> iCallable, ORID rid, boolean iExclusiveLock);

  public ORecordMetadata getRecordMetadata(final ORID rid);

  /**
   * Flush cached storage content to the disk.
   * 
   * After this call users can perform only select queries. All write-related commands will queued till {@link #release()} command
   * will be called.
   * 
   * Given command waits till all on going modifications in indexes or DB will be finished.
   * 
   * IMPORTANT: This command is not reentrant.
   */
  public void freeze();

  /**
   * Allows to execute write-related commands on DB. Called after {@link #freeze()} command.
   */
  public void release();

  /**
   * Flush cached storage content to the disk.
   * 
   * After this call users can perform only select queries. All write-related commands will queued till {@link #release()} command
   * will be called or exception will be thrown on attempt to modify DB data. Concrete behaviour depends on
   * <code>throwException</code> parameter.
   * 
   * IMPORTANT: This command is not reentrant.
   * 
   * @param throwException
   *          If <code>true</code> {@link com.orientechnologies.common.concur.lock.OModificationOperationProhibitedException}
   *          exception will be thrown in case of write command will be performed.
   */
  public void freeze(boolean throwException);

  /**
   * Flush cached cluster content to the disk.
   * 
   * After this call users can perform only select queries. All write-related commands will queued till {@link #releaseCluster(int)}
   * command will be called.
   * 
   * Given command waits till all on going modifications in indexes or DB will be finished.
   * 
   * IMPORTANT: This command is not reentrant.
   * 
   * @param iClusterId
   *          that must be released
   */
  public void freezeCluster(int iClusterId);

  /**
   * Allows to execute write-related commands on the cluster
   * 
   * @param iClusterId
   *          that must be released
   */
  public void releaseCluster(int iClusterId);

  /**
   * Flush cached cluster content to the disk.
   * 
   * After this call users can perform only select queries. All write-related commands will queued till {@link #releaseCluster(int)}
   * command will be called.
   * 
   * Given command waits till all on going modifications in indexes or DB will be finished.
   * 
   * IMPORTANT: This command is not reentrant.
   * 
   * @param iClusterId
   *          that must be released
   * @param throwException
   *          If <code>true</code> {@link com.orientechnologies.common.concur.lock.OModificationOperationProhibitedException}
   *          exception will be thrown in case of write command will be performed.
   */
  public void freezeCluster(int iClusterId, boolean throwException);
}
