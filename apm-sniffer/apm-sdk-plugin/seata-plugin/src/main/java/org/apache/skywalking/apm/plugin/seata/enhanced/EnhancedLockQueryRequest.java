package org.apache.skywalking.apm.plugin.seata.enhanced;

import io.seata.core.protocol.transaction.GlobalLockQueryRequest;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class EnhancedLockQueryRequest extends GlobalLockQueryRequest implements EnhancedRequest {
  private Map<String, String> headers = new HashMap<String, String>();

  public EnhancedLockQueryRequest(final GlobalLockQueryRequest lockQueryRequest) {
    setApplicationData(lockQueryRequest.getApplicationData());
    setBranchType(lockQueryRequest.getBranchType());
    setLockKey(lockQueryRequest.getLockKey());
    setResourceId(lockQueryRequest.getResourceId());
    setXid(lockQueryRequest.getXid());
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(final Map<String, String> headers) {
    this.headers = headers;
  }

  @Override
  public void put(final String key, final String value) {
    headers.put(key, value);
  }

  @Override
  public String get(final String key) {
    return headers.get(key);
  }

  @Override
  public byte[] encode() {
    return EnhancedRequestHelper.encode(super.encode(), getHeaders());
  }

  @Override
  public void decode(final ByteBuffer byteBuffer) {
    super.decode(byteBuffer);
    EnhancedRequestHelper.decode(byteBuffer, getHeaders());
  }
}
