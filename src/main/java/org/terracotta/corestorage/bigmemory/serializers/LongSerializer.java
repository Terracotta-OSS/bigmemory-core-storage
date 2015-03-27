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
