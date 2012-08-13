package org.jclouds.aws.ec2.xml;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import org.jclouds.aws.ec2.domain.AWSVolume;
import org.jclouds.aws.ec2.domain.VolumeType;
import org.jclouds.date.DateCodecFactory;
import org.jclouds.ec2.xml.CreateVolumeResponseHandler;
import org.jclouds.location.Region;
import org.jclouds.location.Zone;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

/**
 * @author Vijay Kiran
 */
public class AWSCreateVolumeResponseHandler extends CreateVolumeResponseHandler {
   @Inject
   protected AWSCreateVolumeResponseHandler(DateCodecFactory dateCodecFactory, @Region Supplier<String> defaultRegion,
                                            @Zone Supplier<Map<String, Supplier<Set<String>>>> regionToZonesSupplier,
                                            @Zone Supplier<Set<String>> zonesSupplier) {
      super(dateCodecFactory, defaultRegion, regionToZonesSupplier, zonesSupplier);
   }

   private VolumeType volumeType;
   private int iops;

   private AWSVolume.Builder builder = AWSVolume.builder();

   public void endElement(String uri, String name, String qName) {
      if (qName.equals("volumetype")) {
         String volumeTypeString = currentText.toString().trim();
         volumeType = VolumeType.fromValue(volumeTypeString);
      }
   }

   @Override
   public AWSVolume getResult() {

      AWSVolume volume = new AWSVolume(region, id, size, snapshotId, availabilityZone, volumeStatus, createTime, attachments, VolumeType.STANDARD, 1);
      id = null;
      size = 0;
      snapshotId = null;
      availabilityZone = null;
      volumeStatus = null;
      createTime = null;
      attachments = Sets.newLinkedHashSet();
      volumeType = VolumeType.STANDARD;
      iops = 1;
      return volume;
   }
}
