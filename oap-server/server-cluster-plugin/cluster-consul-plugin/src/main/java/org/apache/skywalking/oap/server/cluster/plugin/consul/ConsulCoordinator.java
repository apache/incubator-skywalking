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

package org.apache.skywalking.oap.server.cluster.plugin.consul;

import com.google.common.base.Strings;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.health.ServiceHealth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Setter;
import org.apache.skywalking.oap.server.core.cluster.ClusterNodesQuery;
import org.apache.skywalking.oap.server.core.cluster.ClusterRegister;
import org.apache.skywalking.oap.server.core.cluster.RemoteInstance;
import org.apache.skywalking.oap.server.core.cluster.ServiceQueryException;
import org.apache.skywalking.oap.server.core.cluster.ServiceRegisterException;
import org.apache.skywalking.oap.server.core.remote.client.Address;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.apache.skywalking.oap.server.library.util.HealthCheckUtil;
import org.apache.skywalking.oap.server.telemetry.api.HealthCheckMetrics;

public class ConsulCoordinator implements ClusterRegister, ClusterNodesQuery {

    private final Consul client;
    private final String serviceName;
    private final ClusterModuleConsulConfig config;
    private volatile Address selfAddress;
    @Setter
    private HealthCheckMetrics healthChecker;

    public ConsulCoordinator(ClusterModuleConsulConfig config, Consul client) {
        this.config = config;
        this.client = client;
        this.serviceName = config.getServiceName();
    }

    @Override
    public List<RemoteInstance> queryRemoteNodes() {
        List<RemoteInstance> remoteInstances = new ArrayList<>();
        try {
            HealthClient healthClient = client.healthClient();
            // Discover only "passing" nodes
            List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances(serviceName).getResponse();
            if (CollectionUtils.isNotEmpty(nodes)) {
                nodes.forEach(node -> {
                    if (!Strings.isNullOrEmpty(node.getService().getAddress())) {
                        Address address = new Address(node.getService().getAddress(), node.getService().getPort(), false);
                        if (address.equals(selfAddress)) {
                            address.setSelf(true);
                        }
                        remoteInstances.add(new RemoteInstance(address));
                    }
                });
            }
            if (remoteInstances.size() > 1) {
                Set<String> remoteAddressSet = remoteInstances.stream().map(remoteInstance ->
                        remoteInstance.getAddress().getHost()).collect(Collectors.toSet());
                boolean hasUnHealthAddress = HealthCheckUtil.hasUnHealthAddress(remoteAddressSet);
                if (hasUnHealthAddress) {
                    this.healthChecker.unHealth(new ServiceQueryException("found 127.0.0.1 or localhost in cluster mode"));
                } else {
                    List<RemoteInstance> selfInstances = remoteInstances.stream().
                            filter(remoteInstance -> remoteInstance.getAddress().isSelf()).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(selfInstances) && selfInstances.size() == 1) {
                        this.healthChecker.health();
                    } else {
                        this.healthChecker.unHealth(new ServiceQueryException("can't get self instance or multi self instances"));
                    }
                }
            }
        } catch (Throwable e) {
            healthChecker.unHealth(e);
            throw new ServiceQueryException(e.getMessage());
        }
        return remoteInstances;
    }

    @Override
    public void registerRemote(RemoteInstance remoteInstance) throws ServiceRegisterException {
        if (needUsingInternalAddr()) {
            remoteInstance = new RemoteInstance(new Address(config.getInternalComHost(), config.getInternalComPort(), true));
        }
        try {
            AgentClient agentClient = client.agentClient();

            this.selfAddress = remoteInstance.getAddress();

            Registration registration = ImmutableRegistration.builder()
                    .id(remoteInstance.getAddress().toString())
                    .name(serviceName)
                    .address(remoteInstance.getAddress().getHost())
                    .port(remoteInstance.getAddress().getPort())
                    .check(Registration.RegCheck.grpc(remoteInstance.getAddress()
                            .getHost() + ":" + remoteInstance
                            .getAddress()
                            .getPort(), 5)) // registers with a TTL of 5 seconds
                    .build();

            agentClient.register(registration);
            healthChecker.health();
        } catch (Throwable e) {
            healthChecker.unHealth(e);
            throw new ServiceRegisterException(e.getMessage());
        }
    }

    private boolean needUsingInternalAddr() {
        return !Strings.isNullOrEmpty(config.getInternalComHost()) && config.getInternalComPort() > 0;
    }
}
