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

import java.io.IOException;

import org.terracotta.corestorage.Transformer;

public class CompoundTransformer<T, I, U> implements Transformer<T, U> {

  private final Transformer<T, I> one;
  private final Transformer<I, U> two;
  
  public CompoundTransformer(Transformer<T, I> one, Transformer<I, U> two) {
    this.one = one;
    this.two = two;
  }

  @Override
  public T recover(U buffer) throws IOException {
    return one.recover(two.recover(buffer));
  }

  @Override
  public U transform(T original) throws IOException {
    return two.transform(one.transform(original));
  }

  @Override
  public boolean equals(T left, U right) throws IOException {
    return two.equals(one.transform(left), right);
  }

  @Override
  public Class<U> getTargetClass() {
    return two.getTargetClass();
  }
}
