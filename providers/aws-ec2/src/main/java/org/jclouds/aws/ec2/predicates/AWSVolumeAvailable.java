package org.jclouds.aws.ec2.predicates;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.jclouds.aws.ec2.domain.AWSVolume;
import org.jclouds.aws.ec2.services.AWSElasticBlockStoreClient;
import org.jclouds.ec2.domain.Volume;
import org.jclouds.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Singleton;

/**
 * @author Vijay Kiran
 */
@Singleton
public class AWSVolumeAvailable implements Predicate<AWSVolume> {

   private final AWSElasticBlockStoreClient client;
   @Resource
   protected Logger logger = Logger.NULL;

   @Inject
   public AWSVolumeAvailable(AWSElasticBlockStoreClient client) {
      this.client = client;
   }

   public boolean apply(AWSVolume volume) {
      logger.trace("looking for status on volume %s", volume.getId());
      volume = (AWSVolume) Iterables.getOnlyElement(client.describeVolumesInRegion(volume.getRegion(), volume
            .getId()));
      logger.trace("%s: looking for status %s: currently: %s", volume, Volume.Status.AVAILABLE,
            volume.getStatus());
      return volume.getStatus() == Volume.Status.AVAILABLE;
   }
}