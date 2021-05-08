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

package io.sundr.builder.internal;

import static io.sundr.builder.Constants.INLINEABLE;
import static io.sundr.model.utils.Types.newTypeParamRef;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.sundr.adapter.apt.AptContext;
import io.sundr.adapter.source.SourceContext;
import io.sundr.adapter.source.utils.Sources;
import io.sundr.builder.annotations.Inline;
import io.sundr.codegen.ReplacePackage;
import io.sundr.codegen.functions.ClassTo;
import io.sundr.model.Kind;
import io.sundr.model.TypeDef;
import io.sundr.model.TypeDefBuilder;
import io.sundr.model.repo.DefinitionRepository;

public class BuilderContext {

  private final Elements elements;
  private final Types types;
  private final AptContext aptContext;
  private final SourceContext sourceContext;

  private final TypeDef visitorInterface;
  private final TypeDef typedVisitorInterface;
  private final TypeDef pathAwareVisitorClass;
  private final TypeDef fluentInterface;
  private final TypeDef builderInterface;
  private final TypeDef nestedInterface;
  private final TypeDef editableInterface;
  private final TypeDef visitableInterface;
  private final TypeDef visitableBuilderInterface;
  private final TypeDef visitableMapClass;
  private final TypeDef inlineableBase;
  private final TypeDef validationUtils;
  private final TypeDef baseFluentClass;
  private final Boolean generateBuilderPackage;
  private final Boolean validationEnabled;
  private final Boolean externalValidatorSupported;
  private final String builderPackage;
  private final Inline[] inlineables;
  private final BuildableRepository buildableRepository;

  public BuilderContext(Elements elements, Types types, Boolean generateBuilderPackage, Boolean validationEnabled,
      String builderPackage, Inline... inlineables) {
    this.elements = elements;
    this.types = types;
    this.validationEnabled = validationEnabled;
    this.aptContext = AptContext.create(elements, types, DefinitionRepository.getRepository());
    this.sourceContext = new SourceContext(aptContext.getDefinitionRepository());
    this.generateBuilderPackage = generateBuilderPackage;
    this.builderPackage = builderPackage;
    this.inlineables = inlineables;

    buildableRepository = new BuildableRepository();

    //We often have issues with these classes, like missing generics etc. So let's register them correctly from the beggining.
    DefinitionRepository.getRepository().register(ClassTo.TYPEDEF.apply(ArrayList.class));
    DefinitionRepository.getRepository().register(ClassTo.TYPEDEF.apply(Iterable.class));

    visitorInterface = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/Visitor.java", sourceContext))
        .accept(new ReplacePackage("io.sundr.builder", builderPackage))
        .build();

    typedVisitorInterface = new TypeDefBuilder(
        Sources.readTypeDefFromResource("io/sundr/builder/TypedVisitor.java", sourceContext))
            .accept(new ReplacePackage("io.sundr.builder", builderPackage))
            .build();

    pathAwareVisitorClass = new TypeDefBuilder(
        Sources.readTypeDefFromResource("io/sundr/builder/PathAwareTypedVisitor.java", sourceContext))
            .accept(new ReplacePackage("io.sundr.builder", builderPackage))
            .build();

    visitableInterface = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/Visitable.java", sourceContext))
        .accept(new ReplacePackage("io.sundr.builder", builderPackage))
        .build();

    builderInterface = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/Builder.java", sourceContext))
        .withPackageName(builderPackage)
        .build();

    visitableBuilderInterface = new TypeDefBuilder(
        Sources.readTypeDefFromResource("io/sundr/builder/VisitableBuilder.java", sourceContext))
            .accept(new ReplacePackage("io.sundr.builder", builderPackage))
            .build();

    fluentInterface = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/Fluent.java", sourceContext))
        .accept(new ReplacePackage("io.sundr.builder", builderPackage))
        .build();

    nestedInterface = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/Nested.java", sourceContext))
        .accept(new ReplacePackage("io.sundr.builder", builderPackage))
        .build();

    editableInterface = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/Editable.java", sourceContext))
        .accept(new ReplacePackage("io.sundr.builder", builderPackage))
        .build();

    inlineableBase = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/Inlineable.java", sourceContext))
        .accept(new ReplacePackage("io.sundr.builder", builderPackage))
        .build();

    visitableMapClass = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/VisitableMap.java", sourceContext))
        .accept(new ReplacePackage("io.sundr.builder", builderPackage))
        .build();

    baseFluentClass = new TypeDefBuilder(Sources.readTypeDefFromResource("io/sundr/builder/BaseFluent.java", sourceContext))
        .accept(new ReplacePackage("io.sundr.builder", builderPackage))
        .build();

    validationUtils = new TypeDefBuilder(Sources.readTypeDefFromStream(
        BuilderContext.class.getResourceAsStream("/io/sundr/builder/internal/resources/ValidationUtils.java"), sourceContext))
            .accept(new ReplacePackage("io.sundr.builder.internal.resources", builderPackage))
            .withAnnotations(new ArrayList<>())
            .build();

    this.externalValidatorSupported = hasValidatorArg(builderPackage + ".ValidationUtils");
  }

