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
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.terracotta.corestorage.KeyValueStorage;
import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.StorageManager;
import org.terracotta.corestorage.monitoring.MonitoredResource;
import org.terracotta.corestorage.monitoring.MonitoredResource.Type;

import static org.terracotta.offheapstore.util.MemoryUnit.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.IsSame.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.assertThat;

public abstract class AbstractStorageManagerTest {
  
  protected abstract StorageManager create(int size, MemoryUnit unit, Map<String, KeyValueStorageConfig<?, ?>> configs) throws IOException;
  
  protected abstract StorageManager create(int size, MemoryUnit unit) throws IOException;
  
  protected abstract <K, V> KeyValueStorageConfig<K, V> createConfig(Class<K> keyClass, Class<V> valueClass);
  
  @Test(expected=IllegalArgumentException.class)
  public final void testConstructWithNegativeSize() throws IOException {
    create(-1, BYTES);
  }

  @Test
  public final void testConstructCorrectly() throws IOException {
    create(1, BYTES);
  }
  
  @Test
  public final void testGetKeyValueStorageMissing() throws Exception {
    StorageManager manager = create(1, MEGABYTES);
    manager.start().get();
    try {
      assertThat(manager.getKeyValueStorage("missing", Object.class, Object.class), nullValue());
    } finally {
      manager.close();
    }
  }
  
  @Test
  public final void testGetKeyValueStorageCorrectly() throws Exception {
    StorageManager manager = create(1, MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap("present", createConfig(Integer.class, String.class)));
    manager.start().get();
    try {
      assertThat(manager.getKeyValueStorage("present", Integer.class, String.class), notNullValue());
    } finally {
      manager.close();
    }
  }

  @Test(expected=IllegalArgumentException.class)
  public final void testGetKeyValueStorageIncorrectKeyType() throws Exception {
    StorageManager manager = create(1, MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap("present", createConfig(Integer.class, String.class)));
    manager.start().get();
    try {
      manager.getKeyValueStorage("present", Long.class, String.class);
    } finally {
      manager.close();
    }
  }
  
  @Test(expected=IllegalArgumentException.class)
  public final void testGetKeyValueStorageIncorrectValueType() throws Exception {
    StorageManager manager = create(1, MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap("present", createConfig(Integer.class, String.class)));
    manager.start().get();
    try {
      manager.getKeyValueStorage("present", Integer.class, Long.class);
    } finally {
      manager.close();
    }
  }
  
  @Test
  public final void testCreateKeyValueStorage() throws Exception {
    KeyValueStorageConfig<Integer, String> config = createConfig(Integer.class, String.class);
    StorageManager manager = create(1, MEGABYTES);
    manager.start().get();
    try {
      KeyValueStorage<Integer, String> storage = manager.createKeyValueStorage("present", config);
      assertThat(storage, notNullValue());
      assertThat(manager.getKeyValueStorage("present", Integer.class, String.class), sameInstance(storage));
    } finally {
      manager.close();
    }
  }

  @Ignore
  @Test(expected=IllegalStateException.class)
  public final void testCreateKeyValueStorageWhenPresent() throws Exception {
    KeyValueStorageConfig<?, ?> config = createConfig(Integer.class, String.class);
    StorageManager manager = create(1, MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap("present", config));
    manager.start().get();
    try {
      manager.createKeyValueStorage("present", config);
    } finally {
      manager.close();
    }
  }

  @Test(expected=NullPointerException.class)
  public final void testCreateKeyValueStorageNullName() throws Exception {
    StorageManager manager = create(1, MEGABYTES);
    manager.start().get();
    try {
      manager.createKeyValueStorage(null, createConfig(Integer.class, String.class));
    } finally {
      manager.close();
    }
  }
  
