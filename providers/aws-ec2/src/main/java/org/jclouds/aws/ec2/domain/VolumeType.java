package org.jclouds.aws.ec2.domain;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * VolumeType
 *
 * @author Vijay Kiran
 * @see org.jclouds.aws.ec2.services.AWSElasticBlockStoreAsyncClient#createVolumeFromSnapshotInAvailabilityZoneWithVolumeType(String, String, String)
 * @see org.jclouds.aws.ec2.services.AWSElasticBlockStoreAsyncClient#createVolumeFromSnapshotInAvailabilityZoneWithVolumeTypeAndIops(String, String, String, int)
 */
public enum VolumeType {
   STANDARD, IO1, UNRECOGNIZED;

   public String value() {
      return name().toLowerCase();
   }

   @Override
   public String toString() {
      return value();
   }

   public static VolumeType fromValue(String volumeType) {
      try {
         return valueOf(checkNotNull(volumeType, "volumetype").toUpperCase());
      } catch (IllegalArgumentException e) {
         return UNRECOGNIZED;
      }
   }
}
