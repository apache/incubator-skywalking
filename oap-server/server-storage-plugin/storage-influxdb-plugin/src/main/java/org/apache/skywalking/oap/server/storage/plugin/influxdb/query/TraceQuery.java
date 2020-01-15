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

package org.apache.skywalking.oap.server.storage.plugin.influxdb.query;

import com.google.common.collect.Lists;
import org.apache.skywalking.oap.server.core.analysis.Downsampling;
import org.apache.skywalking.oap.server.core.analysis.manual.segment.SegmentRecord;
import org.apache.skywalking.oap.server.core.query.entity.*;
import org.apache.skywalking.oap.server.core.storage.query.ITraceQueryDAO;
import org.apache.skywalking.oap.server.library.util.BooleanUtils;
import org.apache.skywalking.oap.server.storage.plugin.influxdb.InfluxClient;
import org.elasticsearch.common.Strings;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.querybuilder.SelectQueryImpl;
import org.influxdb.querybuilder.WhereQueryImpl;
import org.influxdb.querybuilder.clauses.Clause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.apache.skywalking.oap.server.storage.plugin.influxdb.InfluxClient.timeInterval;
import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.*;

public class TraceQuery implements ITraceQueryDAO {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final InfluxClient client;

    public TraceQuery(InfluxClient client) {
        this.client = client;
    }

    @Override
    public TraceBrief queryBasicTraces(long startSecondTB, long endSecondTB, long minDuration, long maxDuration,
                                       String endpointName, int serviceId, int serviceInstanceId, int endpointId,
                                       String traceId, int limit, int from, TraceState traceState, QueryOrder queryOrder)
            throws IOException {

        String orderBy = SegmentRecord.START_TIME;
        if (queryOrder == QueryOrder.BY_DURATION) {
            orderBy = SegmentRecord.LATENCY;
        }

        WhereQueryImpl<SelectQueryImpl> query1 = select()
                .function("top", orderBy, limit + from)
                .column(SegmentRecord.SEGMENT_ID)
                .column(SegmentRecord.START_TIME)
                .column(SegmentRecord.ENDPOINT_NAME)
                .column(SegmentRecord.LATENCY)
                .column(SegmentRecord.IS_ERROR)
                .column(SegmentRecord.TRACE_ID)
                .from(client.getDatabase(), SegmentRecord.INDEX_NAME)
                .where();

        if (startSecondTB != 0 && endSecondTB != 0) {
            query1.and(gte(InfluxClient.TIME, timeInterval(startSecondTB, Downsampling.Second)))
                    .and(lte(InfluxClient.TIME, timeInterval(endSecondTB, Downsampling.Second)));
        }
        if (minDuration != 0) {
            query1.and(gte(SegmentRecord.LATENCY, minDuration));
        }
        if (maxDuration != 0) {
            query1.and(lte(SegmentRecord.LATENCY, maxDuration));
        }
        if (!Strings.isNullOrEmpty(endpointName)) {
            query1.and(regex(SegmentRecord.ENDPOINT_NAME, "/" + endpointName.replaceAll("/", "\\\\/") + "/"));
        }
        if (serviceId != 0) {
            query1.and(eq(SegmentRecord.SERVICE_ID, serviceId));
        }
        if (serviceInstanceId != 0) {
            query1.and(eq(SegmentRecord.SERVICE_INSTANCE_ID, serviceInstanceId));
        }
        if (endpointId != 0) {
            query1.and(eq(SegmentRecord.ENDPOINT_ID, endpointId));
        }
        if (!Strings.isNullOrEmpty(traceId)) {
            query1.and(eq(SegmentRecord.TRACE_ID, traceId));
        }
        switch (traceState) {
            case ERROR:
                query1.and(eq(SegmentRecord.IS_ERROR, BooleanUtils.TRUE));
                break;
            case SUCCESS:
                query1.and(eq(SegmentRecord.IS_ERROR, BooleanUtils.FALSE));
                break;
        }

        WhereQueryImpl<SelectQueryImpl> query2 = select()
                .count(SegmentRecord.ENDPOINT_ID)
                .from(client.getDatabase(), SegmentRecord.INDEX_NAME)
                .where();
        for (Clause clause : query1.getClauses()) {
            query2.where(clause);
        }
        Query query = new Query(query2.getCommand() + query1.getCommand());

        List<QueryResult.Result> results = client.query(query);
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL: {} \nresult set: {}", query.getCommand(), results);
        }
        if (results.size() != 2) {
            throw new IOException("We expect to get 2 Results, but it is " + results.size());
        }
        List<QueryResult.Series> counter = results.get(0).getSeries();
        List<QueryResult.Series> result = results.get(1).getSeries();
        if (result == null || result.isEmpty()) {
            return new TraceBrief();
        }

