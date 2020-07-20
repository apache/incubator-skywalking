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

package org.apache.skywalking.apm.agent.core.boot;

/**
 * ConfigInitializationService provides the config class which should host all parameters originally from agent setup.
 * {@link org.apache.skywalking.apm.agent.core.conf.Config} provides the core level config, all plugins could implement
 * this interface to have the same capability about initializing config from agent.config, system properties and system
 * environment variables.
 */
public interface ConfigInitializationService {
    /**
     * @return Config to host parameters, all static fields set based on the config variable name.
     */
    Class config();
}
