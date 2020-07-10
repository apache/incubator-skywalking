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

package org.apache.skywalking.oap.server.configuration.configmap;

import com.google.common.base.Strings;
import org.apache.skywalking.oap.server.configuration.api.AbstractConfigurationProvider;
import org.apache.skywalking.oap.server.configuration.api.ConfigWatcherRegister;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;

public class ConfigmapConfigurationProvider extends AbstractConfigurationProvider {

    private final ConfigmapConfigurationSettings settings;

    public ConfigmapConfigurationProvider() {
        this.settings = new ConfigmapConfigurationSettings();
    }

    @Override
    public String name() {
        return "configmap";
    }

    @Override
    public ModuleConfig createConfigBeanIfAbsent() {
        return settings;
    }

    @Override
    protected ConfigWatcherRegister initConfigReader() throws ModuleStartException {

        if (Strings.isNullOrEmpty(settings.getLabelSelector()) || Strings.isNullOrEmpty(settings.getNamespace())) {
            throw new ModuleStartException("the settings of configmap configuration is illegal.");
        }

        ConfigurationConfigmapInformer informer = new ConfigurationConfigmapInformer(settings);
        return new ConfigmapConfigurationWatcherRegister(settings, informer);
    }

}
