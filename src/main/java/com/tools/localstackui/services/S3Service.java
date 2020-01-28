package com.tools.localstackui.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Service {

  @Value("${aws.region.name}")
  String region;


  @Autowired
  private AmazonS3 amazonS3;

  public List<Bucket> getListofBuckets() {
    return this.amazonS3.listBuckets();
  }

  public Bucket createS3Bucket(String name) {
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
    return objectListing.getObjectSummaries();
  }

  public String uploadFile(MultipartFile multipartFile, String bucketName) {
    String fileUrl = "";
    try {
      File file = convertMultiPartToFile(multipartFile);
      String fileName = generateFileName(multipartFile);
      fileUrl = "http://localhost:4572" + "/" + bucketName + "/" + fileName;
      uploadFileTos3bucket(fileName, file, bucketName);
      file.delete();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return fileUrl;
  }

  private File convertMultiPartToFile(MultipartFile file) throws IOException {
    File convFile = new File(file.getOriginalFilename());
    FileOutputStream fos = new FileOutputStream(convFile);
    fos.write(file.getBytes());
    fos.close();
    return convFile;
  }

  private String generateFileName(MultipartFile multiPart) {
    return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
  }

  private void uploadFileTos3bucket(String fileName, File file, String bucketName) {
    amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file)
        .withCannedAcl(CannedAccessControlList.PublicRead));
  }

  public String deleteFileFromS3Bucket(String fileUrl, String bucketName) {
    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
    return "Successfully deleted";
  }
}
