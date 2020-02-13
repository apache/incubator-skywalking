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

package org.apache.skywalking.oap.server.storage.plugin.elasticsearch.base;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.query.sql.Where;
import org.apache.skywalking.oap.server.core.storage.AbstractDAO;
import org.apache.skywalking.oap.server.core.storage.type.StorageDataType;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch.cache.AliasIndexNameCache;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public abstract class EsDAO extends AbstractDAO<ElasticSearchClient> {

    protected AliasIndexNameCache aliasCache;

    public EsDAO(ElasticSearchClient client) {
        super(client);
        this.aliasCache = new AliasIndexNameCache(client);
    }

    protected List<String> filterNotExistIndex(List<String> indexName, String indName) throws IOException {
        ListIterator<String> iter = indexName.listIterator();
        while (iter.hasNext()) {
            //if alias not exist, then delete
            if (!aliasCache.checkIndexExist(iter.next(), indName)) {
                iter.remove();
            }
        }
        return indexName;
    }

    protected final void queryBuild(SearchSourceBuilder sourceBuilder, Where where, long startTB, long endTB) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(Metrics.TIME_BUCKET).gte(startTB).lte(endTB);
        if (where.getKeyValues().isEmpty()) {
            sourceBuilder.query(rangeQueryBuilder);
        } else {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must().add(rangeQueryBuilder);

            where.getKeyValues().forEach(keyValues -> {
                if (keyValues.getValues().size() > 1) {
                    boolQuery.must().add(QueryBuilders.termsQuery(keyValues.getKey(), keyValues.getValues()));
                } else {
                    boolQuery.must().add(QueryBuilders.termQuery(keyValues.getKey(), keyValues.getValues().get(0)));
                }
            });
            sourceBuilder.query(boolQuery);
        }
        sourceBuilder.size(0);
    }

    protected XContentBuilder map2builder(Map<String, Object> objectMap) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
        for (String key : objectMap.keySet()) {
            Object value = objectMap.get(key);
            if (value instanceof StorageDataType) {
                builder.field(key, ((StorageDataType) value).toStorageData());
            } else {
                builder.field(key, value);
            }
        }
        builder.endObject();

        return builder;
    }
}
