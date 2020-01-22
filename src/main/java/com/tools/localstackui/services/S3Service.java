package com.tools.localstackui.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class S3Service {

  @Value("${aws.region.name}")
  String region;


  @Autowired
  private AmazonS3 amazonS3;

  public List<Bucket> getListofBuckets(){
    return this.amazonS3.listBuckets();
  }

  public Bucket createS3Bucket(String name){
    return this.amazonS3.createBucket(name);
  }

  public String deleteBucket(String name) {

    ObjectListing objectListing = amazonS3.listObjects(name);
    while (true) {
      Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
      while (objIter.hasNext()) {
        amazonS3.deleteObject(name, objIter.next().getKey());
      }
      if (objectListing.isTruncated()) {
        objectListing = amazonS3.listNextBatchOfObjects(objectListing);
      } else {
        break;
      }
    }
    this.amazonS3.deleteBucket(name);
    return "Successfully removed subscription";
  }

  public List<S3ObjectSummary> listInternal(String name) {
    ObjectListing objectListing = this.amazonS3.listObjects(name);
    for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
      System.out.println(os.getKey());
    }
    return objectListing.getObjectSummaries();
  }
}
