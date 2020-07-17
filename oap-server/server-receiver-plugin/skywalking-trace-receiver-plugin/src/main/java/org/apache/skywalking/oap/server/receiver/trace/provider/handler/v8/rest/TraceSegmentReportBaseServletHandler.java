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

package org.apache.skywalking.oap.server.receiver.trace.provider.handler.v8.rest;

import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.network.language.agent.v3.SegmentObject;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.server.jetty.JettyJsonHandler;
import org.apache.skywalking.oap.server.receiver.trace.TraceServiceModuleConfig;
import org.apache.skywalking.oap.server.receiver.trace.parser.SegmentParserListenerManager;
import org.apache.skywalking.oap.server.receiver.trace.parser.TraceAnalyzer;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

@Slf4j
public abstract class TraceSegmentReportBaseServletHandler extends JettyJsonHandler {

    private final ModuleManager moduleManager;
    private final SegmentParserListenerManager listenerManager;
    private final TraceServiceModuleConfig config;
    private HistogramMetrics histogram;
    private CounterMetrics errorCounter;

    public TraceSegmentReportBaseServletHandler(ModuleManager moduleManager,
                                                SegmentParserListenerManager listenerManager,
                                                TraceServiceModuleConfig config) {
        this.moduleManager = moduleManager;
        this.listenerManager = listenerManager;
        this.config = config;
        MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME)
                                                     .provider()
                                                     .getService(MetricsCreator.class);
        histogram = metricsCreator.createHistogramMetric(
            "trace_in_latency", "The process latency of trace data",
            new MetricsTag.Keys("protocol"), new MetricsTag.Values("http")
        );
        errorCounter = metricsCreator.createCounter("trace_analysis_error_count", "The error number of trace analysis",
                                                    new MetricsTag.Keys("protocol"), new MetricsTag.Values("http")
        );
    }

    @Override
    protected JsonElement doGet(HttpServletRequest req) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected JsonElement doPost(HttpServletRequest req) {
        if (log.isDebugEnabled()) {
            log.debug("receive stream segment");
        }
        HistogramMetrics.Timer timer = histogram.createTimer();

        try {
            final List<SegmentObject> segments = parseSegments(req);

            for (SegmentObject segment : segments) {
                final TraceAnalyzer traceAnalyzer = new TraceAnalyzer(moduleManager, listenerManager, config);
                traceAnalyzer.doAnalysis(segment);
            }
        } catch (Exception e) {
            errorCounter.inc();
            log.error(e.getMessage(), e);
        } finally {
            timer.finish();
        }

        return null;
    }

    /**
     * parsing segment list from request
     */
    protected abstract List<SegmentObject> parseSegments(HttpServletRequest request) throws IOException;

}
