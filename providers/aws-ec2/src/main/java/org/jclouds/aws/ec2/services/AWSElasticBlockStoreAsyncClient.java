package org.jclouds.aws.ec2.services;

import com.google.common.util.concurrent.ListenableFuture;
import org.jclouds.aws.ec2.domain.AWSVolume;
import org.jclouds.aws.filters.FormSigner;
import org.jclouds.ec2.domain.Volume;
import org.jclouds.ec2.services.ElasticBlockStoreAsyncClient;
import org.jclouds.ec2.xml.CreateVolumeResponseHandler;
import org.jclouds.location.functions.ZoneToEndpoint;
import org.jclouds.rest.annotations.EndpointParam;
import org.jclouds.rest.annotations.FormParams;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.VirtualHost;
import org.jclouds.rest.annotations.XMLResponseParser;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import static org.jclouds.aws.reference.FormParameters.ACTION;

/**
 * Provides access to EC2 Elastic Block Store services.
 * <p/>
 *
 * @author Vijay Kiran
 */
@RequestFilters(FormSigner.class)
@VirtualHost
public interface AWSElasticBlockStoreAsyncClient extends ElasticBlockStoreAsyncClient {

   /**
    * Creates an Amazon EBS volume that can be attached to any Amazon EC2 instance in the same Availability Zone.
    * Any AWS Marketplace product codes from the snapshot are propagated to the volume.
    * For more information about Amazon EBS, see Amazon Elastic Block Store.
    *
    * @param availabilityZone The Availability Zone for the new volume. Use #DescribeAvailabilityZones
    *                         to display Availability Zones that are currently available to your account.
    * @param volumeType       The volume type, valid values: "standard" (default) or "io1"
    */
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "CreateVolume")
   @XMLResponseParser(CreateVolumeResponseHandler.class)
   ListenableFuture<AWSVolume> createVolumeFromSnapshotInAvailabilityZoneWithVolumeType(
         @EndpointParam(parser = ZoneToEndpoint.class) @FormParam("AvailabilityZone") String availabilityZone,
         @FormParam("SnapshotId") String snapshotId,
         @FormParam("VolumeType") String volumeType);

   /**
    * Creates an Amazon EBS volume that can be attached to any Amazon EC2 instance in the same Availability Zone.
    * Any AWS Marketplace product codes from the snapshot are propagated to the volume.
    * For more information about Amazon EBS, see Amazon Elastic Block Store.
    *
    * @param availabilityZone The Availability Zone for the new volume. Use #DescribeAvailabilityZones
    *                         to display Availability Zones that are currently available to your account.
    * @param volumeType       The volume type, valid values: "standard" (default) or "io1"
    * @param iops             The number of I/O operations per second (IOPS) that the volume supports, valid range is 1 to 1000.
    */
   @POST
   @Path("/")
   @FormParams(keys = ACTION, values = "CreateVolume")
   @XMLResponseParser(CreateVolumeResponseHandler.class)
   ListenableFuture<AWSVolume> createVolumeFromSnapshotInAvailabilityZoneWithVolumeTypeAndIops(
         @EndpointParam(parser = ZoneToEndpoint.class) @FormParam("AvailabilityZone") String availabilityZone,
         @FormParam("SnapshotId") String snapshotId,
         @FormParam("VolumeType") String volumeType,
         @FormParam("Iops") int iops);


}
