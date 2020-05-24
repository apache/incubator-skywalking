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

package org.apache.skywalking.apm.toolkit.meter;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.util.Arrays;

public class HistogramTest {

    @Test
    public void testBuild() {
        // normal
        Histogram histogram = Histogram.create("test_histogram").steps(Arrays.asList(1, 5, 10)).exceptMinValue(-10)
            .tag("k1", "v1").build();
        Assert.assertEquals(histogram.getTag("k1"), "v1");
        Assert.assertNull(histogram.getTag("k2"));
        Assert.assertEquals(histogram.getName(), "test_histogram");
        Assert.assertEquals(Whitebox.getInternalState(histogram, "steps"), Arrays.asList(-10, 1, 5, 10));

        // except value bigger than first bucket
        try {
            histogram = Histogram.create("test_histogram").steps(Arrays.asList(1, 5, 10)).exceptMinValue(2).build();
            throw new IllegalStateException("valid failed");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }

        // empty step
        try {
            Histogram.create("test").build();
            throw new IllegalStateException();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
