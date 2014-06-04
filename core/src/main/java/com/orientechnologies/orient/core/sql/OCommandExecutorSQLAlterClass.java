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
package com.orientechnologies.orient.core.sql;

import com.orientechnologies.orient.core.command.OCommandDistributedReplicateRequest;
import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.command.OCommandRequestText;
import com.orientechnologies.orient.core.db.record.ODatabaseRecord;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.id.OClusterPosition;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.ATTRIBUTES;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.OBinaryProtocol;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializerFactory;
import com.orientechnologies.orient.core.serialization.serializer.record.string.ORecordSerializerSchemaAware2CSV;
import com.orientechnologies.orient.core.storage.OPhysicalPosition;
import com.orientechnologies.orient.core.storage.ORawBuffer;
import com.orientechnologies.orient.core.storage.OStorage;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * SQL ALTER PROPERTY command: Changes an attribute of an existent property in the target class.
 * 
 * @author Luca Garulli
 * 
 */
@SuppressWarnings("unchecked")
public class OCommandExecutorSQLAlterClass extends OCommandExecutorSQLAbstract implements OCommandDistributedReplicateRequest {
  public static final String KEYWORD_ALTER = "ALTER";
  public static final String KEYWORD_CLASS = "CLASS";

  private String             className;
  private ATTRIBUTES         attribute;
  private String             value;

  public OCommandExecutorSQLAlterClass parse(final OCommandRequest iRequest) {
    final ODatabaseRecord database = getDatabase();

    init((OCommandRequestText) iRequest);

    StringBuilder word = new StringBuilder();

    int oldPos = 0;
    int pos = nextWord(parserText, parserTextUpperCase, oldPos, word, true);
    if (pos == -1 || !word.toString().equals(KEYWORD_ALTER))
      throw new OCommandSQLParsingException("Keyword " + KEYWORD_ALTER + " not found", parserText, oldPos);

    oldPos = pos;
    pos = nextWord(parserText, parserTextUpperCase, oldPos, word, true);
    if (pos == -1 || !word.toString().equals(KEYWORD_CLASS))
      throw new OCommandSQLParsingException("Keyword " + KEYWORD_CLASS + " not found", parserText, oldPos);

    oldPos = pos;
    pos = nextWord(parserText, parserTextUpperCase, oldPos, word, false);
    if (pos == -1)
      throw new OCommandSQLParsingException("Expected <class>", parserText, oldPos);

    className = word.toString();

    oldPos = pos;
    pos = nextWord(parserText, parserTextUpperCase, oldPos, word, true);
    if (pos == -1)
      throw new OCommandSQLParsingException("Missed the class's attribute to change", parserText, oldPos);

    final String attributeAsString = word.toString();

    try {
      attribute = OClass.ATTRIBUTES.valueOf(attributeAsString.toUpperCase(Locale.ENGLISH));
    } catch (IllegalArgumentException e) {
      throw new OCommandSQLParsingException("Unknown class's attribute '" + attributeAsString + "'. Supported attributes are: "
          + Arrays.toString(OClass.ATTRIBUTES.values()), parserText, oldPos);
    }

    value = parserText.substring(pos + 1).trim();

    if (value.length() == 0)
      throw new OCommandSQLParsingException("Missed the property's value to change for attribute '" + attribute + "'", parserText,
          oldPos);

    if (value.equalsIgnoreCase("null"))
      value = null;

    return this;
  }

  /**
   * Execute the ALTER CLASS.
   */
  public Object execute(final Map<Object, Object> iArgs) {
    final ODatabaseRecord database = getDatabase();

    if (attribute == null)
      throw new OCommandExecutionException("Cannot execute the command because it has not been parsed yet");

    final OClassImpl cls = (OClassImpl) database.getMetadata().getSchema().getClass(className);
    if (cls == null)
      throw new OCommandExecutionException("Source class '" + className + "' not found");

    final Object result = cls.setInternalAndSave(attribute, value);

    if (OClass.ATTRIBUTES.NAME.equals(attribute))
      renameClass(database, cls);

    renameCluster();

    return result;
  }

  public String getSyntax() {
    return "ALTER CLASS <class> <attribute-name> <attribute-value>";
  }

  protected void renameClass(ODatabaseRecord database, OClassImpl cls) {
    final OStorage storage = database.getStorage();

    for (int clusterId : cls.getClusterIds()) {
      OClusterPosition[] range = storage.getClusterDataRange(clusterId);

      OPhysicalPosition[] positions = storage.ceilingPhysicalPositions(clusterId, new OPhysicalPosition(range[0]));
      do {
        for (OPhysicalPosition position : positions) {
          final ORecordId identity = new ORecordId(clusterId, position.clusterPosition);
          final ORawBuffer record = storage.readRecord(identity, null, true, null, false, OStorage.LOCKING_STRATEGY.DEFAULT)
              .getResult();

          if (!database.getStorageVersions().classesAreDetectedByClusterId() && record.recordType == ODocument.RECORD_TYPE) {
            final ORecordSerializerSchemaAware2CSV serializer = (ORecordSerializerSchemaAware2CSV) ORecordSerializerFactory
                .instance().getFormat(ORecordSerializerSchemaAware2CSV.NAME);

            if (serializer.getClassName(OBinaryProtocol.bytes2string(record.buffer)).equalsIgnoreCase(className)) {
              final ODocument document = new ODocument();
              document.setLazyLoad(false);
              document.fromStream(record.buffer);
              document.getRecordVersion().copyFrom(record.version);
              document.setIdentity(identity);
              document.setClassName(cls.getName());
              document.setDirty();
              document.save();
            }
          }

          if (positions.length > 0)
            positions = storage.higherPhysicalPositions(clusterId, positions[positions.length - 1]);
        }
      } while (positions.length > 0);
    }
  }

  private void renameCluster() {
    final ODatabaseRecord database = getDatabase();
    if (attribute.equals(OClass.ATTRIBUTES.NAME) && checkClusterRenameOk(database.getStorage().getClusterIdByName(value))) {
      database.command(new OCommandSQL("alter cluster " + className + " name " + value)).execute();
    }
  }

  private boolean checkClusterRenameOk(int clusterId) {
    final ODatabaseRecord database = getDatabase();
    for (OClass clazz : database.getMetadata().getSchema().getClasses()) {
      if (clazz.getName().equals(value))
        continue;
      else if (clazz.getDefaultClusterId() == clusterId || Arrays.asList(clazz.getClusterIds()).contains(clusterId))
        return false;
    }
    return true;
  }
}
