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

import java.util.Properties;

import org.jclouds.Constants;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * 
 * @author Adrian Cole
 */
@Test(groups = "live", singleThreaded = true, testName = "BaseNodePoolLiveTest")
public abstract class BaseNodePoolLiveTest {
   public static final String CONTAINER_PREFIX = System.getProperty("user.name") + "-blobstore";

   protected ComputeServiceContext computeContext;
   protected BlobStoreContext blobStoreContext;

   @BeforeGroups(groups = "live")
   protected void setupContexts() {

      setupBlobStoreContext();
      setupComputeContext();

   }

   protected void setupBlobStoreContext() {

      String blobstoreProvider = System.getProperty("test.nodepool.blobstore.provider", "transient");
      blobStoreContext = new BlobStoreContextFactory().createContext(blobstoreProvider, System.getProperty(
               "test.nodepool.blobstore.identity", "foo"), System.getProperty("test.nodepool.blobstore.crendential",
               "bar"), blobStoreModules(), propertiesFor("blobstore", blobstoreProvider));

      blobStoreContext.getBlobStore().list();
   }

   protected void setupComputeContext() {
      String computeProvider = System.getProperty("test.nodepool.compute.provider", "stub");

      computeContext = new ComputeServiceContextFactory().createContext(computeProvider, System.getProperty(
               "test.nodepool.compute.identity", "foo"),
               System.getProperty("test.nodepool.compute.crendential", "bar"), computeModules(blobStoreContext),
               propertiesFor("compute", computeProvider));

      computeContext.getComputeService().listNodes();

   }

   protected ImmutableSet<Module> blobStoreModules() {
      return ImmutableSet.<Module> of(new Log4JLoggingModule());
   }

   protected ImmutableSet<Module> computeModules(BlobStoreContext blobStoreContext) {
      return ImmutableSet.<Module> of(new Log4JLoggingModule());
   }

   protected Properties propertiesFor(String scope, String provider) {
      String endpoint = System.getProperty("test.nodepool." + scope + ".endpoint");
      String apiversion = System.getProperty("test.nodepool." + scope + ".apiversion");
      Properties overrides = new Properties();
      overrides.setProperty(Constants.PROPERTY_TRUST_ALL_CERTS, "true");
      overrides.setProperty(Constants.PROPERTY_RELAX_HOSTNAME, "true");
      if (endpoint != null)
         overrides.setProperty(provider + ".endpoint", endpoint);
      if (apiversion != null)
         overrides.setProperty(provider + ".apiversion", apiversion);
      return overrides;
   }

   @AfterGroups(groups = "live")
   protected void closeContexts() {
      if (computeContext != null)
         computeContext.close();
      if (blobStoreContext != null)
         blobStoreContext.close();
   }
}
