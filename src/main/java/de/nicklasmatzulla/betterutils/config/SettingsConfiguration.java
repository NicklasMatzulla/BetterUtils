/*
 * Copyright 2023 Nicklas Matzulla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.nicklasmatzulla.betterutils.config;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SettingsConfiguration {

    @Getter
    private static SettingsConfiguration instance;

    private final YamlFile config;

    @Getter
    private String token;

    @Getter
    private Long supportGuildId;

    @Getter
    private Long featureRequestChannelId;

    public SettingsConfiguration() {
        SettingsConfiguration.instance = this;
        this.config = load();
        init();
    }

    @NotNull
    private YamlFile load() {
        final File configFile = new File("configurations/settings.yml");
        if (!configFile.exists()) {
            try {
                final InputStream configFileInputStream = this.getClass().getClassLoader().getResourceAsStream("configurations/settings.yml");
                assert configFileInputStream != null;
                FileUtils.copyInputStreamToFile(configFileInputStream, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return YamlFile.loadConfiguration(configFile, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        this.token = this.config.getString("token");
        this.supportGuildId = this.config.getLong("supportGuildId");
        this.featureRequestChannelId = this.config.getLong("featureRequestChannelId");
    }

}