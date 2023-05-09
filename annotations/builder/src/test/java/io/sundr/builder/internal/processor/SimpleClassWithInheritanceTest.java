/*
 * Copyright 2016 The original authors.
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

package io.sundr.builder.internal.processor;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import io.sundr.adapter.api.AdapterContext;
import io.sundr.adapter.source.utils.Sources;
import io.sundr.builder.internal.functions.ClazzAs;
import io.sundr.model.ClassRef;
import io.sundr.model.Kind;
import io.sundr.model.RichTypeDef;
import io.sundr.model.TypeDef;
import io.sundr.model.TypeRef;
import io.sundr.model.repo.DefinitionRepository;
import io.sundr.model.utils.TypeArguments;

public class SimpleClassWithInheritanceTest extends AbstractProcessorTest {

  private final AdapterContext context = AdapterContext.create(DefinitionRepository.getRepository());

  TypeDef simpleClassDef = Sources.readTypeDefFromResource("SimpleClass.java", context);
  RichTypeDef simpleClassWithDateDef = TypeArguments
      .apply(Sources.readTypeDefFromResource("SimpleClassWithDate.java", context));

  @Before
  public void setUp() {
    builderContext.getBuildableRepository().register(simpleClassDef);
  }

  @Test
  public void testFluent() throws FileNotFoundException {
    TypeDef fluent = ClazzAs.FLUENT.apply(simpleClassWithDateDef);
    System.out.println(fluent);

    assertEquals(Kind.CLASS, fluent.getKind());
    assertEquals("SimpleClassWithDateFluent", fluent.getName());
    assertEquals(1, fluent.getExtendsList().size());

    ClassRef superClass = fluent.getExtendsList().iterator().next();
    assertEquals("SimpleClassFluent", superClass.getName());
    assertEquals(1, superClass.getArguments().size());
    assertEquals("A", superClass.getArguments().iterator().next().toString());
  }

  @Test
  public void testBuilder() throws FileNotFoundException {
    TypeDef builder = ClazzAs.BUILDER.apply(simpleClassWithDateDef);
    System.out.println(builder);

    assertEquals(Kind.CLASS, builder.getKind());
    assertEquals("SimpleClassWithDateBuilder", builder.getName());
    assertEquals(1, builder.getExtendsList().size());

    ClassRef superClass = builder.getImplementsList().iterator().next();
    assertEquals(builderContext.getVisitableBuilderInterface().getName(), superClass.getName());
    assertEquals(2, superClass.getArguments().size());
    Iterator<TypeRef> argIterator = superClass.getArguments().iterator();
    TypeRef ref = argIterator.next();
    assertEquals("testpackage.SimpleClassWithDate", ref.render());
    assertEquals("testpackage.SimpleClassWithDate", ref.toString());

    ref = argIterator.next();
    assertEquals("testpackage.SimpleClassWithDateBuilder", ref.render());
    assertEquals("testpackage.SimpleClassWithDateBuilder", ref.toString());
  }

  @Test
  public void testEditable() {
    TypeDef editable = ClazzAs.EDITABLE.apply(simpleClassWithDateDef);
    System.out.println(editable);

    assertEquals(Kind.CLASS, editable.getKind());
    assertEquals("EditableSimpleClassWithDate", editable.getName());
    assertEquals(1, editable.getExtendsList().size());

    ClassRef superClass = editable.getExtendsList().iterator().next();
    assertEquals(simpleClassWithDateDef.toInternalReference(), superClass);
  }

  @Test
  public void testInline() {
    TypeDef inlineable = BuildableProcessor.inlineableOf(builderContext, simpleClassWithDateDef, inline);
    System.out.println(inlineable);
    assertEquals(Kind.CLASS, inlineable.getKind());
    assertEquals("CallableSimpleClassWithDate", inlineable.getName());
  }
}
