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

package org.apache.skywalking.apm.collector.analysis.metric.define.graph;

/**
 * @author peng-yongsheng
 */
public class MetricWorkerIdDefine {
    public static final int SERVICE_REFERENCE_MINUTE_METRIC_AGGREGATION_WORKER_ID = 4100;
    public static final int SERVICE_REFERENCE_MINUTE_METRIC_REMOTE_WORKER_ID = 4101;
    public static final int SERVICE_REFERENCE_MINUTE_METRIC_PERSISTENCE_WORKER_ID = 4102;
    public static final int SERVICE_REFERENCE_HOUR_METRIC_PERSISTENCE_WORKER_ID = 4103;
    public static final int SERVICE_REFERENCE_HOUR_METRIC_TRANSFORM_NODE_ID = 4104;
    public static final int SERVICE_REFERENCE_DAY_METRIC_PERSISTENCE_WORKER_ID = 4105;
    public static final int SERVICE_REFERENCE_DAY_METRIC_TRANSFORM_NODE_ID = 4106;
    public static final int SERVICE_REFERENCE_MONTH_METRIC_PERSISTENCE_WORKER_ID = 4107;
    public static final int SERVICE_REFERENCE_MONTH_METRIC_TRANSFORM_NODE_ID = 4108;

    public static final int INSTANCE_REFERENCE_MINUTE_METRIC_AGGREGATION_WORKER_ID = 4200;
    public static final int INSTANCE_REFERENCE_MINUTE_METRIC_REMOTE_WORKER_ID = 4201;
    public static final int INSTANCE_REFERENCE_MINUTE_METRIC_PERSISTENCE_WORKER_ID = 4202;
    public static final int INSTANCE_REFERENCE_HOUR_METRIC_PERSISTENCE_WORKER_ID = 4203;
    public static final int INSTANCE_REFERENCE_HOUR_METRIC_TRANSFORM_NODE_ID = 4204;
    public static final int INSTANCE_REFERENCE_DAY_METRIC_PERSISTENCE_WORKER_ID = 4205;
    public static final int INSTANCE_REFERENCE_DAY_METRIC_TRANSFORM_NODE_ID = 4206;
    public static final int INSTANCE_REFERENCE_MONTH_METRIC_PERSISTENCE_WORKER_ID = 4207;
    public static final int INSTANCE_REFERENCE_MONTH_METRIC_TRANSFORM_NODE_ID = 4208;

    public static final int APPLICATION_REFERENCE_MINUTE_METRIC_AGGREGATION_WORKER_ID = 4300;
    public static final int APPLICATION_REFERENCE_MINUTE_METRIC_REMOTE_WORKER_ID = 4301;
    public static final int APPLICATION_REFERENCE_MINUTE_METRIC_PERSISTENCE_WORKER_ID = 4302;
    public static final int APPLICATION_REFERENCE_HOUR_METRIC_PERSISTENCE_WORKER_ID = 4303;
    public static final int APPLICATION_REFERENCE_HOUR_METRIC_TRANSFORM_NODE_ID = 4304;
    public static final int APPLICATION_REFERENCE_DAY_METRIC_PERSISTENCE_WORKER_ID = 4305;
    public static final int APPLICATION_REFERENCE_DAY_METRIC_TRANSFORM_NODE_ID = 4306;
    public static final int APPLICATION_REFERENCE_MONTH_METRIC_PERSISTENCE_WORKER_ID = 4307;
    public static final int APPLICATION_REFERENCE_MONTH_METRIC_TRANSFORM_NODE_ID = 4308;

    public static final int SERVICE_MINUTE_METRIC_AGGREGATION_WORKER_ID = 4400;
    public static final int SERVICE_MINUTE_METRIC_REMOTE_WORKER_ID = 4401;
    public static final int SERVICE_MINUTE_METRIC_PERSISTENCE_WORKER_ID = 4402;
    public static final int SERVICE_HOUR_METRIC_PERSISTENCE_WORKER_ID = 4403;
    public static final int SERVICE_HOUR_METRIC_TRANSFORM_NODE_ID = 4404;
    public static final int SERVICE_DAY_METRIC_PERSISTENCE_WORKER_ID = 4405;
    public static final int SERVICE_DAY_METRIC_TRANSFORM_NODE_ID = 4406;
    public static final int SERVICE_MONTH_METRIC_PERSISTENCE_WORKER_ID = 4407;
    public static final int SERVICE_MONTH_METRIC_TRANSFORM_NODE_ID = 4408;

