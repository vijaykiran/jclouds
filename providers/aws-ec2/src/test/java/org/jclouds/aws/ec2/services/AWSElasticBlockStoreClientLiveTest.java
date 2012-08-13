package org.jclouds.aws.ec2.services;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jclouds.aws.ec2.AWSEC2ApiMetadata;
import org.jclouds.aws.ec2.domain.AWSVolume;
import org.jclouds.aws.ec2.domain.VolumeType;
import org.jclouds.aws.ec2.predicates.AWSVolumeAvailable;
import org.jclouds.ec2.domain.Volume;
import org.jclouds.ec2.services.ElasticBlockStoreClientLiveTest;
import org.jclouds.predicates.RetryablePredicate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Vijay Kiran
 */
@Test(groups = "live", singleThreaded = true, testName = "AWSElasticBlockStoreClientLiveTest")
public class AWSElasticBlockStoreClientLiveTest extends ElasticBlockStoreClientLiveTest {

   public AWSElasticBlockStoreClientLiveTest() {
      provider = "aws-ec2";
   }

   private AWSElasticBlockStoreClient client;

   @Override
   @BeforeClass(groups = {"integration", "live"})
   public void setupContext() {
      super.setupContext();
      client = view.unwrap(AWSEC2ApiMetadata.CONTEXT_TOKEN).getApi().getElasticBlockStoreServices();
   }

   @Test
   protected void testCreateVolumeInAvailabilityZone() {
      super.testCreateVolumeInAvailabilityZone();
   }

   @Test(dependsOnMethods = "testCreateVolumeInAvailabilityZone")
   protected void testCreateSnapshotInRegion() {
      super.testCreateSnapshotInRegion();
   }

   @Test(dependsOnMethods = "testCreateSnapshotInRegion")
   void testCreateVolumeFromSnapshotInAvailabilityZoneWithVolumeTypeAndIops() {
      AWSVolume volume = client.createVolumeFromSnapshotInAvailabilityZoneWithVolumeTypeAndIops(defaultZone, snapshot.getId(), "standard", 500);
      assertNotNull(volume);

      Predicate<AWSVolume> available = new RetryablePredicate<AWSVolume>(new AWSVolumeAvailable(client), 600, 10,
            TimeUnit.SECONDS);
      assert available.apply(volume);

      AWSVolume result = (AWSVolume) Iterables.getOnlyElement(client.describeVolumesInRegion(snapshot.getRegion(), volume.getId()));
      assertEquals(volume.getId(), result.getId());
      assertEquals(volume.getSnapshotId(), snapshot.getId());
      assertEquals(volume.getAvailabilityZone(), defaultZone);
      assertEquals(result.getStatus(), Volume.Status.AVAILABLE);
      assertEquals(result.getVolumeType(), VolumeType.STANDARD);
      assertEquals(result.getIops(), 500);

      client.deleteVolumeInRegion(snapshot.getRegion(), volume.getId());
   }
}
