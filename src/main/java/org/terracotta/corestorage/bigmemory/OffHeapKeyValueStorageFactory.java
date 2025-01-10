/*
 * Copyright Terracotta, Inc.
 * Copyright IBM Corp. 2024, 2025
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

import org.terracotta.offheapstore.storage.OffHeapBufferStorageEngine;
import org.terracotta.offheapstore.storage.PointerSize;
import org.terracotta.offheapstore.storage.StorageEngine;
import org.terracotta.offheapstore.util.Factory;
import org.terracotta.offheapstore.util.MemoryUnit;

import java.nio.ByteBuffer;

import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.Transformer;
import org.terracotta.corestorage.TransformerLookup;

public class OffHeapKeyValueStorageFactory extends AbstractOffHeapKeyValueStorageFactory {

  public OffHeapKeyValueStorageFactory(long size, MemoryUnit unit) {
    super(size, -1, -1, unit);
  }
  
  public OffHeapKeyValueStorageFactory(long size, int minChunkSize, int maxChunkSize, MemoryUnit unit) {
    super(size, minChunkSize, maxChunkSize, unit);
  }
  
  public OffHeapKeyValueStorageFactory(long size, MemoryUnit unit, TransformerLookup transformerLookup) {
    super(size, -1, -1, unit, transformerLookup);
  }  
  
  public OffHeapKeyValueStorageFactory(long size, int minChunkSize, int maxChunkSize, MemoryUnit unit, TransformerLookup transformerLookup) {
    super(size, minChunkSize, maxChunkSize, unit, transformerLookup);
  }

  @Override
  public <K, V> OffHeapKeyValueStorage<K, V> create(KeyValueStorageConfig<K, V> storageConfig) {
    BigMemoryKeyValueStorageConfig<K, V> bigMemoryConfig;
    if (storageConfig instanceof BigMemoryKeyValueStorageConfig<?, ?>) {
      bigMemoryConfig = (BigMemoryKeyValueStorageConfig<K, V>) storageConfig;
    } else {
      bigMemoryConfig = ImmutableBigMemoryKeyValueStorageConfig.builder(storageConfig).build();
    }
    return new OffHeapKeyValueStorage<K, V>(getPageSource(), false, createStorageEngineFactory(bigMemoryConfig), bigMemoryConfig.getMutationListeners(), bigMemoryConfig.getInitialTableSize(), bigMemoryConfig.getConcurrency());
  }

  private <K, V> Factory<? extends StorageEngine<K, V>> createStorageEngineFactory(BigMemoryKeyValueStorageConfig<K, V> config) {
    Transformer<? super K, ByteBuffer> keySerializer = createSerializer(config.getKeyTransformer(), config.getKeyClass());
    Transformer<? super V, ByteBuffer> valueSerializer = createSerializer(config.getValueTransformer(), config.getValueClass());
    return OffHeapBufferStorageEngine.createFactory(PointerSize.LONG, getPageSource(), config.getInitialPageSize(), config.getMaximalPageSize(), new CoreStoragePortability(keySerializer), new CoreStoragePortability(valueSerializer), false, false, 0.75f);
  }  
}
