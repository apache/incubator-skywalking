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

package org.apache.skywalking.oap.server.storage.plugin.influxdb.base;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.storage.IMetricsDAO;
import org.apache.skywalking.oap.server.core.storage.StorageBuilder;
import org.apache.skywalking.oap.server.core.storage.model.Model;
import org.apache.skywalking.oap.server.core.storage.type.StorageDataComplexObject;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;
import org.apache.skywalking.oap.server.library.client.request.UpdateRequest;
import org.apache.skywalking.oap.server.storage.plugin.influxdb.InfluxClient;
import org.apache.skywalking.oap.server.storage.plugin.influxdb.InfluxConstants;
import org.apache.skywalking.oap.server.storage.plugin.influxdb.TableMetaInfo;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.querybuilder.SelectQueryImpl;
import org.influxdb.querybuilder.WhereQueryImpl;

import static org.apache.skywalking.oap.server.storage.plugin.influxdb.InfluxConstants.ALL_FIELDS;
import static org.apache.skywalking.oap.server.storage.plugin.influxdb.InfluxConstants.ID_COLUMN;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.contains;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.eq;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.select;

@Slf4j
public class MetricsDAO implements IMetricsDAO {

    private final StorageBuilder<Metrics> storageBuilder;
    private final InfluxClient client;

    public MetricsDAO(InfluxClient client, StorageBuilder<Metrics> storageBuilder) {
        this.client = client;
        this.storageBuilder = storageBuilder;
    }

    @Override
    public List<Metrics> multiGet(Model model, List<String> ids) throws IOException {
        final StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            String[] keys = id.split(Const.ID_CONNECTOR, 2);
            select().raw(ALL_FIELDS)
                    .from(client.getDatabase(), model.getName())
                    .where(eq(InfluxConstants.TagName.TIME_BUCKET, keys[0]))
                    .and(eq(InfluxConstants.TagName.ENTITY_ID, keys[1]))
                    .and(eq(ID_COLUMN, id))
                    .buildQueryString(builder);
            builder.append(";");
        }

        QueryResult.Series series = client.queryForSingleSeries(new Query(builder.toString()));
        if (log.isDebugEnabled()) {
            log.debug("SQL: {} result: {}", builder.toString(), series);
        }

        if (series == null) {
            return Collections.emptyList();
        }

        final List<Metrics> metrics = Lists.newArrayList();
        final List<String> columns = series.getColumns();

        final TableMetaInfo metaInfo = TableMetaInfo.get(model.getName());
        final Map<String, String> storageAndColumnMap = metaInfo.getStorageAndColumnMap();

        series.getValues().forEach(values -> {
            Map<String, Object> data = Maps.newHashMap();

            for (int i = 1; i < columns.size(); i++) {
                Object value = values.get(i);
                if (value instanceof StorageDataComplexObject) {
                    value = ((StorageDataComplexObject) value).toStorageData();
                }

                data.put(storageAndColumnMap.get(columns.get(i)), value);
            }
            metrics.add(storageBuilder.map2Data(data));

        });

        return metrics;
    }

    @Override
    public InsertRequest prepareBatchInsert(Model model, Metrics metrics) {
        final long timestamp = TimeBucket.getTimestamp(metrics.getTimeBucket(), model.getDownsampling());
        final TableMetaInfo tableMetaInfo = TableMetaInfo.get(model.getName());

        final InfluxInsertRequest request = new InfluxInsertRequest(model, metrics, storageBuilder)
            .time(timestamp, TimeUnit.MILLISECONDS);

        tableMetaInfo.getStorageAndTagMap().forEach(request::addFieldAsTag);
        return request;
    }

    @Override
    public UpdateRequest prepareBatchUpdate(Model model, Metrics metrics) {
        return (UpdateRequest) this.prepareBatchInsert(model, metrics);
    }
}
