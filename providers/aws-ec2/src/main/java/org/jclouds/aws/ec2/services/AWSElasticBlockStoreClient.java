package org.jclouds.aws.ec2.services;

import org.jclouds.aws.ec2.domain.AWSVolume;
import org.jclouds.concurrent.Timeout;
import org.jclouds.ec2.services.ElasticBlockStoreClient;

import java.util.concurrent.TimeUnit;

/**
 * * Provides access to EC2 Instance Services via their REST API.
 * <p/>
 *
 * @author Vijay Kiran
 */
@Timeout(duration = 45, timeUnit = TimeUnit.SECONDS)
public interface AWSElasticBlockStoreClient extends ElasticBlockStoreClient {
   /**
    * Creates an Amazon EBS volume that can be attached to any Amazon EC2 instance in the same Availability Zone.
    * Any AWS Marketplace product codes from the snapshot are propagated to the volume.
    * For more information about Amazon EBS, see Amazon Elastic Block Store.
    *
    * @param availabilityZone The Availability Zone for the new volume. Use #DescribeAvailabilityZones
    *                         to display Availability Zones that are currently available to your account.
    * @param volumeType       The volume type, valid values: "standard" (default) or "io1"
    */
   AWSVolume createVolumeFromSnapshotInAvailabilityZoneWithVolumeType(
         String availabilityZone,
         String snapshotId,
         String volumeType);

   /**
    * Creates an Amazon EBS volume that can be attached to any Amazon EC2 instance in the same Availability Zone.
    * Any AWS Marketplace product codes from the snapshot are propagated to the volume.
    * For more information about Amazon EBS, see Amazon Elastic Block Store.
    *
    * @param availabilityZone The Availability Zone for the new volume. Use #DescribeAvailabilityZones
    *                         to display Availability Zones that are currently available to your account.
    * @param volumeType       The volume type, default value: "io1", if you specify "standard", the IOPS parameter is not used.
    * @param iops             The number of I/O operations per second (IOPS) that the volume supports, valid range is 1 to 1000.
    */
   AWSVolume createVolumeFromSnapshotInAvailabilityZoneWithVolumeTypeAndIops(
         String availabilityZone,
         String snapshotId,
         String volumeType,
         int iops);
}
