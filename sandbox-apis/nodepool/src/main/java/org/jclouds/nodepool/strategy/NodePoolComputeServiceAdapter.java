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
package org.jclouds.nodepool.strategy;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.byon.Node;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceAdapter;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.predicates.RetryablePredicate;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * 
 * 
 */
@Singleton
public class NodePoolComputeServiceAdapter implements ComputeServiceAdapter<NodeMetadata, Hardware, Image, Location> {
   static class NodeMetadataByReservationId implements Function<String, NodeMetadata> {

      private final ComputeService client;
      private final Map<String, Node> reservations;

      @Inject
      public NodeMetadataByReservationId(ComputeService client, Map<String, Node> reservations) {
         this.client = checkNotNull(client, "client");
         this.reservations = checkNotNull(reservations, "reservations");
      }

      @Override
      public NodeMetadata apply(String reservationKey) {
         if (reservationKey == null)
            return null;
         Node frontendNode = reservations.get(reservationKey);
         NodeMetadata backendNode = client.getNodeMetadata(backendIdFor(reservationKey));
         return NodeMetadataBuilder.fromNodeMetadata(backendNode).ids(frontendNode.getId())
                  .name(frontendNode.getName()).tags(frontendNode.getTags()).group(frontendNode.getGroup()).uri(null)
                  .build();
      }
   }

   private final ComputeService client;
   private final Map<String, NodeRequest> requests;
   private final Map<String, Node> reservations;
   private final Map<String, Node> tombstones;
   private final NodeMetadataByReservationId nodeMetadataByReservationId;
   private final Supplier<String> frontendIdGenerator;

   @Inject
   public NodePoolComputeServiceAdapter(ComputeService client, @Named("requests") Map<String, NodeRequest> requests,
            @Named("reservations") Map<String, Node> reservations, @Named("tombstones") Map<String, Node> tombstones,
            NodeMetadataByReservationId nodeMetadataByReservationId) {
      this.client = checkNotNull(client, "client");
      this.requests = checkNotNull(requests, "requests");
      this.reservations = checkNotNull(reservations, "reservations");
      this.tombstones = checkNotNull(tombstones, "tombstones");
      this.nodeMetadataByReservationId = checkNotNull(nodeMetadataByReservationId, "nodeMetadataByReservationId");
      // to save unnecessary round trips, we'll have the client generate uuids, assuming there won't
      // be any clashes
      // TODO: guicify
      this.frontendIdGenerator = new Supplier<String>() {

         @Override
         public String get() {
            return UUID.randomUUID().toString();
         }

      };
   }

   @Override
   public NodeMetadata createNodeWithGroupEncodedIntoNameThenStoreCredentials(String group, String name,
            Template template, Map<String, Credentials> credentialStore) {
      // create the backend object using parameters from the template.
      OperatingSystem os = template.getImage().getOperatingSystem();
      Node node = Node.builder().id(frontendIdGenerator.get()).group(group).name(name).tags(
               template.getOptions().getTags()).os64Bit(os.is64Bit()).osArch(os.getArch()).osDescription(
               os.getDescription()).osVersion(os.getVersion()).locationId(template.getLocation().getId()).build();
      // TODO make a hook to actually create the new reservation from available nodes (ex. backend
      // nodes that match the inputs and are not reserved or tombstoned. runScriptOnNode when
      // reserved, then add to reservations and remove entry from requests.
      requests.put(node.getId(), new NodeRequest(node, template.getOptions()));
      return toPendingNode.apply(node);
   }

   static final Function<Node, NodeMetadata> toPendingNode = new Function<Node, NodeMetadata>() {
      public NodeMetadata apply(Node node) {

         NodeMetadataBuilder builder = new NodeMetadataBuilder();
         builder.ids(node.getId());
         builder.name(node.getName());
         // TODO
         // builder.location(node.getLocationId());
         builder.group(node.getGroup());
         builder.tags(node.getTags());
         builder.operatingSystem(OperatingSystem.builder().arch(node.getOsArch()).family(
                  OsFamily.fromValue(node.getOsFamily())).description(node.getOsDescription()).version(
                  node.getOsVersion()).build());
         builder.state(NodeState.PENDING);
         builder.publicAddresses(ImmutableSet.<String> of(node.getHostname()));
         return builder.build();

      }
   };

   public static class NodeRequest {
      private final Node node;
      private final TemplateOptions options;

      public NodeRequest(Node node, TemplateOptions options) {
         this.node = node;
         this.options = options;
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(node.getId());
      }

