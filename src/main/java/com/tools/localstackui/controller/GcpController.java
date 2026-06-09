package com.tools.localstackui.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.tools.localstackui.services.GcpService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class GcpController {

  @Autowired
  GcpService gcpService;

  @GetMapping(value = "/gcp/pubsub/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getPubSubStatus() {
    return gcpService.getPubSubStatus();
  }

  @PostMapping(value = "/gcp/pubsub/start", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startPubSub() {
    return runAction(() -> gcpService.startPubSub());
  }

  @PostMapping(value = "/gcp/pubsub/stop", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopPubSub() {
    return runAction(() -> gcpService.stopPubSub());
  }

  @GetMapping(value = "/gcp/pubsub/topics", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getPubSubTopics() throws Exception {
    return gcpService.getPubSubTopics();
  }

  @PostMapping(value = "/gcp/pubsub/topics", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createPubSubTopic(@RequestParam String topicName) {
    return runAction(() -> gcpService.createPubSubTopic(topicName));
  }

  @GetMapping(value = "/gcp/pubsub/topics/{topicName}/subscriptions",
      produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getPubSubSubscriptionsForTopic(@PathVariable String topicName)
      throws Exception {
    return gcpService.getPubSubSubscriptionsForTopic(topicName);
  }

  @GetMapping(value = "/gcp/pubsub/subscriptions", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getPubSubSubscriptions() throws Exception {
    return gcpService.getPubSubSubscriptions();
  }

  @GetMapping(value = "/gcp/pubsub/subscriptions/{subscriptionName}/details",
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> getPubSubSubscription(@PathVariable String subscriptionName)
      throws Exception {
    return gcpService.getPubSubSubscription(subscriptionName);
  }

  @PostMapping(value = "/gcp/pubsub/subscriptions", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createPubSubSubscription(@RequestParam String subscriptionName,
      @RequestParam String topicName) {
    return runAction(() -> gcpService.createPubSubSubscription(subscriptionName, topicName));
  }

  @GetMapping(value = "/gcp/pubsub/subscriptions/{subscriptionName}/messages",
      produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> pullPubSubMessages(@PathVariable String subscriptionName,
      @RequestParam(defaultValue = "10") int limit) throws Exception {
    return gcpService.pullPubSubMessages(subscriptionName, limit);
  }

  @PostMapping(value = "/gcp/pubsub/topics/{topicName}/publish",
      consumes = MediaType.TEXT_PLAIN_VALUE, produces = APPLICATION_JSON_VALUE)
  public Map<String, String> publishPubSubMessage(@PathVariable String topicName,
      @RequestBody String message) {
    return runAction(() -> gcpService.publishPubSubMessage(topicName, message));
  }

  @GetMapping(value = "/gcp/storage/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getStorageStatus() {
    return gcpService.getStorageStatus();
  }

  @PostMapping(value = "/gcp/storage/start", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startStorage() {
    return runAction(() -> gcpService.startStorage());
  }

  @PostMapping(value = "/gcp/storage/stop", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopStorage() {
    return runAction(() -> gcpService.stopStorage());
  }

  @GetMapping(value = "/gcp/storage/buckets", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getStorageBuckets() throws Exception {
    return gcpService.getStorageBuckets();
  }

  @PostMapping(value = "/gcp/storage/buckets", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createStorageBucket(@RequestParam String bucketName) {
    return runAction(() -> gcpService.createStorageBucket(bucketName));
  }

  @DeleteMapping(value = "/gcp/storage/buckets/{bucketName}", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> deleteStorageBucket(@PathVariable String bucketName) {
    return runAction(() -> gcpService.deleteStorageBucket(bucketName));
  }

  @GetMapping(value = "/gcp/storage/buckets/{bucketName}/objects", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getStorageObjects(@PathVariable String bucketName) throws Exception {
    return gcpService.getStorageObjects(bucketName);
  }

  @PostMapping(value = "/gcp/storage/buckets/{bucketName}/objects",
      consumes = MediaType.TEXT_PLAIN_VALUE, produces = APPLICATION_JSON_VALUE)
  public Map<String, String> uploadStorageObject(@PathVariable String bucketName,
      @RequestParam String objectName, @RequestBody String content) {
    return runAction(() -> gcpService.uploadStorageObject(bucketName, objectName, content));
  }

  @PostMapping(value = "/gcp/storage/buckets/{bucketName}/objects/file",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
  public Map<String, String> uploadStorageFile(@PathVariable String bucketName,
      @RequestParam("file") MultipartFile file,
      @RequestParam(required = false) String objectName) {
    return runAction(() -> {
      String resolvedObjectName = objectName == null || objectName.trim().isEmpty()
          ? file.getOriginalFilename() : objectName.trim();
      return gcpService.uploadStorageObject(bucketName, resolvedObjectName, file.getBytes(),
          file.getContentType());
    });
  }

  @GetMapping(value = "/gcp/storage/buckets/{bucketName}/objects/download")
  public ResponseEntity<byte[]> downloadStorageObject(@PathVariable String bucketName,
      @RequestParam String objectName) throws Exception {
    GcpService.StorageObjectDownload download = gcpService.downloadStorageObject(bucketName,
        objectName);
    String downloadName = objectName.contains("/")
        ? objectName.substring(objectName.lastIndexOf("/") + 1) : objectName;
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
        .contentType(MediaType.parseMediaType(download.contentType()))
        .body(download.content());
  }

  @DeleteMapping(value = "/gcp/storage/buckets/{bucketName}/objects/{objectName}",
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> deleteStorageObject(@PathVariable String bucketName,
      @PathVariable String objectName) {
    return runAction(() -> gcpService.deleteStorageObject(bucketName, objectName));
  }

  @GetMapping(value = "/gcp/firestore/status", produces = APPLICATION_JSON_VALUE)
  public Map<String, Object> getFirestoreStatus() {
    return gcpService.getFirestoreStatus();
  }

  @PostMapping(value = "/gcp/firestore/start", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> startFirestore() {
    return runAction(() -> gcpService.startFirestore());
  }

  @PostMapping(value = "/gcp/firestore/stop", produces = APPLICATION_JSON_VALUE)
  public Map<String, String> stopFirestore() {
    return runAction(() -> gcpService.stopFirestore());
  }

  @GetMapping(value = "/gcp/firestore/collections", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getFirestoreCollections() throws Exception {
    return gcpService.getFirestoreCollections();
  }

  @GetMapping(value = "/gcp/firestore/collections/{collectionName}/documents",
      produces = APPLICATION_JSON_VALUE)
  public List<Map<String, String>> getFirestoreDocuments(@PathVariable String collectionName)
      throws Exception {
    return gcpService.getFirestoreDocuments(collectionName);
  }

  @PostMapping(value = "/gcp/firestore/collections/{collectionName}/documents",
      consumes = MediaType.TEXT_PLAIN_VALUE, produces = APPLICATION_JSON_VALUE)
  public Map<String, String> createFirestoreDocument(@PathVariable String collectionName,
      @RequestParam String documentName, @RequestBody String content) {
    return runAction(() -> gcpService.createFirestoreDocument(collectionName, documentName, content));
  }

  @DeleteMapping(value = "/gcp/firestore/collections/{collectionName}/documents/{documentName}",
      produces = APPLICATION_JSON_VALUE)
  public Map<String, String> deleteFirestoreDocument(@PathVariable String collectionName,
      @PathVariable String documentName) {
    return runAction(() -> gcpService.deleteFirestoreDocument(collectionName, documentName));
  }

  private Map<String, String> runAction(Action action) {
    Map<String, String> response = new LinkedHashMap<>();
    try {
      response.put("status", "success");
      response.put("message", action.run());
    } catch (Exception e) {
      response.put("status", "error");
      response.put("message", e.getMessage());
    }
    return response;
  }

  private interface Action {
    String run() throws Exception;
  }
}
