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

package org.apache.skywalking.apm.toolkit.activation.meter;

import org.apache.skywalking.apm.agent.core.boot.ServiceManager;
import org.apache.skywalking.apm.agent.core.meter.Histogram;
import org.apache.skywalking.apm.agent.core.meter.Meter;
import org.apache.skywalking.apm.agent.core.meter.MeterId;
import org.apache.skywalking.apm.agent.core.meter.MeterRegistryService;
import org.apache.skywalking.apm.agent.core.meter.MeterType;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceConstructorInterceptor;
import org.apache.skywalking.apm.toolkit.activation.meter.util.MeterTagConverter;
import org.apache.skywalking.apm.toolkit.meter.Tag;

import java.util.List;

public class HistogramConstructInterceptor implements InstanceConstructorInterceptor {

    private static MeterRegistryService REGISTRY_SERVICE;

    @Override
    public void onConstruct(EnhancedInstance objInst, Object[] allArguments) {
        final String name = (String) allArguments[0];
        final List<Tag> tags = (List<Tag>) allArguments[1];
        final List<Integer> buckets = (List<Integer>) allArguments[2];

        final MeterId id = new MeterId(name, MeterType.HISTOGRAM, MeterTagConverter.convert(tags));
        final Histogram histogram = new Histogram(id, buckets);

        // register the meter
        if (REGISTRY_SERVICE == null) {
            REGISTRY_SERVICE = ServiceManager.INSTANCE.findService(MeterRegistryService.class);
        }
        final Meter dbMeter = REGISTRY_SERVICE.registerOrFound(histogram);
        objInst.setSkyWalkingDynamicField(dbMeter);
    }

}
