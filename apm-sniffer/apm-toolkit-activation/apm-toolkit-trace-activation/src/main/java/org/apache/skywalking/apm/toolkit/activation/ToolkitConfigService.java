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

package org.apache.skywalking.apm.toolkit.activation;

import org.apache.skywalking.apm.agent.core.boot.ConfigInitializationService;

public class ToolkitConfigService implements ConfigInitializationService {
    @Override
    public Class config() {
        return ToolkitConfigService.class;
    }

    public static class Plugin {
        public static class Toolkit {
            /**
             * If true, the fully qualified method name will be used as the operation name instead of the given
             * operation name, default is false.
             */
            public static boolean USE_QUALIFIED_NAME_AS_OPERATION_NAME = false;
        }
    }
}