      @Override
      public boolean equals(Object that) {
         if (that == null)
            return false;
         return Objects.equal(this.toString(), that.toString());
      }

      @Override
      public String toString() {
         return String.format("[node=%s, options=%s]", node, options);
      }

   }

   @Override
   public Iterable<Hardware> listHardwareProfiles() {
      // Note that not all hardware profiles may be available. We will probably need to constrain
      // this to a pseudo-list built from backend nodes
      return (Iterable<Hardware>) client.listHardwareProfiles();
   }

   @Override
   public Iterable<Image> listImages() {
      // Note that not all images may be available. We will probably need to constrain
      // this to a pseudo-list built from backend nodes
      return (Iterable<Image>) client.listImages();
   }

   @Override
   public Iterable<NodeMetadata> listNodes() {
      // TODO exclude reservation ids who have tombstoned entries included
      return Iterables.concat(Iterables.transform(Iterables.transform(requests.values(),
               new Function<NodeRequest, Node>() {

                  @Override
                  public Node apply(NodeRequest arg0) {
                     return arg0.node;
                  }

               }), toPendingNode), Iterables.filter(Iterables.transform(reservations.keySet(),
               nodeMetadataByReservationId), Predicates.notNull()));
   }

   @Override
   public Iterable<Location> listLocations() {
      // Note that not all locations may be available. We will probably need to constrain
      // this to a pseudo-list built from backend nodes
      return (Iterable<Location>) client.listAssignableLocations();
   }

   static String frontendIdFor(String id) {
      Matcher matcher = ID_PATTERN.matcher(id);
      return matcher.find() ? matcher.group(1) : null;
   }

   static String backendIdFor(String id) {
      Matcher matcher = ID_PATTERN.matcher(id);
      return matcher.find() ? matcher.group(2) : null;
   }

   static Pattern ID_PATTERN = Pattern.compile("(.*)->(.*)");

   static Predicate<String> frontendIdEquals(final String frontendId) {
      return new Predicate<String>() {

         @Override
         public boolean apply(String arg0) {
            return frontendId.equals(frontendIdFor(arg0));
         }

      };
   }

   static Predicate<String> backendIdEquals(final String backendId) {
      return new Predicate<String>() {

         @Override
         public boolean apply(String arg0) {
            return backendId.equals(backendIdFor(arg0));
         }

      };
   }

   @Override
   public NodeMetadata getNode(String id) {
      try {
         return nodeMetadataByReservationId.apply(getBackendIdForFrontendId(id));
      } catch (NoSuchElementException e) {
         return null;
      }
   }

   /**
    * first thing is to synchronize the state by transitioning to a
    */
   @Override
   public void destroyNode(String frontendId) {
      try {
         String reservationKey = reservationKeyForFrontendId(frontendId);
         final String backendId = backendIdFor(reservationKey);
         Node node = reservations.get(reservationKey);
         if (node != null) {
            // tombstoning allows us to mark the node unavailable so that we can perform cleanup
            tombstones.put(backendId, node);
            // before we clear the reservation, lets ensure that the backing store is coherent, a
            // concern that happens often with eventually consistent blobstores
            boolean tombstoned = new RetryablePredicate<Map<String, Node>>(new Predicate<Map<String, Node>>() {

               @Override
               public boolean apply(Map<String, Node> arg0) {
                  return arg0.containsKey(backendId);
               }

            }, 1000, 100, TimeUnit.MILLISECONDS).apply(tombstones);
            if (!tombstoned)
               throw new IllegalStateException("error adding tombstone for " + backendId);
            // the above blocking could be eliminated if we had a background task polling on all new
            // tombstones. Such logic would handle the below.

            // now that it is tombstoned, we can safely clear the reservation and perform and
            // cleanup
            reservations.remove(reservationKey);
            // TODO perform some sort of cleanup now that this is tombstoned
            // now, remove the tombstone, noting that it isn't important we block on this
            tombstones.remove(backendId);
         }
      } catch (NoSuchElementException e) {

      }
   }

   @Override
   public void rebootNode(String id) {
      client.rebootNode(getBackendIdForFrontendId(id));
   }

   private String getBackendIdForFrontendId(String id) {
      return backendIdFor(reservationKeyForFrontendId(id));
   }

   private String reservationKeyForFrontendId(String id) {
      return Iterables.find(reservations.keySet(), frontendIdEquals(id));
   }

   @Override
   public void resumeNode(String id) {
      client.resumeNode(getBackendIdForFrontendId(id));

   }

   @Override
   public void suspendNode(String id) {
      client.suspendNode(getBackendIdForFrontendId(id));
   }
}