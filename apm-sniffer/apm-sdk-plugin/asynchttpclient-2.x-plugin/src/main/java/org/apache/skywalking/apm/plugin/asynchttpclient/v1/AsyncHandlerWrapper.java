/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.apm.plugin.asynchttpclient.v1;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import java.net.InetSocketAddress;
import java.util.List;
import javax.net.ssl.SSLSession;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.asynchttpclient.AsyncCompletionHandlerBase;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.netty.request.NettyRequest;

/**
 * {@link AsyncHandlerWrapper} wrapper the {@link AsyncHandler} object for tracing.
 * if userAsyncHandler is null, we will set {@link AsyncCompletionHandlerBase} to avoid NPE.
 */
public class AsyncHandlerWrapper implements AsyncHandler {

    private final AsyncHandler userAsyncHandler;
    private final AbstractSpan asyncSpan;

    public AsyncHandlerWrapper(AsyncHandler asyncHandler, AbstractSpan span) {
        this.userAsyncHandler = asyncHandler == null ? new AsyncCompletionHandlerBase() : asyncHandler;
        this.asyncSpan = span;
    }

    @Override
    public State onStatusReceived(final HttpResponseStatus httpResponseStatus) throws Exception {
        return userAsyncHandler.onStatusReceived(httpResponseStatus);
    }

    @Override
    public State onHeadersReceived(final HttpHeaders httpHeaders) throws Exception {
        return userAsyncHandler.onHeadersReceived(httpHeaders);
    }

    @Override
    public State onBodyPartReceived(final HttpResponseBodyPart httpResponseBodyPart) throws Exception {
        return userAsyncHandler.onBodyPartReceived(httpResponseBodyPart);
    }

    @Override
    public State onTrailingHeadersReceived(final HttpHeaders headers) throws Exception {
        return userAsyncHandler.onTrailingHeadersReceived(headers);
    }

    @Override
    public void onThrowable(final Throwable throwable) {
        asyncSpan.log(throwable);
        asyncSpan.asyncFinish();
        userAsyncHandler.onThrowable(throwable);
    }

    @Override
    public Object onCompleted() throws Exception {
        asyncSpan.asyncFinish();
        return userAsyncHandler.onCompleted();
    }

    @Override
    public void onHostnameResolutionAttempt(final String name) {
        userAsyncHandler.onHostnameResolutionAttempt(name);
    }

    @Override
    public void onHostnameResolutionFailure(final String name, final Throwable cause) {
        userAsyncHandler.onHostnameResolutionFailure(name, cause);
    }

    @Override
    public void onTcpConnectAttempt(final InetSocketAddress remoteAddress) {
        userAsyncHandler.onTcpConnectAttempt(remoteAddress);
    }

    @Override
    public void onTcpConnectSuccess(final InetSocketAddress remoteAddress, final Channel connection) {
        userAsyncHandler.onTcpConnectSuccess(remoteAddress, connection);
    }

    @Override
    public void onTcpConnectFailure(final InetSocketAddress remoteAddress, final Throwable cause) {
        userAsyncHandler.onTcpConnectFailure(remoteAddress, cause);
    }

    @Override
    public void onTlsHandshakeAttempt() {
        userAsyncHandler.onTlsHandshakeAttempt();
    }

    @Override
    public void onTlsHandshakeSuccess(final SSLSession sslSession) {
        userAsyncHandler.onTlsHandshakeSuccess(sslSession);
    }

    @Override
    public void onTlsHandshakeFailure(final Throwable cause) {
        userAsyncHandler.onTlsHandshakeFailure(cause);
    }

    @Override
    public void onConnectionPoolAttempt() {
        userAsyncHandler.onConnectionPoolAttempt();
    }

    @Override
    public void onConnectionPooled(final Channel connection) {
        userAsyncHandler.onConnectionPooled(connection);
    }

    @Override
    public void onConnectionOffer(final Channel connection) {
        userAsyncHandler.onConnectionOffer(connection);
    }

    @Override
    public void onRequestSend(final NettyRequest request) {
        userAsyncHandler.onRequestSend(request);
    }

    @Override
    public void onRetry() {
        userAsyncHandler.onRetry();
    }

    @Override
    public void onHostnameResolutionSuccess(final String name, final List list) {
        userAsyncHandler.onHostnameResolutionSuccess(name, list);
    }
}