    public static final int INSTANCE_MINUTE_METRIC_AGGREGATION_WORKER_ID = 4500;
    public static final int INSTANCE_MINUTE_METRIC_REMOTE_WORKER_ID = 4501;
    public static final int INSTANCE_MINUTE_METRIC_PERSISTENCE_WORKER_ID = 4502;
    public static final int INSTANCE_HOUR_METRIC_PERSISTENCE_WORKER_ID = 4503;
    public static final int INSTANCE_HOUR_METRIC_TRANSFORM_NODE_ID = 4504;
    public static final int INSTANCE_DAY_METRIC_PERSISTENCE_WORKER_ID = 4505;
    public static final int INSTANCE_DAY_METRIC_TRANSFORM_NODE_ID = 4506;
    public static final int INSTANCE_MONTH_METRIC_PERSISTENCE_WORKER_ID = 4507;
    public static final int INSTANCE_MONTH_METRIC_TRANSFORM_NODE_ID = 4508;

    public static final int APPLICATION_MINUTE_METRIC_AGGREGATION_WORKER_ID = 4600;
    public static final int APPLICATION_MINUTE_METRIC_REMOTE_WORKER_ID = 4601;
    public static final int APPLICATION_MINUTE_METRIC_PERSISTENCE_WORKER_ID = 4602;
    public static final int APPLICATION_HOUR_METRIC_PERSISTENCE_WORKER_ID = 4603;
    public static final int APPLICATION_HOUR_METRIC_TRANSFORM_NODE_ID = 4604;
    public static final int APPLICATION_DAY_METRIC_PERSISTENCE_WORKER_ID = 4605;
    public static final int APPLICATION_DAY_METRIC_TRANSFORM_NODE_ID = 4606;
    public static final int APPLICATION_MONTH_METRIC_PERSISTENCE_WORKER_ID = 4607;
    public static final int APPLICATION_MONTH_METRIC_TRANSFORM_NODE_ID = 4608;

    public static final int INSTANCE_MINUTE_MAPPING_AGGREGATION_WORKER_ID = 4700;
    public static final int INSTANCE_MINUTE_MAPPING_REMOTE_WORKER_ID = 4701;
    public static final int INSTANCE_MINUTE_MAPPING_PERSISTENCE_WORKER_ID = 4702;
    public static final int INSTANCE_HOUR_MAPPING_PERSISTENCE_WORKER_ID = 4703;
    public static final int INSTANCE_HOUR_MAPPING_TRANSFORM_NODE_ID = 4704;
    public static final int INSTANCE_DAY_MAPPING_PERSISTENCE_WORKER_ID = 4705;
    public static final int INSTANCE_DAY_MAPPING_TRANSFORM_NODE_ID = 4706;
    public static final int INSTANCE_MONTH_MAPPING_PERSISTENCE_WORKER_ID = 4707;
    public static final int INSTANCE_MONTH_MAPPING_TRANSFORM_NODE_ID = 4708;

    public static final int APPLICATION_COMPONENT_AGGREGATION_WORKER_ID = 418;
    public static final int APPLICATION_COMPONENT_REMOTE_WORKER_ID = 419;
    public static final int APPLICATION_COMPONENT_PERSISTENCE_WORKER_ID = 420;

    public static final int APPLICATION_MAPPING_AGGREGATION_WORKER_ID = 421;
    public static final int APPLICATION_MAPPING_REMOTE_WORKER_ID = 422;
    public static final int APPLICATION_MAPPING_PERSISTENCE_WORKER_ID = 423;

    public static final int SERVICE_ENTRY_AGGREGATION_WORKER_ID = 424;
    public static final int SERVICE_ENTRY_REMOTE_WORKER_ID = 425;
    public static final int SERVICE_ENTRY_PERSISTENCE_WORKER_ID = 426;

    public static final int GLOBAL_TRACE_PERSISTENCE_WORKER_ID = 427;
    public static final int SEGMENT_COST_PERSISTENCE_WORKER_ID = 428;

    public static final int INSTANCE_REFERENCE_GRAPH_BRIDGE_WORKER_ID = 429;
    public static final int APPLICATION_REFERENCE_GRAPH_BRIDGE_WORKER_ID = 430;
    public static final int SERVICE_METRIC_GRAPH_BRIDGE_WORKER_ID = 431;
    public static final int INSTANCE_METRIC_GRAPH_BRIDGE_WORKER_ID = 432;
    public static final int APPLICATION_METRIC_GRAPH_BRIDGE_WORKER_ID = 433;

}
