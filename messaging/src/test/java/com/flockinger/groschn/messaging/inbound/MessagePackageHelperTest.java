package com.flockinger.groschn.messaging.inbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.messaging.ExecutorConfig;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.RequestHeader;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.util.BeanValidator;
import com.flockinger.groschn.messaging.util.MessagingContext;
import com.flockinger.groschn.messaging.util.TestBlock;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.ImmutableList;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes =  {BeanValidator.class, MessagePackageHelper.class, MessagingContext.class, Compressor.class})
@Import(ExecutorConfig.class)
public class MessagePackageHelperTest {

  @MockBean
  private ClusterCommunicationService commMock;
  @MockBean
  private NetworkStatistics statsMock ;
  @Autowired
  private BeanValidator validator ;
  @Autowired
  private MessagingContext utils;
  @Autowired
  private Compressor compressor;

  private MessagePackageHelper helper;

  @Before
  public void setup () {
    helper = new MessagePackageHelper(statsMock,validator,utils);
  }

  @Test
  public void testVerifyAndUnpackRequest_withValidRequest_shouldUnpackAndReturn() {
    var nodeId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of(nodeId));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(anyLong())).thenReturn(null);

    Optional<SyncRequest> unpackedRequest = helper.verifyAndUnpackRequest(validMessage(nodeId, fakeSyncRequest()),cacheMock);

    assertTrue("verify that response is not empty", unpackedRequest.isPresent());
    assertEquals("verify correct request package size", 10L, unpackedRequest.get().getRequestPackageSize().longValue());
    assertEquals("verify correct request starting position", 23L, unpackedRequest.get().getStartingPosition().longValue());
    assertEquals("verify correct request header count", 1L, unpackedRequest.get().getWantedHeaders().size());
    assertEquals("verify correct request header position", 23L, unpackedRequest.get().getWantedHeaders().get(0).getPosition().longValue());
    assertEquals("verify correct request header hash", "hash23", unpackedRequest.get().getWantedHeaders().get(0).getHash());
  }


  @Test
  public void testVerifyAndUnpackRequest_withValidRequestFromUnknownNode_shouldReturnEmpty() {
    var nodeId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of("groschn1", "groschn2"));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(any())).thenReturn(null);

    Optional<SyncRequest> unpackedRequest = helper.verifyAndUnpackRequest(validMessage(nodeId, fakeSyncRequest()),cacheMock);

    assertFalse("verify that response is empty", unpackedRequest.isPresent());
  }

  @Test
  public void testVerifyAndUnpackRequest_withRequestAlreadyReceived_shouldReturnEmpty() {
    var nodeId = UUID.randomUUID().toString();
    var messageId = "duplicate";
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of("groschn1", "groschn2"));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(eq("messageId"))).thenReturn("messageId");
    var message = validMessage(nodeId, fakeSyncRequest());
    message.setId(messageId);

    Optional<SyncRequest> unpackedRequest = helper.verifyAndUnpackRequest(message,cacheMock);

    assertFalse("verify that response is empty", unpackedRequest.isPresent());
  }


  @Test
  public void testVerifyAndUnpackRequest_withRequestMissingPackageSize_shouldReturnEmpty() {
    var nodeId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of(nodeId));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(any())).thenReturn(null);
    var request = fakeSyncRequest();
    request.setRequestPackageSize(null);

    Optional<SyncRequest> unpackedRequest = helper.verifyAndUnpackRequest(validMessage(nodeId, request),cacheMock);

    assertFalse("verify that response is empty", unpackedRequest.isPresent());
  }

  @Test
  public void testVerifyAndUnpackRequest_withTooLowMissingPackageSize_shouldReturnEmpty() {
    var nodeId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of(nodeId));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(any())).thenReturn(null);
    var request = fakeSyncRequest();
    request.setRequestPackageSize(0L);

    Optional<SyncRequest> unpackedRequest = helper.verifyAndUnpackRequest(validMessage(nodeId, request),cacheMock);

    assertFalse("verify that response is empty", unpackedRequest.isPresent());
  }

  @Test
  public void testVerifyAndUnpackRequest_withRequestMissingStartPosition_shouldReturnEmpty() {
    var nodeId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of(nodeId));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(any())).thenReturn(null);
    var request = fakeSyncRequest();
    request.setStartingPosition(null);

    Optional<SyncRequest> unpackedRequest = helper.verifyAndUnpackRequest(validMessage(nodeId, request),cacheMock);

    assertFalse("verify that response is empty", unpackedRequest.isPresent());
  }

  @Test
  public void testVerifyAndUnpackRequest_witTooLowMissingStartPosition_shouldReturnEmpty() {
    var nodeId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of(nodeId));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(any())).thenReturn(null);
    var request = fakeSyncRequest();
    request.setStartingPosition(0L);

    Optional<SyncRequest> unpackedRequest = helper.verifyAndUnpackRequest(validMessage(nodeId, request),cacheMock);

    assertFalse("verify that response is empty", unpackedRequest.isPresent());
  }


  @Test
  public void testVerifyAndUnpackMessage_withValidMessage_shouldUnpack() {
    var nodeId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of(nodeId));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(any())).thenReturn(null);
    var mockBlock = new TestBlock();
    mockBlock.setHash("12345");

    Optional<TestBlock> unpackedBlock = helper.verifyAndUnpackMessage(validMessage(nodeId, mockBlock), cacheMock, TestBlock.class);

    assertTrue("verify a response is present", unpackedBlock.isPresent());
    assertEquals("verify the block is unpacked correctly", mockBlock.getHash(), unpackedBlock.get().getHash());
  }

  @Test
  public void testVerifyAndUnpackMessage_withValidMessageFromUnknownNode_shouldReturnEmpty() {
    var nodeId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of("groschn1", "groschn2"));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(any())).thenReturn(null);

    Optional<TestBlock> unpackedBlock = helper.verifyAndUnpackMessage(validMessage(nodeId, new TestBlock()), cacheMock, TestBlock.class);

    assertFalse("verify a response is empty", unpackedBlock.isPresent());
  }

  @Test
  public void testVerifyAndUnpackMessage_withValidMessageReceivedTwice_shouldReturnEmpty() {
    var nodeId = UUID.randomUUID().toString();
    var messageId = UUID.randomUUID().toString();
    when(statsMock.activeNodeIds()).thenReturn(ImmutableList.of(nodeId));
    var cacheMock = mock(Cache.class);
    when(cacheMock.getIfPresent(eq(messageId))).thenReturn(messageId);
    var message = validMessage(nodeId, new TestBlock());
    message.setId(messageId);

    Optional<TestBlock> unpackedBlock = helper.verifyAndUnpackMessage(message, cacheMock, TestBlock.class);

    assertFalse("verify a response is empty", unpackedBlock.isPresent());
  }


  @Test
  public void testPackageResponse_shouldPackageCorrectly() {
    SyncResponse<TestBlock> response = new SyncResponse<>();
    response.setLastPosition(123L);
    response.setEntities(ImmutableList.of(new TestBlock()));
    response.setLastPositionReached(true);
    response.setLastPosition(999L);
    response.setNodeId("groschn99");

    var message = helper.packageResponse(response, "groschn1");

    assertNotNull("verify packaged response is not null", message);
    assertEquals("verify correct sender ID", "groschn1", message.getPayload().getSenderId());
    assertNotNull("verify correct payload is valid", compressor.decompress(
        message.getPayload().getEntity().getEntity(),
        message.getPayload().getEntity().getOriginalSize(),
        SyncResponse.class));
  }

  public Message<MessagePayload> validMessage(String id, Hashable entity) {
    Message<MessagePayload> message = new Message<>();
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(1000l);
    MessagePayload txMessage = new MessagePayload();
    txMessage.setEntity(compressor.compress(entity));
    txMessage.setSenderId(id);
    message.setPayload(txMessage);
    return message;
  }

  public SyncRequest fakeSyncRequest() {
    var request = new SyncRequest();
    request.setRequestPackageSize(10L);
    request.setStartingPosition(23L);
    var header1 = new RequestHeader();
    header1.setPosition(23L);
    header1.setHash("hash23");
    request.setWantedHeaders(ImmutableList.of(header1));
    return  request;
  }
}