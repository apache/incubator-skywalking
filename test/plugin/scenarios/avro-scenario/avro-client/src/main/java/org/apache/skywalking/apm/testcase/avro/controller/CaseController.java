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
 *
 */

package org.apache.skywalking.apm.testcase.avro.controller;

import example.proto.Greeter;
import example.proto.Message;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.generic.GenericRequestor;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/case")
public class CaseController {
    private static final Logger logger = LogManager.getLogger(CaseController.class);

    private static final String SUCCESS = "Success";
    private Greeter httpClient;
    private Greeter nettyClient;
    private GenericRequestor genericRequestor;

    @RequestMapping("/avro-scenario")
    @ResponseBody
    public String testcase() throws IOException {
        nettyClient.hello(new Message("SkyWalker"));
        httpClient.hello(new Message("SkyWalker"));

        testGeneric();
        return SUCCESS;
    }

    public void testGeneric() throws IOException {
        GenericRecord datum = new GenericData.Record(Message.getClassSchema());
        datum.put("name", "SkyWalker");

        Schema schemaRequest = Greeter.PROTOCOL.getMessages().get("hello").getRequest();
        GenericRecord request = new GenericData.Record(schemaRequest);
        request.put("message", datum);

        genericRequestor.request("hello", request);
    }

    @RequestMapping("/healthCheck")
    @ResponseBody
    public String healthCheck() throws IOException {
        try {
            nettyClient = SpecificRequestor.getClient(Greeter.class, new NettyTransceiver(new InetSocketAddress("localhost", 9018)));
            httpClient = SpecificRequestor.getClient(Greeter.class, new HttpTransceiver(new URL("http://localhost:9019")));

            genericRequestor = new GenericRequestor(Greeter.PROTOCOL, new NettyTransceiver(new InetSocketAddress("localhost", 9018)));
        } catch (Exception e) {
            throw e;
        }
        return SUCCESS;
    }

}