  private static boolean hasValidatorArg(String c) {
    Class validator;
    try {
      validator = Class.forName("javax.validation.Validator");
    } catch (ClassNotFoundException e) {
      return false;
    }
    try {
      Method m = Class.forName(c).getMethod("validate", Object.class, validator);
      return true;
    } catch (ClassNotFoundException e) {
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  public Elements getElements() {
    return elements;
  }

  public Types getTypes() {
    return types;
  }

  public Boolean getGenerateBuilderPackage() {
    return generateBuilderPackage;
  }

  public Boolean isValidationEnabled() {
    return validationEnabled;
  }

  public Boolean isExternalvalidatorSupported() {
    return validationEnabled && externalValidatorSupported;
  }

  public String getBuilderPackage() {
    return builderPackage;
  }

  public TypeDef getBaseFluentClass() {
    return baseFluentClass;
  }

  public TypeDef getFluentInterface() {
    return fluentInterface;
  }

  public TypeDef getBuilderInterface() {
    return builderInterface;
  }

  public TypeDef getNestedInterface() {
    return nestedInterface;
  }

  public TypeDef getEditableInterface() {
    return editableInterface;
  }

  public TypeDef getVisitableInterface() {
    return visitableInterface;
  }

  public TypeDef getVisitableBuilderInterface() {
    return visitableBuilderInterface;
  }

  public TypeDef getVisitableMapClass() {
    return visitableMapClass;
  }

  public TypeDef getVisitorInterface() {
    return visitorInterface;
  }

  public TypeDef getTypedVisitorInterface() {
    return typedVisitorInterface;
  }

  public TypeDef getPathAwareVisitorClass() {
    return pathAwareVisitorClass;
  }

  public TypeDef getInlineableBase() {
    return inlineableBase;
  }

  public Boolean getValidationEnabled() {
    return validationEnabled;
  }

  public TypeDef getInlineableInterface(Inline inline) {
    return new TypeDefBuilder(inlineableBase)
        .withKind(Kind.INTERFACE)
        .withPackageName(builderPackage)
        .withName(inline.prefix() + (!inline.name().isEmpty() ? inline.name() : INLINEABLE.getName()) + inline.suffix())
        .withParameters(INLINEABLE.getParameters())
        .addNewMethod()
        .withReturnType(newTypeParamRef("T"))
        .withName(inline.value())
        .and()
        .build();
  }

  public Inline[] getInlineables() {
    return inlineables;
  }

  public TypeDef getValidationUtils() {
    return validationUtils;
  }

  public BuildableRepository getBuildableRepository() {
    return buildableRepository;
  }

  public DefinitionRepository getDefinitionRepository() {
    return aptContext.getDefinitionRepository();
  }

  public AptContext getAptContext() {
    return aptContext;
  }
}
