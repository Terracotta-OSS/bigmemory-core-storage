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

import org.terracotta.corestorage.bigmemory.serializers.IntegerSerializer;
import org.terracotta.corestorage.bigmemory.serializers.StringSerializer;
import org.terracotta.offheapstore.util.MemoryUnit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.junit.Test;
import org.terracotta.corestorage.AnonymousTransformerLookup;
import org.terracotta.corestorage.KeyValueStorage;
import org.terracotta.corestorage.KeyValueStorageConfig;
import org.terracotta.corestorage.KeyValueStorageFactory;
import org.terracotta.corestorage.Serializer;
import org.terracotta.corestorage.Transformer;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.assertThat;
import static org.terracotta.corestorage.ImmutableKeyValueStorageConfig.builder;

public class OffHeapKeyValueStorageFactoryTest {

  private static final Transformer<Thread, String> THREAD_STRING_TRANSFORMER = new Transformer<Thread, String>() {
    @Override
    public Thread recover(String transformed) throws IOException {
      return new Thread(transformed);
    }

    @Override
    public String transform(Thread original) throws IOException {
      return original.getName();
    }

    @Override
    public boolean equals(Thread left, String right) throws IOException {
      return left.getName().equals(right);
    }

    @Override
    public Class<String> getTargetClass() {
      return String.class;
    }
  };
  private static final Serializer<Thread> THREAD_STRING_SERIALIZER = new Serializer<Thread>() {

    @Override
    public Thread recover(ByteBuffer transformed) throws IOException {
      return new Thread(transformed.asCharBuffer().toString());
    }

    @Override
    public ByteBuffer transform(Thread original) throws IOException {
      CharBuffer chars = CharBuffer.wrap(original.getName());
      ByteBuffer bytes = ByteBuffer.allocate(chars.remaining() * 2);
      bytes.asCharBuffer().put(chars);
      return bytes;
    }

    @Override
    public boolean equals(Thread left, ByteBuffer right) throws IOException {
      return left.getName().contentEquals(right.asCharBuffer());
    }
  };
          
  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSize() {
    new OffHeapKeyValueStorageFactory(-1, MemoryUnit.BYTES);
  }
  
  @Test
  public void testCreateWithExplicitSerializers() {
    KeyValueStorageFactory factory = new OffHeapKeyValueStorageFactory(1, MemoryUnit.MEGABYTES);
    Serializer<Integer> integerSerializer = IntegerSerializer.INSTANCE;
    Serializer<String> stringSerializer = StringSerializer.INSTANCE;
    KeyValueStorageConfig<Integer, String> config = builder(Integer.class, String.class).keyTransformer(integerSerializer).valueTransformer(stringSerializer).build();
    KeyValueStorage<Integer, String> storage = factory.create(config);
    
    storage.put(0, "foo");
    assertThat(storage.get(0), is("foo"));
  }
  
  @Test
  public void testCreateWithNoSerializers() {
    KeyValueStorageFactory factory = new OffHeapKeyValueStorageFactory(1, MemoryUnit.MEGABYTES);
    KeyValueStorageConfig<Integer, String> config = builder(Integer.class, String.class).build();
    KeyValueStorage<Integer, String> storage = factory.create(config);
    
    storage.put(0, "foo");
    assertThat(storage.get(0), is("foo"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCreateWithNonPortableType() {
    KeyValueStorageFactory factory = new OffHeapKeyValueStorageFactory(1, MemoryUnit.MEGABYTES);
    KeyValueStorageConfig<Integer, Thread> config = builder(Integer.class, Thread.class).build();
    factory.create(config);
  }
  
  @Test
  public void testCreateWithTransformers() {
    KeyValueStorageFactory factory = new OffHeapKeyValueStorageFactory(1, MemoryUnit.MEGABYTES);
    
    KeyValueStorageConfig<Integer, Thread> config = builder(Integer.class, Thread.class).valueTransformer(THREAD_STRING_TRANSFORMER).build();
    KeyValueStorage<Integer, Thread> storage = factory.create(config);
    
    storage.put(0, new Thread("foo"));
    assertThat(storage.get(0).getName(), is("foo"));
  }
  
  @Test
  public void testCreateWithProvidedSerializerLookup() {
    KeyValueStorageFactory factory = new OffHeapKeyValueStorageFactory(1, MemoryUnit.MEGABYTES, new AnonymousTransformerLookup() {

      @Override
      public <T> Serializer<? super T> lookup(Class<T> klazz) {
        if (klazz == Thread.class) {
          return (Serializer<? super T>) THREAD_STRING_SERIALIZER;
        } else {
          return null;
        }
      }
    });
    KeyValueStorageConfig<Integer, Thread> config = builder(Integer.class, Thread.class).build();
    KeyValueStorage<Integer, Thread> storage = factory.create(config);

    storage.put(0, new Thread("foo"));
    assertThat(storage.get(0).getName(), is("foo"));
  }
  
  @Test
  public void testCreateWithProvidedSerializerLookupWithMiss() {
    KeyValueStorageFactory factory = new OffHeapKeyValueStorageFactory(1, MemoryUnit.MEGABYTES, new AnonymousTransformerLookup() {

      @Override
      public <T> Serializer<? super T> lookup(Class<T> klazz) {
        if (klazz == Thread.class) {
          return (Serializer<? super T>) THREAD_STRING_SERIALIZER;
        } else {
          return null;
        }
      }
    });
    KeyValueStorageConfig<Integer, String> config = builder(Integer.class, String.class).build();
    KeyValueStorage<Integer, String> storage = factory.create(config);

    storage.put(0, "foo");
    assertThat(storage.get(0), is("foo"));
  }
  
  @Test
  public void testCreateWithProvidedTransformerLookup() {
    KeyValueStorageFactory factory = new OffHeapKeyValueStorageFactory(1, MemoryUnit.MEGABYTES, new AnonymousTransformerLookup() {

      @Override
      public <T> Transformer<? super T, ?> lookup(Class<T> klazz) {
        if (klazz == Thread.class) {
          return (Transformer<? super T, ?>) THREAD_STRING_TRANSFORMER;
        } else {
          return null;
        }
      }
    });
    KeyValueStorageConfig<Integer, Thread> config = builder(Integer.class, Thread.class).build();
    KeyValueStorage<Integer, Thread> storage = factory.create(config);

    storage.put(0, new Thread("foo"));
    assertThat(storage.get(0).getName(), is("foo"));
  }

  @Test
  public void testCreateWithProvidedTransformerLookupWithMiss() {
    KeyValueStorageFactory factory = new OffHeapKeyValueStorageFactory(1, MemoryUnit.MEGABYTES, new AnonymousTransformerLookup() {

      @Override
      public <T> Transformer<? super T, ?> lookup(Class<T> klazz) {
        if (klazz == Thread.class) {
          return (Transformer<? super T, ?>) THREAD_STRING_TRANSFORMER;
        } else {
          return null;
        }
      }
    });
    KeyValueStorageConfig<Integer, String> config = builder(Integer.class, String.class).build();
    KeyValueStorage<Integer, String> storage = factory.create(config);

    storage.put(0, "foo");
    assertThat(storage.get(0), is("foo"));
  }
}
