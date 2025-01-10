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

import org.terracotta.offheapstore.buffersource.OffHeapBufferSource;
import org.terracotta.offheapstore.paging.PageSource;
import org.terracotta.offheapstore.paging.UpfrontAllocatingPageSource;
import org.terracotta.offheapstore.util.MemoryUnit;

import java.nio.ByteBuffer;

import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.KeyValueStorageFactory;
import org.terracotta.corestorage.Transformer;
import org.terracotta.corestorage.TransformerLookup;
import org.terracotta.corestorage.monitoring.MonitoredResource;

public abstract class AbstractOffHeapKeyValueStorageFactory implements KeyValueStorageFactory {

  private final UpfrontAllocatingPageSource source;
  private final OffHeapMonitoredResource resource;
  private final TransformerLookup transformerLookup;
  private final DefaultSerializerLookup serializerLookup = new DefaultSerializerLookup();
  
  protected AbstractOffHeapKeyValueStorageFactory(long size, int minChunkSize, int maxChunkSize, MemoryUnit unit) {
    this(size, minChunkSize, maxChunkSize, unit, null);
  }
  
  protected AbstractOffHeapKeyValueStorageFactory(long size, int minChunkSize, int maxChunkSize, MemoryUnit unit, TransformerLookup transformerLookup) {
    if (minChunkSize < 0 ) {
        minChunkSize = unit.MEGABYTES.toBytes(32);
    }
    if ( maxChunkSize < 0 ) {
        maxChunkSize = unit.GIGABYTES.toBytes(1);
    }
    this.source = new UpfrontAllocatingPageSource(new OffHeapBufferSource(), unit.toBytes(size),
                                                  maxChunkSize, minChunkSize);
    this.resource = new OffHeapMonitoredResource(source, unit.toBytes(size));
    this.transformerLookup = transformerLookup;
  }

  public abstract <K, V> OffHeapKeyValueStorage<K, V> create(KeyValueStorageConfig<K, V> storageConfig);
  
  public MonitoredResource getOffHeapResource() {
    return resource;
  }
  
  protected PageSource getPageSource() {
    return source;
  }
  
  protected <T> Transformer<? super T, ByteBuffer> createSerializer(Transformer<? super T, ?> transformer, Class<T> klazz) {
    return createSerializer(null, transformer, klazz);
  }
  
  protected <T> Transformer<? super T, ByteBuffer> createSerializer(String alias, Transformer<? super T, ?> transformer, Class<T> klazz) {
    if (transformer == null && transformerLookup != null) {
      transformer = transformerLookup.lookup(alias, klazz);
    }

    if (transformer == null) {
      return (Transformer<? super T, ByteBuffer>) serializerLookup.lookup(alias, klazz);
    } else if (transformer.getTargetClass() == ByteBuffer.class) {
      return (Transformer<? super T, ByteBuffer>) transformer;
    } else {
      return new CompoundTransformer(transformer, serializerLookup.lookup(alias, transformer.getTargetClass()));
    }
  }
}
