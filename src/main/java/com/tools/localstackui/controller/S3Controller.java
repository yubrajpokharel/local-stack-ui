package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.model.Message;
import com.tools.localstackui.services.S3Service;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class S3Controller {

  @Autowired
  S3Service s3Service;

  @RequestMapping(value = "/s3-buckets/{bucketName}", method = GET)
  public String getMessage(@PathVariable("bucketName") String bucketName, Model model) {
    System.out.println(bucketName);
    List<S3ObjectSummary> objectListing =  s3Service.listInternal(bucketName);
    model.addAttribute("bucketName", bucketName);
    model.addAttribute("objects", objectListing);
    return "indBucket";
  }

}
