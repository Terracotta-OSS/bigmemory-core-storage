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

import java.nio.ByteBuffer;

import org.terracotta.corestorage.Serializer;

public class StringSerializer extends Serializer<String> {

  public static final Serializer<String> INSTANCE = new StringSerializer();
  
  private StringSerializer() {
    //hidden
  }
  
  @Override
  public String recover(ByteBuffer buffer) {
    return buffer.asCharBuffer().toString();
  }

  @Override
  public ByteBuffer transform(String t) {
    ByteBuffer buffer = ByteBuffer.allocate(t.length() * 2);
    buffer.asCharBuffer().put(t);
    return buffer;
  }

  @Override
  public boolean equals(String left, ByteBuffer right) {
    return left.contentEquals(right.asCharBuffer());
  }
}
