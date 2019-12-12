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

package org.apache.skywalking.oap.server.storage.plugin.jdbc.mysql;

import org.apache.skywalking.oap.server.core.analysis.Downsampling;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.query.entity.Order;
import org.apache.skywalking.oap.server.core.query.entity.TopNEntity;
import org.apache.skywalking.oap.server.core.storage.model.ModelName;
import org.apache.skywalking.oap.server.library.client.jdbc.hikaricp.JDBCHikariCPClient;
import org.apache.skywalking.oap.server.storage.plugin.jdbc.h2.dao.H2AggregationQueryDAO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wusheng
 */
public class MySQLAggregationQueryDAO extends H2AggregationQueryDAO {

    public MySQLAggregationQueryDAO(
        JDBCHikariCPClient client) {
        super(client);
    }

    @Override
    public List<TopNEntity> topNQuery(String indName, String valueCName, int topN, Downsampling downsampling,
        long startTB, long endTB, Order order, AppendCondition appender) throws IOException {
        String tableName = ModelName.build(downsampling, indName);
        StringBuilder sql = new StringBuilder();
        List<Object> conditions = new ArrayList<>(10);
        sql.append("select avg(").append(valueCName).append(") value,").append(Metrics.ENTITY_ID).append(" from ")
            .append(tableName).append(" where ");
        this.setTimeRangeCondition(sql, conditions, startTB, endTB);
        if (appender != null) {
            appender.append(sql, conditions);
        }
        sql.append(" group by ").append(Metrics.ENTITY_ID);
        sql.append(" order by value ").append(order.equals(Order.ASC) ? "asc" : "desc").append(" limit ").append(topN);

        List<TopNEntity> topNEntities = new ArrayList<>();
        try (Connection connection = getClient().getConnection()) {
            try (ResultSet resultSet = getClient().executeQuery(connection, sql.toString(), conditions.toArray(new Object[0]))) {

                try {
                    while (resultSet.next()) {
                        TopNEntity topNEntity = new TopNEntity();
                        topNEntity.setId(resultSet.getString(Metrics.ENTITY_ID));
                        topNEntity.setValue(resultSet.getLong("value"));
                        topNEntities.add(topNEntity);
                    }
                } catch (SQLException e) {
                    throw new IOException(e);
                }
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return topNEntities;
    }
}
