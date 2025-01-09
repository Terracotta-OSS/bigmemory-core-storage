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

import org.terracotta.offheapstore.util.MemoryUnit;
import java.util.Collections;
import org.junit.Test;
import org.terracotta.corestorage.KeyValueStorage;
import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.StorageManager;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.assertThat;
import static org.terracotta.corestorage.ImmutableKeyValueStorageConfig.builder;

public class OffHeapKeyValueStorageTest {
  
  @Test
  public void testGetWhenMissing() throws Exception {
    StorageManager manager = createManager("test", Integer.class, String.class);
    manager.start().get();
    try {
      KeyValueStorage<Integer, String> storage = manager.getKeyValueStorage("test", Integer.class, String.class);
      assertThat(storage.get(0), nullValue());
    } finally {
      manager.close();
    }
  }
  
  @Test(expected = NullPointerException.class)
  public void testGetWithNullKey() throws Exception {
    StorageManager manager = createManager("test", Integer.class, String.class);
    manager.start().get();
    try {
      KeyValueStorage<Integer, String> storage = manager.getKeyValueStorage("test", Integer.class, String.class);
      storage.get(null);
    } finally {
      manager.close();
    }
  }
  
  @Test
  public void testGetWhenPresent() throws Exception {
    StorageManager manager = createManager("test", Integer.class, String.class);
    manager.start().get();
    try {
      KeyValueStorage<Integer, String> storage = manager.getKeyValueStorage("test", Integer.class, String.class);
      storage.put(0, "foo");
      assertThat(storage.get(0), is("foo"));
    } finally {
      manager.close();
    }
  }
  
  private static <K, V> StorageManager createManager(String name, Class<K> keyClass, Class<V> valueClass) {
    return new OffHeapStorageManager(1, MemoryUnit.MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap(name, builder(keyClass, valueClass).build()));
  }
}
