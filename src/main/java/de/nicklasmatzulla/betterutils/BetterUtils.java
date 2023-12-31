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

package de.nicklasmatzulla.betterutils;

import de.nicklasmatzulla.betterutils.commands.AdminCommand;
import de.nicklasmatzulla.betterutils.commands.CopyRoleCommand;
import de.nicklasmatzulla.betterutils.commands.FeatureRequestCommand;
import de.nicklasmatzulla.betterutils.config.SettingsConfiguration;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

public class BetterUtils extends ListenerAdapter {

    @Getter
    private static BetterUtils instance;

    @Getter
    private final ShardManager shardManager;

    public static void main(String[] args) {
        new BetterUtils();
    }

    public BetterUtils() {
        BetterUtils.instance = this;
        this.shardManager = start();
    }

    @NotNull
    public ShardManager start() {
        final SettingsConfiguration settingsConfiguration = new SettingsConfiguration();
        final String token = settingsConfiguration.getToken();
        return DefaultShardManagerBuilder.createLight(token)
                .setAutoReconnect(true)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .addEventListeners(this)
                .addEventListeners(new CopyRoleCommand())
                .addEventListeners(new FeatureRequestCommand())
                .addEventListeners(new AdminCommand())
                .build();
    }

    @Override
    public void onReady(ReadyEvent event) {
        final JDA jda = event.getJDA();
        jda.upsertCommand("copyrole", "Copy a role.")
                .addOption(OptionType.ROLE, "role", "The role that should be copied.", true)
                .addOption(OptionType.STRING, "name", "The name that the new role should be assigned.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true)
                .queue();
        jda.upsertCommand("featurerequest", "Request a feature that should be implemented")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setGuildOnly(true)
                .queue();
        if (this.shardManager.getShardsTotal() == this.shardManager.getShardsRunning()) {
            final SettingsConfiguration settingsConfiguration = SettingsConfiguration.getInstance();
            final Long supportGuildId = settingsConfiguration.getSupportGuildId();
            final Guild supportGuild = this.shardManager.getGuildById(supportGuildId);
            if (supportGuild != null) {
                supportGuild.upsertCommand("admin", "Manage BetterUtils functions")
                        .addOption(OptionType.STRING, "function", "What action do you want to perform?", true, true)
                        .queue();
            }
        }
    }

}
