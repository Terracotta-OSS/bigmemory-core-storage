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