  @Test
  public final void testDestroyKeyValueStorage() throws Exception {
    StorageManager manager = create(1, MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap("present", createConfig(Integer.class, String.class)));
    manager.start().get();
    try {
      Collection<MonitoredResource> resources = manager.getMonitoredResources();
      MonitoredResource offheap = null;
      MonitoredResource data = null;
     
      for (MonitoredResource res : resources) {
        switch (res.getType()) {
        case OFFHEAP: offheap = res;
          break;
        case DATA: data = res;
          break;
        }
      }
      
      assertThat(offheap.getReserved(), greaterThan(0L));
      assertThat(data.getReserved(), is(0L));
      assertThat(data.getUsed(), is(0L));
      assertThat(data.getVital(), is(0L));
      
      manager.destroyKeyValueStorage("present");
      assertThat(manager.getKeyValueStorage("present", Integer.class, String.class), nullValue());
      assertThat(offheap.getReserved(), is(0L));
      assertThat(data.getReserved(), is(0L));
      assertThat(data.getUsed(), is(0L));
      assertThat(data.getVital(), is(0L));
    } finally {
      manager.close();
    }
  }

  @Ignore("Lifecycling behavior not spec'ed yet")
  @Test(expected=IllegalStateException.class)
  public final void testUnstartedUsability() throws IOException {
    StorageManager manager = create(1, MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap("present", createConfig(Integer.class, String.class)));
    manager.getKeyValueStorage("present", Integer.class, String.class);
  }

  @Ignore("Lifecycling behavior not spec'ed yet")
  @Test(expected=IllegalStateException.class)
  public final void testShutdownUsability() throws Exception {
    StorageManager manager = create(1, MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap("present", createConfig(Integer.class, String.class)));
    manager.start().get();
    manager.close();
    manager.getKeyValueStorage("present", Integer.class, String.class);
  }

  @Test
  public void testMonitoredResourceWhenEmpty() throws Exception {
    StorageManager manager = create(1, MEGABYTES);
    manager.start().get();
    try {
      Collection<MonitoredResource> resources = manager.getMonitoredResources();
      assertThat(resources.size(), is(2));
      Iterator<MonitoredResource> it = resources.iterator();
      MonitoredResource resourceA = it.next();
      MonitoredResource resourceB = it.next();
      
      MonitoredResource offheap = Type.OFFHEAP.equals(resourceA.getType()) ? resourceA : resourceB;
      MonitoredResource data = Type.DATA.equals(resourceA.getType()) ? resourceA : resourceB;

      assertThat(offheap.getVital(), is(0L));
      assertThat(offheap.getUsed(), is(0L));
      assertThat(offheap.getReserved(), is(0L));
      assertThat(offheap.getTotal(), is(MEGABYTES.toBytes(1L)));
      assertThat(data.getVital(), is(0L));
      assertThat(data.getUsed(), is(0L));
      assertThat(data.getReserved(), is(0L));
    } finally {
      manager.close();
    }
  }

  @Test
  public void testMonitoredResource() throws Exception {
    StorageManager manager = create(1, MEGABYTES, Collections.<String, KeyValueStorageConfig<?, ?>>singletonMap("present", createConfig(Integer.class, String.class)));
    manager.start().get();
    try {
      Collection<MonitoredResource> resources = manager.getMonitoredResources();
      assertThat(resources.size(), is(2));
      Iterator<MonitoredResource> it = resources.iterator();
      MonitoredResource resourceA = it.next();
      MonitoredResource resourceB = it.next();
      
      MonitoredResource offheap = Type.OFFHEAP.equals(resourceA.getType()) ? resourceA : resourceB;
      MonitoredResource data = Type.DATA.equals(resourceA.getType()) ? resourceA : resourceB;

      assertThat(offheap.getVital(), greaterThanOrEqualTo(0L));
      assertThat(offheap.getUsed(), greaterThanOrEqualTo(0L));
      assertThat(offheap.getReserved(), greaterThanOrEqualTo(offheap.getUsed()));
      assertThat(offheap.getTotal(), greaterThanOrEqualTo(offheap.getReserved()));
      assertThat(offheap.getTotal(), is(MEGABYTES.toBytes(1L)));
      assertThat(data.getVital(), is(0L));
      assertThat(data.getUsed(), is(0L));
      assertThat(data.getReserved(), is(0L));
    } finally {
      manager.close();
    }
  }
}
