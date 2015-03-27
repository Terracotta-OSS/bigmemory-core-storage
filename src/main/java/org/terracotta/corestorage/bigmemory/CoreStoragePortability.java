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

import org.terracotta.offheapstore.storage.portability.Portability;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.terracotta.corestorage.Transformer;

public class CoreStoragePortability<T> implements Portability<T> {

  private final Transformer<T, ByteBuffer> serializer;

  public CoreStoragePortability(Transformer<T, ByteBuffer> serializer) {
    this.serializer = serializer;
  }

  @Override
  public T decode(ByteBuffer buffer) {
    try {
      return serializer.recover(buffer);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public ByteBuffer encode(T object) {
    try {
      return serializer.transform(object);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean equals(Object object, ByteBuffer buffer) {
    try {
      return serializer.equals((T) object, buffer);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
