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
