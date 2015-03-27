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

import org.terracotta.offheapstore.paging.UpfrontAllocatingPageSource;
import org.terracotta.offheapstore.paging.UpfrontAllocatingPageSource.ThresholdDirection;

import org.terracotta.corestorage.monitoring.MonitoredResource;

public class OffHeapMonitoredResource implements MonitoredResource {

  private final long total;
  private final UpfrontAllocatingPageSource source;

  public OffHeapMonitoredResource(UpfrontAllocatingPageSource source, long total) {
    this.source = source;
    this.total = total;
  }

  @Override
  public MonitoredResource.Type getType() {
    return MonitoredResource.Type.OFFHEAP;
  }

  @Override
  public long getVital() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getUsed() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getReserved() {
    return source.getAllocatedSizeUnSync();
  }

  @Override
  public long getTotal() {
    return total;
  }

  @Override
  public Runnable addUsedThreshold(Direction direction, long value, Runnable action) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Runnable removeUsedThreshold(Direction direction, long value) {
    return null;
  }

  @Override
  public Runnable addReservedThreshold(Direction direction, long value, Runnable action) {
    switch (direction) {
      case RISING:
        return source.addAllocationThreshold(ThresholdDirection.RISING, value, action);
      case FALLING:
        return source.addAllocationThreshold(ThresholdDirection.FALLING, value, action);
    }
    throw new AssertionError();
  }

  @Override
  public Runnable removeReservedThreshold(Direction direction, long value) {
    switch (direction) {
      case RISING:
        return source.removeAllocationThreshold(ThresholdDirection.RISING, value);
      case FALLING:
        return source.removeAllocationThreshold(ThresholdDirection.FALLING, value);
    }
    throw new AssertionError();
  }
}