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

package org.apache.skywalking.apm.collector.storage.es.dao.impp;

import java.util.HashMap;
import java.util.Map;
import org.apache.skywalking.apm.collector.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.apm.collector.storage.es.base.dao.AbstractPersistenceEsDAO;
import org.apache.skywalking.apm.collector.storage.table.instance.InstanceMapping;
import org.apache.skywalking.apm.collector.storage.table.instance.InstanceMappingTable;

/**
 * @author peng-yongsheng
 */
public abstract class AbstractInstanceMappingEsPersistenceDAO extends AbstractPersistenceEsDAO<InstanceMapping> {

    AbstractInstanceMappingEsPersistenceDAO(ElasticSearchClient client) {
        super(client);
    }

    @Override protected final String timeBucketColumnNameForDelete() {
        return InstanceMappingTable.COLUMN_TIME_BUCKET;
    }

    @Override protected final InstanceMapping esDataToStreamData(Map<String, Object> source) {
        InstanceMapping instanceMapping = new InstanceMapping();
        instanceMapping.setMetricId((String)source.get(InstanceMappingTable.COLUMN_METRIC_ID));

        instanceMapping.setApplicationId(((Number)source.get(InstanceMappingTable.COLUMN_APPLICATION_ID)).intValue());
        instanceMapping.setInstanceId(((Number)source.get(InstanceMappingTable.COLUMN_INSTANCE_ID)).intValue());
        instanceMapping.setAddressId(((Number)source.get(InstanceMappingTable.COLUMN_ADDRESS_ID)).intValue());
        instanceMapping.setTimeBucket(((Number)source.get(InstanceMappingTable.COLUMN_TIME_BUCKET)).longValue());
        return instanceMapping;
    }

    @Override protected final Map<String, Object> esStreamDataToEsData(InstanceMapping streamData) {
        Map<String, Object> source = new HashMap<>();
        source.put(InstanceMappingTable.COLUMN_METRIC_ID, streamData.getMetricId());

        source.put(InstanceMappingTable.COLUMN_APPLICATION_ID, streamData.getApplicationId());
        source.put(InstanceMappingTable.COLUMN_INSTANCE_ID, streamData.getInstanceId());
        source.put(InstanceMappingTable.COLUMN_ADDRESS_ID, streamData.getAddressId());
        source.put(InstanceMappingTable.COLUMN_TIME_BUCKET, streamData.getTimeBucket());

        return source;
    }
}
