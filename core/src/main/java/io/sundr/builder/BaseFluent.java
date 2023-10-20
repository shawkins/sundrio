/*
 * Copyright 2015 The original authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.sundr.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseFluent<F> {

  public final VisitableMap _visitables = new VisitableMap();

  public static <T> VisitableBuilder<T, ?> builderOf(T item) {
    if (item instanceof Editable) {
      Object editor = ((Editable) item).edit();
      if (editor instanceof VisitableBuilder) {
        return (VisitableBuilder<T, ?>) editor;
      }
    }

    try {
      return (VisitableBuilder<T, ?>) Class
          .forName(item.getClass().getName() + "Builder", true, item.getClass().getClassLoader())
          .getConstructor(item.getClass())
          .newInstance(item);
    } catch (Exception e) {
      try {
        return (VisitableBuilder<T, ?>) Class.forName(item.getClass().getName() + "Builder").getConstructor(item.getClass())
            .newInstance(item);
      } catch (Exception e1) {
        throw new IllegalStateException("Failed to create builder for: " + item.getClass(), e1);
      }
    }
  }

  public static <T> List<T> build(List<? extends Builder<? extends T>> list) {
    return list == null ? null : list.stream().map(Builder::build).collect(Collectors.toList());
  }

  public static <T> Set<T> build(Set<? extends Builder<? extends T>> set) {
    return set == null ? null : new LinkedHashSet<T>(set.stream().map(Builder::build).collect(Collectors.toSet()));
  }

  public static <T> List<T> aggregate(List<? extends T>... lists) {
    return new ArrayList(Arrays.stream(lists).filter(Objects::nonNull).collect(Collectors.toList()));
  }

  public static <T> Set<T> aggregate(Set<? extends T>... sets) {
    return new LinkedHashSet(Arrays.stream(sets).filter(Objects::nonNull).collect(Collectors.toSet()));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    return true;
  }

  public Optional<VisitableMap> getVisitableMap() {
    return Optional.of(_visitables);
  }
}
