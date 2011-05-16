/**
 *
 * Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.nodepool;

import static com.google.common.collect.Iterables.any;

import java.util.List;
import java.util.Properties;

import org.jclouds.byon.config.ConfiguresNodeStore;
import org.jclouds.byon.config.YamlNodeStoreModule;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.StandaloneComputeServiceContextBuilder;
import org.jclouds.nodepool.config.NodePoolComputeServiceContextModule;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * 
 * @author Adrian Cole
 */
public class NodePoolComputeServiceContextBuilder extends StandaloneComputeServiceContextBuilder<ComputeService> {

   public NodePoolComputeServiceContextBuilder(Properties props) {
      super(ComputeService.class, props);
   }

   @Override
   protected void addContextModule(List<Module> modules) {
      modules.add(new NodePoolComputeServiceContextModule());
   }

   @VisibleForTesting
   protected void addNodeStoreIfNotPresent(List<Module> modules) {
      if (!any(modules, new Predicate<Module>() {
         public boolean apply(Module input) {
            return input.getClass().isAnnotationPresent(ConfiguresNodeStore.class);
         }
      }

      )) {
         modules.add(new YamlNodeStoreModule());
      }
   }

   public Injector buildInjector() {
      addNodeStoreIfNotPresent(modules);
      return super.buildInjector();
   }
}
