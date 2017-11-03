/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.storage.es.dao;

import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.skywalking.apm.collector.core.data.Data;
import org.skywalking.apm.collector.storage.base.dao.IPersistenceDAO;
import org.skywalking.apm.collector.core.data.DataDefine;
import org.skywalking.apm.collector.storage.dao.IInstPerformanceDAO;
import org.skywalking.apm.collector.storage.es.base.dao.EsDAO;
import org.skywalking.apm.collector.storage.table.instance.InstPerformanceTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peng-yongsheng
 */
public class InstPerformanceEsDAO extends EsDAO implements IInstPerformanceDAO, IPersistenceDAO<IndexRequestBuilder, UpdateRequestBuilder> {

    private final Logger logger = LoggerFactory.getLogger(InstPerformanceEsDAO.class);

    @Override public Data get(String id, DataDefine dataDefine) {
        GetResponse getResponse = getClient().prepareGet(InstPerformanceTable.TABLE, id).get();
        if (getResponse.isExists()) {
            logger.debug("id: {} is exist", id);
            Data data = dataDefine.build(id);
            Map<String, Object> source = getResponse.getSource();
            data.setDataInteger(0, (Integer)source.get(InstPerformanceTable.COLUMN_APPLICATION_ID));
            data.setDataInteger(1, (Integer)source.get(InstPerformanceTable.COLUMN_INSTANCE_ID));
            data.setDataInteger(2, (Integer)source.get(InstPerformanceTable.COLUMN_CALLS));
            data.setDataLong(0, ((Number)source.get(InstPerformanceTable.COLUMN_COST_TOTAL)).longValue());
            data.setDataLong(1, ((Number)source.get(InstPerformanceTable.COLUMN_TIME_BUCKET)).longValue());
            return data;
        } else {
            return null;
        }
    }

    @Override public IndexRequestBuilder prepareBatchInsert(Data data) {
        Map<String, Object> source = new HashMap<>();
        source.put(InstPerformanceTable.COLUMN_APPLICATION_ID, data.getDataInteger(0));
        source.put(InstPerformanceTable.COLUMN_INSTANCE_ID, data.getDataInteger(1));
        source.put(InstPerformanceTable.COLUMN_CALLS, data.getDataInteger(2));
        source.put(InstPerformanceTable.COLUMN_COST_TOTAL, data.getDataLong(0));
        source.put(InstPerformanceTable.COLUMN_TIME_BUCKET, data.getDataLong(1));

        return getClient().prepareIndex(InstPerformanceTable.TABLE, data.getDataString(0)).setSource(source);
    }

    @Override public UpdateRequestBuilder prepareBatchUpdate(Data data) {
        Map<String, Object> source = new HashMap<>();
        source.put(InstPerformanceTable.COLUMN_APPLICATION_ID, data.getDataInteger(0));
        source.put(InstPerformanceTable.COLUMN_INSTANCE_ID, data.getDataInteger(1));
        source.put(InstPerformanceTable.COLUMN_CALLS, data.getDataInteger(2));
        source.put(InstPerformanceTable.COLUMN_COST_TOTAL, data.getDataLong(0));
        source.put(InstPerformanceTable.COLUMN_TIME_BUCKET, data.getDataLong(1));

        return getClient().prepareUpdate(InstPerformanceTable.TABLE, data.getDataString(0)).setDoc(source);
    }
}
