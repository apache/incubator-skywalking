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

package org.apache.skywalking.oap.server.storage.plugin.elasticsearch7.dao;

import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author kezhenxu94
 */
public class StorageEsInstaller extends org.apache.skywalking.oap.server.storage.plugin.elasticsearch.base.StorageEsInstaller {

    private static final Logger logger = LoggerFactory.getLogger(StorageEsInstaller.class);

    public StorageEsInstaller(final ModuleManager moduleManager,
                              final int indexShardsNumber,
                              final int indexReplicasNumber,
                              final int indexRefreshInterval) {
        super(moduleManager, indexShardsNumber, indexReplicasNumber, indexRefreshInterval);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> createMapping(Model model) {
        Map<String, Object> mapping = super.createMapping(model);
        Map<String, Object> type = (Map<String, Object>) mapping.remove(ElasticSearchClient.TYPE);
        mapping.put("properties", type.get("properties"));

        logger.debug("elasticsearch index template setting: {}", mapping.toString());

        return mapping;
    }
}
