package com.tools.localstackui.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class S3Service {

  @Autowired
  private AmazonS3 amazonS3;

  public void getListofBuckets(){
    List<Bucket> bucketList = this.amazonS3.listBuckets();
  }

  public Bucket createS3Bucket(String name){
    return this.amazonS3.createBucket(name);
  }

  public Bucket deleteBucket(String name){
    return this.deleteBucket(name);
  }
}
