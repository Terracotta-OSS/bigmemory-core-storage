/*
 * Copyright Terracotta, Inc.
 * Copyright Super iPaaS Integration LLC, an IBM Company 2024
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
package org.terracotta.corestorage.bigmemory;

import org.terracotta.offheapstore.OffHeapHashMap;
import org.terracotta.offheapstore.concurrent.ConcurrentOffHeapHashMap;
import org.terracotta.offheapstore.paging.PageSource;
import org.terracotta.offheapstore.storage.StorageEngine;
import org.terracotta.offheapstore.util.Factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.terracotta.corestorage.KeyValueStorage;
import org.terracotta.corestorage.KeyValueStorageMutationListener;
import org.terracotta.corestorage.Retriever;

public class OffHeapKeyValueStorage<K, V> implements KeyValueStorage<K, V> {
  private static final int RESERVED_BITS = Integer.SIZE - Integer.numberOfLeadingZeros(OffHeapHashMap.RESERVED_STATUS_BITS);

  private final ConcurrentOffHeapHashMap<K, V> backing;
  private final List<KeyValueStorageMutationListener<K, V>> listeners;

  public OffHeapKeyValueStorage(PageSource source, boolean tableAllocationsSteal, Factory<? extends StorageEngine<K, V>> storageEngineFactory, List<KeyValueStorageMutationListener<? super K, ? super V>> listeners, int initialTableSize, int concurrency) {
    this.listeners = Collections.unmodifiableList(new ArrayList(listeners));
    this.backing = new ConcurrentOffHeapHashMap(source, tableAllocationsSteal, storageEngineFactory, initialTableSize, concurrency);
  }
  
  @Override
  public Set<K> keySet() {
    return backing.keySet();
  }

  @Override
  public Collection<V> values() {
    return backing.values();
  }

  @Override
  public long size() {
    return backing.size();
  }

  @Override
  public void put(K key, V value) {
    backing.put(key, value);
    for (KeyValueStorageMutationListener<K, V> l : listeners) {
      l.added(new EasyRetriever<K>(key), new EasyRetriever<V>(value), (byte) 0);
    }
  }

  public void put(K key, V value, byte metadata) {
    int encodedMetadata = (0xff & metadata) << RESERVED_BITS;
    backing.put(key, value, encodedMetadata);
    for (KeyValueStorageMutationListener<K, V> l : listeners) {
      l.added(new EasyRetriever<K>(key), new EasyRetriever<V>(value), metadata);
    }
  }
  
  @Override
  public V get(K key) {
    return backing.get(key);
  }

  @Override
  public boolean remove(K key) {
    if (backing.removeNoReturn(key)) {
      for (KeyValueStorageMutationListener<K, V> l : listeners) {
        l.removed(new EasyRetriever<K>(key));
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void removeAll(Collection<K> keys) {
    for (K key : keys) {
      remove(key);
    }
  }

  @Override
  public boolean containsKey(K key) {
    return backing.containsKey(key);
  }

  long getVitalMemory() {
    return backing.getVitalMemory();
  }
  
  long getOccupiedMemory() {
    return backing.getDataOccupiedMemory() + (backing.getTableCapacity() * 4 * (Integer.SIZE / Byte.SIZE));
  }

  long getDataSetSize() {
    return backing.getDataSize();
  }

  public void destroy() {
    backing.destroy();
  }

  public ConcurrentOffHeapHashMap<K, V> getDelegate() {
    return backing;
  }
  
  static class EasyRetriever<T> implements Retriever<T> {

    private final T result;

    public EasyRetriever(T result) {
      this.result = result;
    }

    @Override
    public T retrieve() {
      return result;
    }

  }

  @Override
  public void clear() {
    if (listeners.isEmpty()) {
      backing.clear();
    } else {
      for (K key : keySet()) {
        remove(key);
      }
    }
  }
}
