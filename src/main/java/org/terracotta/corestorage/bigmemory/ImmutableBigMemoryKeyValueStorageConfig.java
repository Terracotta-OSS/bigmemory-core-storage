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

import org.terracotta.offheapstore.util.MemoryUnit;

import java.util.List;

import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.KeyValueStorageMutationListener;
import org.terracotta.corestorage.Transformer;

/**
 *
 * @author cdennis
 */
public class ImmutableBigMemoryKeyValueStorageConfig<K, V> implements BigMemoryKeyValueStorageConfig<K, V> {

  private final KeyValueStorageConfig<K, V> baseConfig;
  private final int initialPageSize;
  private final int maximalPageSize;
  private final int initialTableSize;
  private final OffHeapMode offheapMode;
  
  public ImmutableBigMemoryKeyValueStorageConfig(KeyValueStorageConfig<K, V> config, int initialPageSize, int maximalPageSize, int initialTableSize, OffHeapMode offheapMode) {
    this.baseConfig = config;
    this.initialPageSize = initialPageSize;
    this.maximalPageSize = maximalPageSize;
    this.initialTableSize = initialTableSize;
    this.offheapMode = offheapMode;
  }
  
  @Override
  public List<KeyValueStorageMutationListener<? super K, ? super V>> getMutationListeners() {
    return baseConfig.getMutationListeners();
  }

  @Override
  public Class<K> getKeyClass() {
    return baseConfig.getKeyClass();
  }

  @Override
  public Class<V> getValueClass() {
    return baseConfig.getValueClass();
  }

  @Override
  public Transformer<? super K, ?> getKeyTransformer() {
    return baseConfig.getKeyTransformer();
  }

  @Override
  public Transformer<? super V, ?> getValueTransformer() {
    return baseConfig.getValueTransformer();
  }

  @Override
  public int getConcurrency() {
    return baseConfig.getConcurrency();
  }

  @Override
  public int getInitialPageSize() {
    return initialPageSize;
  }

  @Override
  public int getMaximalPageSize() {
    return maximalPageSize;
  }

  @Override
  public int getInitialTableSize() {
    return initialTableSize;
  }
  
  @Override
  public OffHeapMode getOffHeapMode() {
    return offheapMode;
  }
  
  public static <K, V> Builder<K, V> builder(KeyValueStorageConfig<K, V> config) {
    return new Builder(config);
  }
  
  public static class Builder<K, V> {

    private final KeyValueStorageConfig<K, V> base;
    
    private int initialPageSize = MemoryUnit.KILOBYTES.toBytes(4);
    private int maximalPageSize = MemoryUnit.MEGABYTES.toBytes(8);
    private int initialTableSize;
    private OffHeapMode offheapMode = OffHeapMode.FULL;
    
    public Builder(KeyValueStorageConfig<K, V> config) {
      this.base = config;
      this.initialTableSize = config.getConcurrency() * 8;
    }
    
    public Builder<K, V> initialPageSize(int initialPageSize) {
      this.initialPageSize = initialPageSize;
      return this;
    }
    
    public Builder<K, V> maximalPageSize(int maximalPageSize) {
      this.maximalPageSize = maximalPageSize;
      return this;
    }
    
    public Builder<K, V> initialTableSize(int initialTableSize) {
      this.initialTableSize = initialTableSize;
      return this;
    }
    
    public Builder<K, V> offheapMode(OffHeapMode offheapMode) {
      this.offheapMode = offheapMode;
      return this;
    }
    
    public BigMemoryKeyValueStorageConfig<K, V> build() {
      return new ImmutableBigMemoryKeyValueStorageConfig<K, V>(base, initialPageSize, maximalPageSize, initialTableSize, offheapMode);
    }
  }
}
