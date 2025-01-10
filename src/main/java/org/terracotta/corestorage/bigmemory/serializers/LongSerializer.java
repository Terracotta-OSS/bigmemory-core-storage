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
package org.terracotta.corestorage.bigmemory.serializers;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.terracotta.corestorage.Serializer;

/**
 *
 * @author cdennis
 */
public class LongSerializer extends Serializer<Long> {

  public static final Serializer<Long> INSTANCE = new LongSerializer();
  
  private LongSerializer() {
    //hidden
  }
  
  @Override
  public Long recover(ByteBuffer buffer) throws IOException {
    return buffer.getLong();
  }

  @Override
  public ByteBuffer transform(Long original) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
    buffer.putLong(original).flip();
    return buffer;
  }

  @Override
  public boolean equals(Long left, ByteBuffer right) throws IOException {
    return left.longValue() == right.getLong();
  }
  
}
