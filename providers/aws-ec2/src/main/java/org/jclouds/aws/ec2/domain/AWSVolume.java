package org.jclouds.aws.ec2.domain;

import com.google.common.base.Objects;
import org.jclouds.ec2.domain.Attachment;
import org.jclouds.ec2.domain.Volume;

import java.util.Date;

/**
 * @author Vijay Kiran
 */
public class AWSVolume extends Volume {
   public static Builder builder() {
      return new Builder();
   }

   public static class Builder extends Volume.Builder {
      private VolumeType volumeType;
      private int iops;

      public Builder volumeType(VolumeType volumeType) {
         this.volumeType = volumeType;
         return this;
      }

      public Builder iops(int iops) {
         this.iops = iops;
         return this;
      }

      public Builder fromAWSVolume(Volume in) {
         return fromVolume(in).volumeType(VolumeType.STANDARD).iops(1);
      }

      public AWSVolume build() {

      }

   }

   private VolumeType volumeType;
   private int iops;


   public AWSVolume(String region, String id, int size, String snapshotId, String availabilityZone, Status status, Date createTime, Iterable<Attachment> attachments, VolumeType volumeType, int iops) {
      super(region, id, size, snapshotId, availabilityZone, status, createTime, attachments);
      this.volumeType = volumeType;
      this.iops = iops;
   }


   public VolumeType getVolumeType() {
      return volumeType;
   }

   public void setVolumeType(VolumeType volumeType) {
      this.volumeType = volumeType;
   }

   public int getIops() {
      return iops;
   }

   public void setIops(int iops) {
      this.iops = iops;
   }

   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      AWSVolume other = (AWSVolume) obj;
      if (volumeType == null) {
         if (other.volumeType != null)
            return false;
      } else if (!volumeType.equals(other.volumeType)) {
         return false;
      }
      if (iops != other.iops) {
         return false;
      }
      return true;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (volumeType != null ? volumeType.hashCode() : 0);
      result = 31 * result + iops;
      return result;
   }

   @Override
   public String toString() {
      return Objects.toStringHelper(this)
            .add("attachments", getAttachments())
            .add("availabilityZone", getAvailabilityZone())
            .add("createTime", getCreateTime())
            .add("id", getId())
            .add("region", getRegion())
            .add("size", getSize())
            .add("snapshotId", getSnapshotId())
            .add("status", getStatus())
            .add("volumeType", volumeType)
            .add("iops", iops).toString();
   }

}
