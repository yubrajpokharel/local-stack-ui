package com.tools.localstackui.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.tools.localstackui.services.S3Service;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class S3Controller {

  @Autowired
  S3Service s3Service;

  @RequestMapping(value = "/s3-buckets/{bucketName}", method = GET)
  public String getMessage(@PathVariable("bucketName") String bucketName, Model model) {
    List<S3ObjectSummary> objectListing = s3Service.listInternal(bucketName);
    model.addAttribute("bucketName", bucketName);
    model.addAttribute("objects", objectListing);
    return "indBucket";
  }
}
