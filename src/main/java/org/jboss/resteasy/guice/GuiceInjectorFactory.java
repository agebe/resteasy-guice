/*
 * Copyright 2024 Andre Gebers
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.resteasy.guice;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.jboss.resteasy.core.ConstructorInjectorImpl;
import org.jboss.resteasy.core.InjectorFactoryImpl;
import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.resteasy.spi.ConstructorInjector;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.MethodInjector;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.ValueInjector;
import org.jboss.resteasy.spi.metadata.Parameter;
import org.jboss.resteasy.spi.metadata.ResourceClass;
import org.jboss.resteasy.spi.metadata.ResourceConstructor;
import org.jboss.resteasy.spi.metadata.ResourceLocator;

import com.google.inject.Injector;

import jakarta.ws.rs.WebApplicationException;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GuiceInjectorFactory implements InjectorFactory {

  private static Injector injector;

  public static Injector getInjector() {
    return injector;
  }

  public static void setInjector(Injector injector) {
    GuiceInjectorFactory.injector = injector;
  }

  public static Injector getInjectorOrFail() {
    Injector injector = getInjector();
    if(injector != null) {
      return injector;
    } else {
      throw new IllegalStateException("injector not setup yet");
    }
  }

  public static Injector getChildInjectorOrFail(HttpRequest request, HttpResponse response) {
    return getInjectorOrFail().createChildInjector(binder -> {
      binder.bind(HttpRequest.class).toInstance(request);
      binder.bind(HttpResponse.class).toInstance(response);
    });
  }

  private Object construct(Class cls, boolean unwrapAsync) {
    Object o = getInjectorOrFail().getInstance(cls);
    return unwrapAsync?CompletableFuture.completedStage(o):o;
  }

  private Object construct(Class cls, HttpRequest request, HttpResponse response, boolean unwrapAsync)
      throws Failure, WebApplicationException, ApplicationException {
    Object o = getChildInjectorOrFail(request, response).getInstance(cls);
    return unwrapAsync?CompletableFuture.completedStage(o):o;
  }

  @Override
  public ConstructorInjector createConstructor(Constructor constructor, ResteasyProviderFactory factory) {
    return new ConstructorInjector() {

      @Override
      public Object construct(boolean unwrapAsync) {
        return GuiceInjectorFactory.this.construct(constructor.getDeclaringClass(), unwrapAsync);
      }

      @Override
      public Object construct(HttpRequest request, HttpResponse response, boolean unwrapAsync)
          throws Failure, WebApplicationException, ApplicationException {
        return GuiceInjectorFactory.this.construct(constructor.getDeclaringClass(), request, response, unwrapAsync);
      }

      @Override
      public Object injectableArguments(boolean unwrapAsync) {
        return new ConstructorInjectorImpl(constructor, factory).injectableArguments(unwrapAsync);
      }

      @Override
      public Object injectableArguments(HttpRequest request, HttpResponse response, boolean unwrapAsync)
          throws Failure {
        return new ConstructorInjectorImpl(constructor, factory).injectableArguments(request, response, unwrapAsync);
      }};
  }

  @Override
  public ConstructorInjector createConstructor(ResourceConstructor constructor, ResteasyProviderFactory factory) {
    return new ConstructorInjector() {

      @Override
      public Object construct(boolean unwrapAsync) {
        return GuiceInjectorFactory.this.construct(constructor.getConstructor().getDeclaringClass(), unwrapAsync);
      }

      @Override
      public Object construct(HttpRequest request, HttpResponse response, boolean unwrapAsync)
          throws Failure, WebApplicationException, ApplicationException {
        return GuiceInjectorFactory.this.construct(constructor.getConstructor().getDeclaringClass(), request, response, unwrapAsync);
      }

      @Override
      public Object injectableArguments(boolean unwrapAsync) {
        return new ConstructorInjectorImpl(constructor, factory).injectableArguments(unwrapAsync);
      }

      @Override
      public Object injectableArguments(HttpRequest request, HttpResponse response, boolean unwrapAsync)
          throws Failure {
        return new ConstructorInjectorImpl(constructor, factory).injectableArguments(request, response, unwrapAsync);
      }};
  }

  private PropertyInjector createPropertyInjector() {
    return new PropertyInjector() {

      @Override
      public CompletionStage<Void> inject(Object target, boolean unwrapAsync) {
        getInjectorOrFail().injectMembers(target);
        return CompletableFuture.completedStage(null);
      }

      @Override
      public CompletionStage<Void> inject(HttpRequest request, HttpResponse response, Object target,
          boolean unwrapAsync) throws Failure, WebApplicationException, ApplicationException {
        getChildInjectorOrFail(request, response).injectMembers(target);
        return CompletableFuture.completedStage(null);
      }};
  }

  @Override
  public PropertyInjector createPropertyInjector(Class resourceClass, ResteasyProviderFactory factory) {
    return createPropertyInjector();
  }

  @Override
  public PropertyInjector createPropertyInjector(ResourceClass resourceClass, ResteasyProviderFactory providerFactory) {
    return createPropertyInjector();
  }

  @Override
  public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget,
      String defaultName, Class type, Type genericType, Annotation[] annotations, ResteasyProviderFactory factory) {
    return InjectorFactoryImpl.INSTANCE.createParameterExtractor(
        injectTargetClass, injectTarget, defaultName, type, genericType, annotations, factory);
  }

  @Override
  public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget,
      String defaultName, Class type, Type genericType, Annotation[] annotations, boolean useDefault,
      ResteasyProviderFactory factory) {
    return InjectorFactoryImpl.INSTANCE.createParameterExtractor(
        injectTargetClass, injectTarget, defaultName, type, genericType, annotations, useDefault, factory);
  }

  @Override
  public ValueInjector createParameterExtractor(Parameter parameter, ResteasyProviderFactory providerFactory) {
    return InjectorFactoryImpl.INSTANCE.createParameterExtractor(parameter, providerFactory);
  }

  @Override
  public MethodInjector createMethodInjector(ResourceLocator method, ResteasyProviderFactory factory) {
    return InjectorFactoryImpl.INSTANCE.createMethodInjector(method, factory);
  }

}
