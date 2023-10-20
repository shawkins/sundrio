/*
 *
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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

public interface Visitable<T> {

  default T accept(Visitor<?>... visitors) {
    return accept(Collections.emptyList(), visitors);
  }

  default <V> T accept(Class<V> type, Visitor<V> visitor) {
    return accept(Collections.emptyList(), new Visitor<V>() {
      @Override
      public Class<V> getType() {
        return type;
      }

      @Override
      public void visit(List<Entry<String, Object>> path, V element) {
        visitor.visit(path, element);
      }

      @Override
      public void visit(V element) {
        visitor.visit(element);
      }
    });
  }

  default T accept(List<Entry<String, Object>> path, Visitor<?>... visitors) {
    return accept(path, "", visitors);
  }

  default T accept(List<Entry<String, Object>> path, String currentKey, Visitor<?>... visitors) {
    List<Visitor> sortedVisitor = new ArrayList<>();
    for (Visitor visitor : visitors) {
      visitor = VisitorListener.wrap(visitor);
      if (!visitor.canVisit(path, this)) {
        continue;
      }
      sortedVisitor.add(visitor);
    }
    sortedVisitor.sort((l, r) -> ((Visitor) r).order() - ((Visitor) l).order());
    for (Visitor visitor : sortedVisitor) {
      visitor.visit(path, this);
    }

    List<Entry<String, Object>> copyOfPath = path != null ? new ArrayList(path) : new ArrayList<>();
    copyOfPath.add(new AbstractMap.SimpleEntry<>(currentKey, this));

    getVisitableMap().ifPresent(vm -> {
      for (Entry<String, ?> entry : vm.entrySet()) {
        List<Entry<String, Object>> newPath = Collections.unmodifiableList(copyOfPath);

        // Copy visitables to avoid ConcurrentModificationException when Visitors add/remove Visitables
        for (Visitable<T> visitable : new ArrayList<>((List<Visitable<T>>) entry.getValue())) {
          for (Visitor visitor : visitors) {
            if (visitor.getType() != null && visitor.getType().isAssignableFrom(visitable.getClass())) {
              visitable.accept(newPath, entry.getKey(), visitor);
            }
          }

          for (Visitor visitor : visitors) {
            if (visitor.getType() == null || !visitor.getType().isAssignableFrom(visitable.getClass())) {
              visitable.accept(newPath, entry.getKey(), visitor);
            }
          }
        }
      }
    });

    return (T) this;
  }

  default T getTarget(Visitable<T> visitable) {
    return (T) visitable;
  }

  default Optional<VisitableMap> getVisitableMap() {
    return Optional.empty();
  }
}
