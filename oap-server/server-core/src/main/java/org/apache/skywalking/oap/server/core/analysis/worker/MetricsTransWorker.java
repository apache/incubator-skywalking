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

package org.apache.skywalking.oap.server.core.analysis.worker;

import java.util.Objects;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.worker.AbstractWorker;
import org.apache.skywalking.oap.server.library.module.ModuleDefineHolder;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

/**
 * MetricsTransWorker is transferring the metrics for downsampling. All streaming process metrics are in the minute
 * precision, but at the storage layer, in order to enhance the query performance, metrics could be saved in minute,
 * hour, day and month, including some of them through {@link CoreModuleConfig#getDownsampling()}.
 */
public class MetricsTransWorker extends AbstractWorker<Metrics> {
    private final MetricsPersistentWorker hourPersistenceWorker;
    private final MetricsPersistentWorker dayPersistenceWorker;
    private final MetricsPersistentWorker monthPersistenceWorker;

    private final CounterMetrics aggregationHourCounter;
    private final CounterMetrics aggregationDayCounter;
    private final CounterMetrics aggregationMonthCounter;

    public MetricsTransWorker(ModuleDefineHolder moduleDefineHolder,
                              String modelName,
                              MetricsPersistentWorker hourPersistenceWorker,
                              MetricsPersistentWorker dayPersistenceWorker,
                              MetricsPersistentWorker monthPersistenceWorker) {
        super(moduleDefineHolder);
        this.hourPersistenceWorker = hourPersistenceWorker;
        this.dayPersistenceWorker = dayPersistenceWorker;
        this.monthPersistenceWorker = monthPersistenceWorker;

        MetricsCreator metricsCreator = moduleDefineHolder.find(TelemetryModule.NAME)
                                                          .provider()
                                                          .getService(MetricsCreator.class);
        aggregationHourCounter = metricsCreator.createCounter(
            "metrics_aggregation", "The number of rows in aggregation", new MetricsTag.Keys("metricName", "level",
                                                                                            "dimensionality"
            ), new MetricsTag.Values(modelName, "2", "hour"));
        aggregationDayCounter = metricsCreator.createCounter(
            "metrics_aggregation", "The number of rows in aggregation", new MetricsTag.Keys("metricName", "level",
                                                                                            "dimensionality"
            ), new MetricsTag.Values(modelName, "2", "day"));
        aggregationMonthCounter = metricsCreator.createCounter(
            "metrics_aggregation", "The number of rows in aggregation", new MetricsTag.Keys("metricName", "level",
                                                                                            "dimensionality"
            ), new MetricsTag.Values(modelName, "2", "month"));
    }

    /**
     * Use the {@link Metrics#toHour()}, {@link Metrics#toDay()} and {@link Metrics#toMonth()} to clone a new metrics
     * instance then process the downsampling. Then forward the data to different works of different precisions for
     * another round aggregation/merging.
     */
    @Override
    public void in(Metrics metrics) {
        if (Objects.nonNull(hourPersistenceWorker)) {
            aggregationMonthCounter.inc();
            hourPersistenceWorker.in(metrics.toHour());
        }
        if (Objects.nonNull(dayPersistenceWorker)) {
            aggregationDayCounter.inc();
            dayPersistenceWorker.in(metrics.toDay());
        }
        if (Objects.nonNull(monthPersistenceWorker)) {
            aggregationHourCounter.inc();
            monthPersistenceWorker.in(metrics.toMonth());
        }
    }
}
