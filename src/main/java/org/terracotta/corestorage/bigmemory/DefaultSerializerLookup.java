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
