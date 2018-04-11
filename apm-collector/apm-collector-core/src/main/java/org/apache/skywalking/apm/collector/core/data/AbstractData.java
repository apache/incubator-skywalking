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

package org.apache.skywalking.apm.collector.core.data;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

import static java.util.Optional.ofNullable;

/**
 * @author peng-yongsheng
 */
public abstract class AbstractData {
    private String[] dataStrings;
    private Long[] dataLongs;
    private Double[] dataDoubles;
    private Integer[] dataIntegers;
    private byte[][] dataBytes;
    private final Column[] stringColumns;
    private final Column[] longColumns;
    private final Column[] doubleColumns;
    private final Column[] integerColumns;
    private final Column[] byteColumns;

    public AbstractData(Column[] stringColumns, Column[] longColumns, Column[] doubleColumns,
        Column[] integerColumns, Column[] byteColumns) {
        this.dataStrings = new String[stringColumns.length];
        this.dataLongs = new Long[longColumns.length];
        this.dataDoubles = new Double[doubleColumns.length];
        this.dataIntegers = new Integer[integerColumns.length];
        this.dataBytes = new byte[byteColumns.length][];
        this.stringColumns = stringColumns;
        this.longColumns = longColumns;
        this.doubleColumns = doubleColumns;
        this.integerColumns = integerColumns;
        this.byteColumns = byteColumns;
    }

    public final int getDataStringsCount() {
        return dataStrings.length;
    }

    public final int getDataLongsCount() {
        return dataLongs.length;
    }

    public final int getDataDoublesCount() {
        return dataDoubles.length;
    }

    public final int getDataIntegersCount() {
        return dataIntegers.length;
    }

    public final int getDataBytesCount() {
        return dataBytes.length;
    }

    public final void setDataString(int position, String value) {
        dataStrings[position] = value;
    }

    public final void setDataLong(int position, Long value) {
        dataLongs[position] = value;
    }

    public final void setDataDouble(int position, Double value) {
        dataDoubles[position] = value;
    }

    public final void setDataInteger(int position, Integer value) {
        dataIntegers[position] = value;
    }

    public final void setDataBytes(int position, byte[] dataBytes) {
        this.dataBytes[position] = dataBytes;
    }

    public final String getDataString(int position) {
        return dataStrings[position];
    }

    public final Long getDataLong(int position) {
        return ofNullable(dataLongs[position]).orElse(0L);
    }

    public final Double getDataDouble(int position) {
        return ofNullable(dataDoubles[position]).orElse(0D);
    }

    public final Integer getDataInteger(int position) {
        return ofNullable(dataIntegers[position]).orElse(0);
    }

    public final byte[] getDataBytes(int position) {
        return dataBytes[position];
    }

    public final void mergeAndFormulaCalculateData(AbstractData newData) {
        mergeData(newData);
        calculateFormula();
    }

    private void mergeData(AbstractData newData) {
        for (int i = 0; i < stringColumns.length; i++) {
            String stringData = stringColumns[i].getMergeOperation().operate(newData.getDataString(i), this.getDataString(i));
            this.dataStrings[i] = stringData;
        }
        for (int i = 0; i < longColumns.length; i++) {
            Long longData = longColumns[i].getMergeOperation().operate(newData.getDataLong(i), this.getDataLong(i));
            this.dataLongs[i] = longData;
        }
        for (int i = 0; i < doubleColumns.length; i++) {
            Double doubleData = doubleColumns[i].getMergeOperation().operate(newData.getDataDouble(i), this.getDataDouble(i));
            this.dataDoubles[i] = doubleData;
        }
        for (int i = 0; i < integerColumns.length; i++) {
            Integer integerData = integerColumns[i].getMergeOperation().operate(newData.getDataInteger(i), this.getDataInteger(i));
            this.dataIntegers[i] = integerData;
        }
        for (int i = 0; i < byteColumns.length; i++) {
            byte[] byteData = byteColumns[i].getMergeOperation().operate(newData.getDataBytes(i), this.getDataBytes(i));
            this.dataBytes[i] = byteData;
        }
    }

    @SuppressWarnings("unchecked")
    private void calculateFormula() {
        this.dataStrings = calculate(stringColumns, String[]::new);
        this.dataLongs = calculate(stringColumns, Long[]::new);
        this.dataDoubles = calculate(stringColumns, Double[]::new);
        this.dataIntegers = calculate(stringColumns, Integer[]::new);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] calculate(Column[] columns, IntFunction<T[]> arrayInitializer) {
        return Arrays.stream(columns)
                .filter(Objects::nonNull)
                .map(t -> (T) t.getFormulaOperation().operate(this))
                .toArray(arrayInitializer);

    }


    @Override public final String toString() {
        StringBuilder dataStr = new StringBuilder();
        dataStr.append("string: [");
        for (String dataString : dataStrings) {
            dataStr.append(dataString).append(",");
        }
        dataStr.append("], longs: [");
        for (Long dataLong : dataLongs) {
            dataStr.append(dataLong).append(",");
        }
        dataStr.append("], double: [");
        for (Double dataDouble : dataDoubles) {
            dataStr.append(dataDouble).append(",");
        }
        dataStr.append("], integer: [");
        for (Integer dataInteger : dataIntegers) {
            dataStr.append(dataInteger).append(",");
        }
        dataStr.append("]");
        return dataStr.toString();
    }
}
