/*
 * Copyright 2010-2013 Luca Garulli (l.garulli--at--orientechnologies.com)
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
package com.orientechnologies.orient.server.distributed;

import com.orientechnologies.common.io.OFileUtils;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Running server instance.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 */
public class ServerRun {
  protected final String serverId;
  protected String       rootPath;
  protected OServer      server;

  public ServerRun(final String iRootPath, final String serverId) {
    this.rootPath = iRootPath;
    this.serverId = serverId;
  }

  public static String getServerHome(final String iServerId) {
    return "target/server" + iServerId;
  }

  public static String getDatabasePath(final String iServerId, final String iDatabaseName) {
    return getServerHome(iServerId) + "/databases/" + iDatabaseName;
  }

  public OServer getServerInstance() {
    return server;
  }

  public String getServerId() {
    return serverId;
  }

  public String getBinaryProtocolAddress() {
    return server.getListenerByProtocol(ONetworkProtocolBinary.class).getListeningAddress();
  }

  public void deleteNode() {
    OFileUtils.deleteRecursively(new File(getServerHome()));
  }

  protected ODatabaseDocumentTx createDatabase(final String iName) {
    OGlobalConfiguration.STORAGE_KEEP_OPEN.setValue(false);

    String dbPath = getDatabasePath(iName);

    new File(dbPath).mkdirs();

    final ODatabaseDocumentTx database = new ODatabaseDocumentTx("plocal:" + dbPath);
    if (database.exists()) {
      System.out.println("Dropping previous database '" + iName + "' under: " + dbPath + "...");
      OFileUtils.deleteRecursively(new File(dbPath));
    }

    System.out.println("Creating database '" + iName + "' under: " + dbPath + "...");
    database.create();
    return database;
  }

  protected void copyDatabase(final String iDatabaseName, final String iDestinationDirectory) throws IOException {
    // COPY THE DATABASE TO OTHER DIRECTORIES
    System.out.println("Dropping any previous database '" + iDatabaseName + "' under: " + iDatabaseName + "...");
    OFileUtils.deleteRecursively(new File(iDestinationDirectory));

    System.out.println("Copying database folder " + iDatabaseName + " to " + iDestinationDirectory + "...");
    OFileUtils.copyDirectory(new File(getDatabasePath(iDatabaseName)), new File(iDestinationDirectory));
  }

  protected OServer startServer(final String iServerConfigFile) throws Exception, InstantiationException, IllegalAccessException,
      ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IOException {
    System.out.println("Starting server " + serverId + " from " + getServerHome() + "...");

    System.setProperty("ORIENTDB_HOME", getServerHome());

    server = new OServer();
    server.setServerRootDirectory(getServerHome());
    server.startup(getClass().getClassLoader().getResourceAsStream(iServerConfigFile));
    server.activate();
    return server;
  }

  protected void shutdownServer() {
    if (server != null)
      server.shutdown();
  }

  protected String getServerHome() {
    return getServerHome(serverId);
  }

  protected String getDatabasePath(final String iDatabaseName) {
    return getDatabasePath(serverId, iDatabaseName);
  }

}
