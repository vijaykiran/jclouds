package org.jclouds.aws.ec2.services;

import com.google.inject.TypeLiteral;
import org.jclouds.aws.ec2.xml.AWSDescribeInstancesResponseHandler;
import org.jclouds.ec2.xml.CreateVolumeResponseHandler;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.functions.ParseSax;
import org.jclouds.rest.functions.ReturnEmptySetOnNotFoundOr404;
import org.jclouds.rest.internal.RestAnnotationProcessor;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Vijay Kiran
 */
@Test(groups = "unit", testName = "AWSElasticBlockStoreAsyncClientTest")
public class AWSElasticBlockStoreAsyncClientTest extends BaseAWSEC2AsyncClientTest<AWSElasticBlockStoreAsyncClient> {

   public void testCreateVolumeFromSnapshotInAvailabilityZoneWithVolumeType() throws Exception {
      Method method = AWSElasticBlockStoreAsyncClient.class.getMethod("createVolumeFromSnapshotInAvailabilityZoneWithVolumeType",
            String.class,
            String.class,
            String.class);
      HttpRequest request = processor.createRequest(method, "us-east-1a", "snapshotId", "standard");

      assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
      assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
      assertPayloadEquals(request,
            "Action=CreateVolume&AvailabilityZone=us-east-1a&VolumeType=standard&SnapshotId=snapshotId",
            "application/x-www-form-urlencoded", false);

      assertResponseParserClassEquals(method, request, ParseSax.class);
      assertSaxResponseParserClassEquals(method, CreateVolumeResponseHandler.class);
      assertExceptionParserClassEquals(method, null);

      checkFilters(request);
   }


   public void testCreateVolumeFromSnapshotInAvailabilityZoneWithVolumeTypeAndIops() throws Exception {
      Method method = AWSElasticBlockStoreAsyncClient.class.getMethod("createVolumeFromSnapshotInAvailabilityZoneWithVolumeTypeAndIops",
            String.class,
            String.class,
            String.class,
            int.class);
      HttpRequest request = processor.createRequest(method, "us-east-1a", "snapshotId", "standard", 23);

      assertRequestLineEquals(request, "POST https://ec2.us-east-1.amazonaws.com/ HTTP/1.1");
      assertNonPayloadHeadersEqual(request, "Host: ec2.us-east-1.amazonaws.com\n");
      assertPayloadEquals(request,
            "Action=CreateVolume&AvailabilityZone=us-east-1a&VolumeType=standard&SnapshotId=snapshotId&Iops=23",
            "application/x-www-form-urlencoded", false);

      assertResponseParserClassEquals(method, request, ParseSax.class);
      assertSaxResponseParserClassEquals(method, CreateVolumeResponseHandler.class);
      assertExceptionParserClassEquals(method, null);

      checkFilters(request);
   }

   @Override
   protected TypeLiteral<RestAnnotationProcessor<AWSElasticBlockStoreAsyncClient>> createTypeLiteral() {
      return new TypeLiteral<RestAnnotationProcessor<AWSElasticBlockStoreAsyncClient>>() {
      };
   }
}
