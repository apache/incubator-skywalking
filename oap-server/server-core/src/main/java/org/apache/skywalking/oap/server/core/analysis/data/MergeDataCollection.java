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

package org.apache.skywalking.oap.server.core.analysis.data;

import java.util.*;
import org.apache.skywalking.oap.server.core.remote.data.StreamData;

/**
 * @author peng-yongsheng
 */
public class MergeDataCollection<STREAM_DATA extends StreamData> implements Collection<Map<STREAM_DATA, STREAM_DATA>> {
    private Map<STREAM_DATA, STREAM_DATA> data;
    private volatile boolean writing;
    private volatile boolean reading;

    MergeDataCollection() {
        this.data = new HashMap<>();
        this.writing = false;
        this.reading = false;
    }

    public void finishWriting() {
        writing = false;
    }

    @Override public void writing() {
        writing = true;
    }

    @Override public boolean isWriting() {
        return writing;
    }

    @Override public void finishReading() {
        reading = false;
    }

    @Override public void reading() {
        reading = true;
    }

    @Override public boolean isReading() {
        return reading;
    }

    boolean containsKey(STREAM_DATA key) {
        return data.containsKey(key);
    }

    void put(STREAM_DATA value) {
        data.put(value, value);
    }

    public STREAM_DATA get(STREAM_DATA key) {
        return data.get(key);
    }

    @Override public int size() {
        return data.size();
    }

    @Override public void clear() {
        data.clear();
    }

    public Map<STREAM_DATA, STREAM_DATA> collection() {
        return data;
    }
}