        TraceBrief traceBrief = new TraceBrief();
        traceBrief.setTotal(((Number) counter.get(0).getValues().get(0).get(1)).intValue());

        result.get(0).getValues().stream().sorted((a, b) -> {
            return Long.compare(((Number) b.get(1)).longValue(), ((Number) a.get(1)).longValue());
        }).skip(from).forEach(values -> {
            BasicTrace basicTrace = new BasicTrace();

            basicTrace.setSegmentId((String) values.get(2));
            basicTrace.setStart(String.valueOf((long) values.get(3)));
            basicTrace.getEndpointNames().add((String) values.get(4));
            basicTrace.setDuration((int) values.get(5));
            basicTrace.setError(BooleanUtils.valueToBoolean((int) values.get(6)));
            basicTrace.getTraceIds().add((String) values.get(7));

            traceBrief.getTraces().add(basicTrace);
        });
        return traceBrief;
    }

    @Override
    public List<SegmentRecord> queryByTraceId(String traceId) throws IOException {
        WhereQueryImpl query = select().column(SegmentRecord.SEGMENT_ID)
                .column(SegmentRecord.TRACE_ID)
                .column(SegmentRecord.SERVICE_ID)
                .column(SegmentRecord.ENDPOINT_NAME)
                .column(SegmentRecord.START_TIME)
                .column(SegmentRecord.END_TIME)
                .column(SegmentRecord.LATENCY)
                .column(SegmentRecord.IS_ERROR)
                .column(SegmentRecord.DATA_BINARY)
                .column(SegmentRecord.VERSION)
                .from(client.getDatabase(), SegmentRecord.INDEX_NAME)
                .where()
                .and(eq(SegmentRecord.TRACE_ID, traceId));
        List<QueryResult.Series> series = client.queryForSeries(query);
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL: {} \nresult set: {}", query.getCommand(), series);
        }
        if (series == null || series.isEmpty()) {
            return Collections.emptyList();
        }
        List<SegmentRecord> segmentRecords = Lists.newArrayList();
        series.get(0).getValues().forEach(values -> {
            SegmentRecord segmentRecord = new SegmentRecord();

            segmentRecord.setSegmentId((String) values.get(1));
            segmentRecord.setTraceId((String) values.get(2));
            segmentRecord.setServiceId((int) values.get(3));
            segmentRecord.setEndpointName((String) values.get(4));
            segmentRecord.setStartTime((long) values.get(5));
            segmentRecord.setEndTime((long) values.get(6));
            segmentRecord.setLatency((int) values.get(7));
            segmentRecord.setIsError((int) values.get(8));
            segmentRecord.setVersion((int) values.get(10));

            String base64 = (String) values.get(9);
            if (!Strings.isNullOrEmpty(base64)) {
                segmentRecord.setDataBinary(Base64.getDecoder().decode(base64));
            }

            segmentRecords.add(segmentRecord);
        });

        return segmentRecords;
    }

    @Override
    public List<Span> doFlexibleTraceQuery(String traceId) throws IOException {
        return Collections.emptyList();
    }
}
