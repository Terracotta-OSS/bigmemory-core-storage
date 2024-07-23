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
package org.terracotta.corestorage.bigmemory.serializers;

import java.nio.ByteBuffer;

import org.terracotta.corestorage.Serializer;

public class IntegerSerializer extends Serializer<Integer> {

  public static final Serializer<Integer> INSTANCE = new IntegerSerializer();
  
  private IntegerSerializer() {
    //hidden
  }
  
  @Override
  public Integer recover(ByteBuffer buffer) {
    return buffer.getInt();
  }

  @Override
  public ByteBuffer transform(Integer t) {
    ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
    buffer.putInt(t).flip();
    return buffer;
  }

  @Override
  public boolean equals(Integer left, ByteBuffer right) {
    return left.intValue() == right.getInt();
  }
  
}
