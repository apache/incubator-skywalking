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

package org.apache.skywalking.apm.agent.core.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.skywalking.apm.agent.core.boot.AgentPackageNotFoundException;
import org.apache.skywalking.apm.agent.core.boot.AgentPackagePath;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.util.ConfigInitializer;
import org.apache.skywalking.apm.util.StringUtil;

/**
 * The <code>SnifferConfigInitializer</code> initializes all configs in several way.
 *
 * @author wusheng
 */
public class SnifferConfigInitializer {
    private static final ILog logger = LogManager.getLogger(SnifferConfigInitializer.class);
    private static String DEFAULT_CONFIG_FILE_NAME = "/config/agent.config";
    private static String ENV_KEY_PREFIX = "skywalking.";
    private static boolean IS_INIT_COMPLETED = false;

    /**
     * @param configFilePath, The configFilePath is the absolute path of your specified agent.config file.
     * if configFilePath is null or empty, The agent will try to locate the default `agent.config`, which should be in the /config dictionary of agent package.
     * <p>
     * Also try to override the config by system.env and system.properties. All the keys in these two places should
     * start with {@link #ENV_KEY_PREFIX}. e.g. in env `skywalking.agent.application_code=yourAppName` to override
     * `agent.application_code` in config file.
     * <p>
     * At the end, `agent.application_code` and `collector.servers` must be not blank.
     */
    public static void initialize(String configFilePath) throws ConfigNotFoundException, AgentPackageNotFoundException {
        InputStreamReader configFileStream;

        try {
            configFileStream = loadConfigFromAgentFolder(configFilePath);
            Properties properties = new Properties();
            properties.load(configFileStream);
            ConfigInitializer.initialize(properties, Config.class);
        } catch (Exception e) {
            logger.error(e, "Failed to read the config file, skywalking is going to run in default config.");
        }

        try {
            overrideConfigBySystemEnv();
        } catch (Exception e) {
            logger.error(e, "Failed to read the system env.");
        }

        if (StringUtil.isEmpty(Config.Agent.APPLICATION_CODE)) {
            throw new ExceptionInInitializerError("`agent.application_code` is missing.");
        }
        if (StringUtil.isEmpty(Config.Collector.SERVERS) && StringUtil.isEmpty(Config.Collector.DIRECT_SERVERS)) {
            throw new ExceptionInInitializerError("`collector.direct_servers` and `collector.servers` cannot be empty at the same time.");
        }

        IS_INIT_COMPLETED = true;
    }

    public static boolean isInitCompleted() {
        return IS_INIT_COMPLETED;
    }

    /**
     * Override the config by system env. The env key must start with `skywalking`, the reuslt should be as same as in
     * `agent.config`
     * <p>
     * such as:
     * Env key of `agent.application_code` shoule be `skywalking.agent.application_code`
     *
     * @return the config file {@link InputStream}, or null if not needEnhance.
     */
    private static void overrideConfigBySystemEnv() throws IllegalAccessException {
        Properties properties = new Properties();
        Properties systemProperties = System.getProperties();
        Iterator<Map.Entry<Object, Object>> entryIterator = systemProperties.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Object, Object> prop = entryIterator.next();
            if (prop.getKey().toString().startsWith(ENV_KEY_PREFIX)) {
                String realKey = prop.getKey().toString().substring(ENV_KEY_PREFIX.length());
                properties.put(realKey, prop.getValue());
            }
        }

        if (!properties.isEmpty()) {
            ConfigInitializer.initialize(properties, Config.class);
        }
    }

    /**
     * @param configFilePath, the absolute path of your specified config file
     *
     * Load your specified config file or default config file
     *
     * @return the config file {@link InputStream}, or null if not needEnhance.
     */
    private static InputStreamReader loadConfigFromAgentFolder(String configFilePath) throws AgentPackageNotFoundException, ConfigNotFoundException, ConfigReadFailedException {

        File configFile = StringUtil.isEmpty(configFilePath) ? new File(AgentPackagePath.getPath(), DEFAULT_CONFIG_FILE_NAME) : new File(configFilePath);

        if (configFile.exists() && configFile.isFile()) {
            try {
                logger.info("Config file found in {}.", configFile);

                return new InputStreamReader(new FileInputStream(configFile), "UTF-8");
            } catch (FileNotFoundException e) {
                throw new ConfigNotFoundException("Fail to load agent.config", e);
            } catch (UnsupportedEncodingException e) {
                throw new ConfigReadFailedException("Fail to load agent.config", e);
            }
        }
        throw new ConfigNotFoundException("Fail to load agent config file.");
    }
}
