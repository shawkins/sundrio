/**
 * Copyright 2015 The original authors.
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
 *
**/

package io.sundr.adapter.reflect;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import org.junit.Test;

import io.sundr.adapter.api.Adapter;
import io.sundr.adapter.api.AdapterContext;
import io.sundr.adapter.api.Adapters;
import io.sundr.model.repo.DefinitionRepository;

public class ReflectionAdapterFactoryTest {

  private final AdapterContext context = AdapterContext.create(DefinitionRepository.getRepository());

  private Optional<Adapter<Class, Type, Field, Method>> createAdapter() {
    return Adapters.getAdapterForType(Class.class, context);
  }

  @Test
  public void shouldCreateAdapter() throws Exception {
    Optional<Adapter<Class, Type, Field, Method>> adapter = createAdapter();
    assertTrue(adapter.isPresent());
  }

  @Test
  public void shouldAdaptList() throws Exception {
    Optional<Adapter<Class, Type, Field, Method>> adapter = createAdapter();
    assertTrue(adapter.isPresent());
    adapter.ifPresent(a -> {
    });
  }
}
