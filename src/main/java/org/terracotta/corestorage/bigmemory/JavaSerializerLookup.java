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

import org.terracotta.offheapstore.util.ByteBufferInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.terracotta.corestorage.AnonymousTransformerLookup;
import org.terracotta.corestorage.Serializer;

class JavaSerializerLookup extends AnonymousTransformerLookup {

  @Override
  public <K> Serializer<? super K> lookup(Class<K> klazz) {
    if (Serializable.class.isAssignableFrom(klazz)) {
      return (Serializer<? super K>) JavaSerializer.INSTANCE;
    } else {
      throw new IllegalArgumentException();
    }
  }

  static class JavaSerializer extends Serializer<Serializable> {

    static final Serializer<Serializable> INSTANCE = new JavaSerializer();

    private JavaSerializer() {
      //nothing
    }
    
    @Override
    public Serializable recover(ByteBuffer buffer) throws IOException {
      ByteBufferInputStream bin = new ByteBufferInputStream(buffer);
      try {
        ObjectInputStream oin = new ObjectInputStream(bin);
        try {
          return (Serializable) oin.readObject();
        } catch (ClassNotFoundException e) {
          throw new IOException(e);
        } finally {
          oin.close();
        }
      } finally {
        bin.close();
      }
    }

    @Override
    public ByteBuffer transform(Serializable t) throws IOException {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      try {
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        try {
          oout.writeObject(t);
        } finally {
          oout.close();
        }
      } finally {
        bout.close();
      }
      return ByteBuffer.wrap(bout.toByteArray());
    }

    @Override
    public boolean equals(Serializable left, ByteBuffer right) throws IOException {
      return left.equals(recover(right));
    }
  }
}
