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

import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.TransformerLookup;

import org.terracotta.offheapstore.util.MemoryUnit;
import org.terracotta.offheapstore.paging.PageSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

import org.terracotta.corestorage.monitoring.MonitoredResource;

public class OffHeapStorageManager extends AbstractStorageManager {

  private final Map<String, String> storageProperties = new ConcurrentHashMap<String, String>();
  private final Map<String, KeyValueStorageConfig<?, ?>> configs;
  private final OffHeapKeyValueStorageFactory factory;
  private final Collection<MonitoredResource> resources;
  
  public OffHeapStorageManager(long size, int minChunkSize, int maxChunkSize, MemoryUnit unit) {
    this(size, minChunkSize, maxChunkSize, unit, Collections.<String, KeyValueStorageConfig<?, ?>>emptyMap(), null);
  }
  
  public OffHeapStorageManager(long size, MemoryUnit unit) {
    this(size, -1, -1, unit, Collections.<String, KeyValueStorageConfig<?, ?>>emptyMap(), null);
  }

  public OffHeapStorageManager(long size, int minChunkSize, int maxChunkSize, MemoryUnit unit, Map<String, KeyValueStorageConfig<?, ?>> configs) {
    this(size, minChunkSize, maxChunkSize, unit, configs, null);
  }

  public OffHeapStorageManager(long size, MemoryUnit unit, Map<String, KeyValueStorageConfig<?, ?>> configs) {
    this(size, -1, -1, unit, configs, null);
  }
  
  public OffHeapStorageManager(long size, int minChunkSize, int maxChunkSize, MemoryUnit unit, Map<String, KeyValueStorageConfig<?, ?>> configs, TransformerLookup transformerLookup) {
    this.factory = new OffHeapKeyValueStorageFactory(size, minChunkSize, maxChunkSize, unit, transformerLookup);
    Collection<MonitoredResource> r = new ArrayList<MonitoredResource>();
    r.add(new OffHeapUsedMonitoredResource(factory.getOffHeapResource()));
    r.add(new DataUsedMonitoredResource());
    this.resources = Collections.unmodifiableCollection(r);
    this.configs = new ConcurrentHashMap<String, KeyValueStorageConfig<?, ?>>(configs);
  }

  public OffHeapStorageManager(long size, MemoryUnit unit, Map<String, KeyValueStorageConfig<?, ?>> configs, TransformerLookup transformerLookup) {
    this(size, -1, -1, unit, configs, transformerLookup);
  }

  @Override
  public Map<String, String> getProperties() {
    return storageProperties;
  }

  @Override
  protected Map<String, KeyValueStorageConfig<?, ?>> getStorageConfigs() {
    return configs;
  }

  @Override
  protected <K, V> OffHeapKeyValueStorage<K, V> create(String alias, KeyValueStorageConfig<K, V> config) {
    return factory.create(config);
  }

  @Override
  protected void destroyed(String alias, OffHeapKeyValueStorage<?, ?> map, Class<?> keyClass, Class<?> valueClass) {
    //no-op
  }

  @Override
  public void begin() {
    //no-op
  }

  @Override
  public void commit() {
    //no-op
  }

  @Override
  public Future<?> start() {
    RunnableFuture<?> create = createMaps();
    new Thread(create).start();
    return create;
  }

  @Override
  public void close() {
    destroyMaps().run();
  }

  @Override
  public Collection<MonitoredResource> getMonitoredResources() {
    return resources;
  }

  public PageSource getPageSource() {
    return factory.getPageSource();
  }
}
