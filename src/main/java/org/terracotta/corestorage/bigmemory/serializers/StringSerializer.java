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
