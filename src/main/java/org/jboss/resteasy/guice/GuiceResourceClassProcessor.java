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

import java.lang.reflect.Constructor;

import org.jboss.resteasy.spi.metadata.ConstructorParameter;
import org.jboss.resteasy.spi.metadata.FieldParameter;
import org.jboss.resteasy.spi.metadata.ResourceClass;
import org.jboss.resteasy.spi.metadata.ResourceClassProcessor;
import org.jboss.resteasy.spi.metadata.ResourceConstructor;
import org.jboss.resteasy.spi.metadata.ResourceLocator;
import org.jboss.resteasy.spi.metadata.ResourceMethod;
import org.jboss.resteasy.spi.metadata.SetterParameter;

import jakarta.ws.rs.ext.Provider;

@Provider
public class GuiceResourceClassProcessor implements ResourceClassProcessor {

  @Override
  public ResourceClass process(final ResourceClass cls) {
    return new ResourceClass() {

      private ResourceClass self = this;

      @Override
      public String getPath() {
        return cls.getPath();
      }

      @Override
      public Class<?> getClazz() {
        return cls.getClazz();
      }

      @Override
      public ResourceConstructor getConstructor() {
        ResourceConstructor rc = cls.getConstructor();
        if(rc != null) {
          return rc;
        } else {
          return new ResourceConstructor() {

            @Override
            public ResourceClass getResourceClass() {
              return self;
            }

            @Override
            public Constructor<?> getConstructor() {
//              Pick any constructor, at this stage. Guice will pick the Inject annotated constructor later automatically
//              This is to get around the following exception which seems to happen when there is no no-arg constructor
//              java.lang.RuntimeException: RESTEASY003190: Could not find constructor for class: agebe.WsConstructor
//                at org.jboss.resteasy.spi.metadata.ResourceBuilder.getConstructor(ResourceBuilder.java:712)
//                at org.jboss.resteasy.plugins.server.resourcefactory.POJOResourceFactory.registered(POJOResourceFactory.java:54)
//                at org.jboss.resteasy.core.ResourceMethodRegistry.addResourceFactory(ResourceMethodRegistry.java:218)
//                at org.jboss.resteasy.core.ResourceMethodRegistry.addResourceFactory(ResourceMethodRegistry.java:201)
//                at org.jboss.resteasy.core.ResourceMethodRegistry.addResourceFactory(ResourceMethodRegistry.java:184)
//                at org.jboss.resteasy.core.ResourceMethodRegistry.addResourceFactory(ResourceMethodRegistry.java:171)
//                at org.jboss.resteasy.core.ResourceMethodRegistry.addResourceFactory(ResourceMethodRegistry.java:156)
//                at org.jboss.resteasy.core.ResourceMethodRegistry.addPerRequestResource(ResourceMethodRegistry.java:81)
//                at org.jboss.resteasy.core.ResteasyDeploymentImpl.registerResources(ResteasyDeploymentImpl.java:543)
//                at org.jboss.resteasy.core.ResteasyDeploymentImpl.registration(ResteasyDeploymentImpl.java:454)
//                at org.jboss.resteasy.core.ResteasyDeploymentImpl.startInternal(ResteasyDeploymentImpl.java:162)
//                at org.jboss.resteasy.core.ResteasyDeploymentImpl.start(ResteasyDeploymentImpl.java:127)
//                at org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher.init(ServletContainerDispatcher.java:134)
//                at org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher.init(HttpServletDispatcher.java:39)
              return cls.getClazz().getConstructors()[0];
            }

            @Override
            public ConstructorParameter[] getParams() {
              // constructor parameters do not matter at this stage.
              return new ConstructorParameter[0];
            }
          };
        }
      }

      @Override
      public FieldParameter[] getFields() {
        return cls.getFields();
      }

      @Override
      public SetterParameter[] getSetters() {
        return cls.getSetters();
      }

      @Override
      public ResourceMethod[] getResourceMethods() {
        return cls.getResourceMethods();
      }

      @Override
      public ResourceLocator[] getResourceLocators() {
        return cls.getResourceLocators();
      }
    };
  }

}
