package org.jclouds.aws.ec2.services;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jclouds.aws.ec2.AWSEC2ApiMetadata;
import org.jclouds.ec2.domain.Volume;
import org.jclouds.ec2.predicates.VolumeAvailable;
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

   @Test(enabled = false, dependsOnMethods = "testCreateSnapshotInRegion")
   void testCreateVolumeFromSnapshotInAvailabilityZone() {
      Volume volume = client.createVolumeFromSnapshotInAvailabilityZoneWithVolumeType(defaultZone, snapshot.getId(), "standard");
      assertNotNull(volume);

      Predicate<Volume> availabile = new RetryablePredicate<Volume>(new VolumeAvailable(client), 600, 10,
            TimeUnit.SECONDS);
      assert availabile.apply(volume);

      Volume result = Iterables.getOnlyElement(client.describeVolumesInRegion(snapshot.getRegion(), volume.getId()));
      assertEquals(volume.getId(), result.getId());
      assertEquals(volume.getSnapshotId(), snapshot.getId());
      assertEquals(volume.getAvailabilityZone(), defaultZone);
      assertEquals(result.getStatus(), Volume.Status.AVAILABLE);

      client.deleteVolumeInRegion(snapshot.getRegion(), volume.getId());
   }
}
