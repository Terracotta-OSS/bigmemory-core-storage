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

import org.terracotta.offheapstore.util.MemoryUnit;
import java.util.Map;

import org.terracotta.corestorage.ImmutableKeyValueStorageConfig;
import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.StorageManager;

public class OffHeapStorageManagerTest extends AbstractStorageManagerTest {

  @Override
  protected StorageManager create(int size, MemoryUnit unit, Map<String, KeyValueStorageConfig<?, ?>> configs) {
    return new OffHeapStorageManager(size, unit, configs);
  }

  @Override
  protected StorageManager create(int size, MemoryUnit unit) {
    return new OffHeapStorageManager(size, unit);
  }

  @Override
  protected <K, V> KeyValueStorageConfig<K, V> createConfig(Class<K> keyClass, Class<V> valueClass) {
    return ImmutableKeyValueStorageConfig.builder(keyClass, valueClass).build();
  }
}
