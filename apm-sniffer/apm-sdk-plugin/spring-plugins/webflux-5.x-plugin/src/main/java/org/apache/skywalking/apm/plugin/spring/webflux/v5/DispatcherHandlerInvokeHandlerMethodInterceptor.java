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

package org.apache.skywalking.apm.plugin.spring.webflux.v5;

/**
 * @author zhaoyuguang
 */

import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

public class DispatcherHandlerInvokeHandlerMethodInterceptor implements InstanceMethodsAroundInterceptor {

    private static final String ROUTER_SEARCH = "$$Lambda";
    private static final String HASHTAG = "#";


    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
        EnhancedInstance instance = (EnhancedInstance) allArguments[0];
        AbstractSpan span = (AbstractSpan) instance.getSkyWalkingDynamicField();
        if (span == null) {
            return;
        }
        String handleClassName = allArguments[1].getClass().getSimpleName();
        int index = handleClassName.indexOf(ROUTER_SEARCH);
        if (index != -1) {
            span.setOperationName(handleClassName.substring(0, index));
        } else if (allArguments[1] instanceof HandlerMethod) {
            HandlerMethod handler = (HandlerMethod) allArguments[1];
            span.setOperationName(getHandlerMethodOperationName(handler));
        }
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret) throws Throwable {
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
        EnhancedInstance instance = (EnhancedInstance) allArguments[0];
        AbstractSpan span = (AbstractSpan) instance.getSkyWalkingDynamicField();
        if (span != null) {
            span.errorOccurred().log(t);
        }
    }

    private String getHandlerMethodOperationName(HandlerMethod handler) {
        Method method = handler.getMethod();
        return method.getDeclaringClass().getSimpleName() + HASHTAG + method.getName();
    }
}
