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

package org.apache.skywalking.apm.collector.storage.table.alarm;

import org.apache.skywalking.apm.collector.storage.table.CommonMetricTable;

/**
 * @author peng-yongsheng
 */
public class ServiceReferenceAlarmTable extends CommonMetricTable {
    public static final String TABLE = "service_reference_alarm";
    public static final String COLUMN_FRONT_SERVICE_ID = "front_service_id";
    public static final String COLUMN_BEHIND_SERVICE_ID = "behind_service_id";
    public static final String COLUMN_FRONT_INSTANCE_ID = "front_instance_id";
    public static final String COLUMN_BEHIND_INSTANCE_ID = "behind_instance_id";
    public static final String COLUMN_FRONT_APPLICATION_ID = "front_application_id";
    public static final String COLUMN_BEHIND_APPLICATION_ID = "behind_application_id";
    public static final String COLUMN_ALARM_TYPE = "alarm_type";
    public static final String COLUMN_LAST_TIME_BUCKET = "last_time_bucket";
    public static final String COLUMN_ALARM_CONTENT = "alarm_content";
}
