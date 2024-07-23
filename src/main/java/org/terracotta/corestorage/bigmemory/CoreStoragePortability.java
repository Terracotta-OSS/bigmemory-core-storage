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
