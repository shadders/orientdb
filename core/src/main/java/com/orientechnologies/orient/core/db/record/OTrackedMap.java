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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Implementation of LinkedHashMap bound to a source ORecord object to keep track of changes. This avoid to call the makeDirty() by
 * hand when the map is changed.
 * 
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 * 
 */
@SuppressWarnings("serial")
public class OTrackedMap<T> extends LinkedHashMap<Object, T> implements ORecordElement, OTrackedMultiValue<Object, T>, Serializable {
  final protected ORecord<?>                        sourceRecord;
  private STATUS                                    status          = STATUS.NOT_LOADED;
  private Set<OMultiValueChangeListener<Object, T>> changeListeners = Collections
                                                                        .newSetFromMap(new WeakHashMap<OMultiValueChangeListener<Object, T>, Boolean>());
  protected Class<?>                                genericClass;
  private final boolean                             embeddedCollection;

  public OTrackedMap(final ORecord<?> iRecord, final Map<Object, T> iOrigin, final Class<?> cls) {
    this(iRecord);
    genericClass = cls;
    if (iOrigin != null && !iOrigin.isEmpty())
      putAll(iOrigin);
  }

  public OTrackedMap(final ORecord<?> iSourceRecord) {
    this.sourceRecord = iSourceRecord;
    embeddedCollection = this.getClass().equals(OTrackedMap.class);
  }

  @Override
  public ORecordElement getOwner() {
    return sourceRecord;
  }

  @Override
  public T put(final Object key, final T value) {
    boolean containsKey = containsKey(key);

    T oldValue = super.put(key, value);

    if (containsKey && oldValue == value)
      return oldValue;

    if (oldValue instanceof ODocument)
      ((ODocument) oldValue).removeOwner(this);

    addOwnerToEmbeddedDoc(value);

    if (containsKey)
      fireCollectionChangedEvent(new OMultiValueChangeEvent<Object, T>(OMultiValueChangeEvent.OChangeType.UPDATE, key, value,
          oldValue));
    else
      fireCollectionChangedEvent(new OMultiValueChangeEvent<Object, T>(OMultiValueChangeEvent.OChangeType.ADD, key, value));

    return oldValue;
  }

  private void addOwnerToEmbeddedDoc(T e) {
    if (embeddedCollection && e instanceof ODocument && !((ODocument) e).getIdentity().isValid())
      ((ODocument) e).addOwner(this);
  }

  @Override
  public T remove(final Object iKey) {
    boolean containsKey = containsKey(iKey);
    final T oldValue = super.remove(iKey);

    if (oldValue instanceof ODocument)
      ((ODocument) oldValue).removeOwner(this);

    if (containsKey)
      fireCollectionChangedEvent(new OMultiValueChangeEvent<Object, T>(OMultiValueChangeEvent.OChangeType.REMOVE, iKey, null,
          oldValue));

    return oldValue;
  }

  @Override
  public void clear() {
    final Map<Object, T> origValues;
    if (changeListeners.isEmpty())
      origValues = null;
    else
      origValues = new HashMap<Object, T>(this);

    if (origValues == null) {
      for (T value : values())
        if (value instanceof ODocument) {
          ((ODocument) value).removeOwner(this);
        }
    }

    super.clear();

    if (origValues != null) {
      for (Map.Entry<Object, T> entry : origValues.entrySet()) {
        if (entry.getValue() instanceof ODocument) {
          ((ODocument) entry.getValue()).removeOwner(this);
        }
        fireCollectionChangedEvent(new OMultiValueChangeEvent<Object, T>(OMultiValueChangeEvent.OChangeType.REMOVE, entry.getKey(),
            null, entry.getValue()));
      }
    } else
      setDirty();
  }

  @Override
  public void putAll(Map<?, ? extends T> m) {
    for (Map.Entry<?, ? extends T> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @SuppressWarnings({ "unchecked" })
  public OTrackedMap<T> setDirty() {
    if (status != STATUS.UNMARSHALLING && sourceRecord != null && !sourceRecord.isDirty())
      sourceRecord.setDirty();
    return this;
  }

  public void onBeforeIdentityChanged(final ORID iRID) {
    remove(iRID);
  }

  @SuppressWarnings("unchecked")
  public void onAfterIdentityChanged(final ORecord<?> iRecord) {
    super.put(iRecord.getIdentity(), (T) iRecord);
  }

  public STATUS getInternalStatus() {
    return status;
  }

  public void setInternalStatus(final STATUS iStatus) {
    status = iStatus;
  }

  public void addChangeListener(OMultiValueChangeListener<Object, T> changeListener) {
    changeListeners.add(changeListener);
  }

  public void removeRecordChangeListener(OMultiValueChangeListener<Object, T> changeListener) {
    changeListeners.remove(changeListener);
  }

  public Map<Object, T> returnOriginalState(final List<OMultiValueChangeEvent<Object, T>> multiValueChangeEvents) {
    final Map<Object, T> reverted = new HashMap<Object, T>(this);

    final ListIterator<OMultiValueChangeEvent<Object, T>> listIterator = multiValueChangeEvents.listIterator(multiValueChangeEvents
        .size());

    while (listIterator.hasPrevious()) {
      final OMultiValueChangeEvent<Object, T> event = listIterator.previous();
      switch (event.getChangeType()) {
      case ADD:
        reverted.remove(event.getKey());
        break;
      case REMOVE:
        reverted.put(event.getKey(), event.getOldValue());
        break;
      case UPDATE:
        reverted.put(event.getKey(), event.getOldValue());
        break;
      default:
        throw new IllegalArgumentException("Invalid change type : " + event.getChangeType());
      }
    }

    return reverted;
  }

  protected void fireCollectionChangedEvent(final OMultiValueChangeEvent<Object, T> event) {
    if (status == STATUS.UNMARSHALLING)
      return;

    setDirty();
    for (final OMultiValueChangeListener<Object, T> changeListener : changeListeners) {
      if (changeListener != null)
        changeListener.onAfterRecordChanged(event);
    }
  }

  public Class<?> getGenericClass() {
    return genericClass;
  }

  public void setGenericClass(Class<?> genericClass) {
    this.genericClass = genericClass;
  }

  private Object writeReplace() {
    return new LinkedHashMap<Object, T>(this);
  }
}
