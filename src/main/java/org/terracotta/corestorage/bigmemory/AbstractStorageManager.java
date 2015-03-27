/* 
 * The contents of this file are subject to the Terracotta Public License Version
 * 2.0 (the "License"); You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at 
 *
 *      http://terracotta.org/legal/terracotta-public-license.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Covered Software is BigMemory Core Storage Implementation.
 *
 * The Initial Developer of the Covered Software is 
 *      Terracotta, Inc., a Software AG company
 */
package org.terracotta.corestorage.bigmemory;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.terracotta.corestorage.KeyValueStorage;
import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.StorageManager;
import org.terracotta.corestorage.monitoring.MonitoredResource;

public abstract class AbstractStorageManager implements StorageManager {
  
  protected final ConcurrentMap<String, MapHolder> maps = new ConcurrentHashMap<String, MapHolder>();
  
  protected RunnableFuture<?> createMaps() {
    return new FutureTask<Void>(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        for (Map.Entry<String, KeyValueStorageConfig<?, ?>> e : getStorageConfigs().entrySet()) {
          String alias = e.getKey();
          KeyValueStorageConfig<?, ?> config = e.getValue();
          if (maps.putIfAbsent(alias, new MapHolder(create(alias, config), config.getKeyClass(), config.getValueClass())) != null) {
            throw new IllegalStateException("Duplicated map for alias: " + alias);
          }
        }
        return null;
      }
    });
  }

  protected RunnableFuture<?> destroyMaps() {
    return new FutureTask<Void>(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        for (String alias : maps.keySet()) {
          destroyKeyValueStorage(alias);
        }
        return null;
      }
    });
  }

  @Override
  public <K, V> KeyValueStorage<K, V> createKeyValueStorage(String alias, KeyValueStorageConfig<K, V> config) throws IllegalStateException {
    OffHeapKeyValueStorage<K, V> storage = create(alias, config);
    if (maps.putIfAbsent(alias, new MapHolder(storage, config.getKeyClass(), config.getValueClass())) != null) {
      throw new IllegalStateException("Duplicated map for alias: " + alias);
    } else {
      getStorageConfigs().put(alias, config);
      return storage;
    }
  }

  @Override
  public void destroyKeyValueStorage(String alias) {
    MapHolder<?, ?> removed = maps.remove(alias);
    if (removed != null) {
      destroyed(alias, removed.map, removed.keyClass, removed.valueClass);
      removed.map.destroy();
      getStorageConfigs().remove(alias);
    }
  }

  @Override
  public <K, V> KeyValueStorage<K, V> getKeyValueStorage(String alias, Class<K> keyClass, Class<V> valueClass) {
    final MapHolder mapHolder = maps.get(alias);
    return mapHolder == null ? null : mapHolder.getMap(keyClass, valueClass);
  }

  protected abstract <K, V> OffHeapKeyValueStorage<K, V> create(String alias, KeyValueStorageConfig<K, V> config);
  
  protected abstract void destroyed(String alias, OffHeapKeyValueStorage<?, ?> map, Class<?> keyClass, Class<?> valueClass);

  protected abstract Map<String, KeyValueStorageConfig<?, ?>> getStorageConfigs();
  
  protected static class MapHolder<K, V> {

    final OffHeapKeyValueStorage<K, V> map;
    final Class<K> keyClass;
    final Class<V> valueClass;

    MapHolder(final OffHeapKeyValueStorage<K, V> map, final Class<K> keyClass, final Class<V> valueClass) {
      this.map = map;
      this.keyClass = keyClass;
      this.valueClass = valueClass;
    }

    public OffHeapKeyValueStorage<K, V> getMap(final Class<?> keyClass, final Class<?> valueClass) {
      if ((keyClass != this.keyClass) || (valueClass != this.valueClass)) {
        throw new IllegalArgumentException("Classes don't match!");
      }
      return map;
    }
  }

  public class OffHeapUsedMonitoredResource implements MonitoredResource {

    private final MonitoredResource delegate;
    
    public OffHeapUsedMonitoredResource(MonitoredResource delegate) {
      this.delegate = delegate;
    }

    @Override
    public MonitoredResource.Type getType() {
      return delegate.getType();
    }

    @Override
    public long getVital() {
      long vital = 0;
      for (MapHolder<?, ?> h : maps.values()) {
        vital += ((OffHeapKeyValueStorage<?, ?>) h.map).getVitalMemory();
      }
      return vital;
    }

    @Override
    public long getUsed() {
      long used = 0;
      for (MapHolder<?, ?> h : maps.values()) {
        used += ((OffHeapKeyValueStorage<?, ?>) h.map).getOccupiedMemory();
      }
      return used;
    }
    
    @Override
    public long getReserved() {
      return delegate.getReserved();
    }

    @Override
    public long getTotal() {
      return delegate.getTotal();
    }

    @Override
    public Runnable addUsedThreshold(Direction direction, long value, Runnable action) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Runnable removeUsedThreshold(Direction direction, long value) {
      return null;
    }

    @Override
    public Runnable addReservedThreshold(Direction direction, long value, Runnable action) {
      return delegate.addReservedThreshold(direction, value, action);
    }

    @Override
    public Runnable removeReservedThreshold(Direction direction, long value) {
      return delegate.removeReservedThreshold(direction, value);
    }
  }
  
  public class DataUsedMonitoredResource implements MonitoredResource {

    @Override
    public Type getType() {
      return Type.DATA;
    }

    @Override
    public long getVital() {
      return getUsed();
    }

    @Override
    public long getUsed() {
      long used = 0;
      for (MapHolder<?, ?> h : maps.values()) {
        used += ((OffHeapKeyValueStorage<?, ?>) h.map).getDataSetSize();
      }
      return used;
    }

    @Override
    public long getReserved() {
      return getUsed();
    }

    @Override
    public long getTotal() {
      return Long.MAX_VALUE;
    }

    @Override
    public Runnable addUsedThreshold(Direction drctn, long l, Runnable r) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Runnable removeUsedThreshold(Direction drctn, long l) {
      return null;
    }

    @Override
    public Runnable addReservedThreshold(Direction drctn, long l, Runnable r) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Runnable removeReservedThreshold(Direction drctn, long l) {
      return null;
    }
  }
}
