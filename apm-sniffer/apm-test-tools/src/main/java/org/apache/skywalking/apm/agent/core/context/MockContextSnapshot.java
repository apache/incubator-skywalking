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

package org.apache.skywalking.apm.agent.core.context;

import java.util.ArrayList;
import java.util.List;
import org.apache.skywalking.apm.agent.core.context.ids.DistributedTraceId;
import org.apache.skywalking.apm.agent.core.context.ids.NewDistributedTraceId;

public enum MockContextSnapshot {
    INSTANCE;

    private final ContextSnapshot contextSnapshot;

    MockContextSnapshot() {
        List<DistributedTraceId> distributedTraceIds = new ArrayList<DistributedTraceId>();
        distributedTraceIds.add(new NewDistributedTraceId());

        contextSnapshot = new ContextSnapshot(
            "1, 2, 3", 1, new NewDistributedTraceId(), "/for-test-entryOperationName", new CorrelationContext(), new ExtensionContext());
    }

    public ContextSnapshot mockContextSnapshot() {
        return contextSnapshot;
    }
}
