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

import org.terracotta.corestorage.bigmemory.serializers.ByteArraySerializer;
import org.terracotta.corestorage.bigmemory.serializers.IntegerSerializer;
import org.terracotta.corestorage.bigmemory.serializers.LongSerializer;
import org.terracotta.corestorage.bigmemory.serializers.StringSerializer;

import org.terracotta.corestorage.Serializer;

public class DefaultSerializerLookup extends JavaSerializerLookup {

  @Override
  public <K> Serializer<? super K> lookup(Class<K> klazz) {
    if (klazz == Integer.class) {
      return (Serializer<K>) IntegerSerializer.INSTANCE;
    } else if (klazz == Long.class) {
      return (Serializer<K>) LongSerializer.INSTANCE;
    } else if (klazz == String.class) {
      return (Serializer<K>) StringSerializer.INSTANCE;
    } else if (klazz == byte[].class) {
      return (Serializer<K>) ByteArraySerializer.INSTANCE;
    } else {
      return super.lookup(klazz);
    }
  }  
}
