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

package de.nicklasmatzulla.betterutils.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;

public class CopyRoleCommand extends ListenerAdapter {

    @SuppressWarnings({"DataFlowIssue", "SpellCheckingInspection"})
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild())
            return;
        if (event.getName().equals("copyrole")) {
            event.deferReply(true).queue();
            final Role targetRole = event.getOption("role", OptionMapping::getAsRole);
            final String roleName = event.getOption("name", OptionMapping::getAsString);
            final Guild guild = event.getGuild();

            guild.createCopyOfRole(targetRole)
                    .setName(roleName)
                    .queue(createdRole -> {
                        final MessageEmbed messageEmbed = new EmbedBuilder()
                                .setAuthor("DiscordUtils")
                                .setTitle("Role successfully created")
                                .setDescription("The role " + targetRole.getAsMention() + " was successfully copied as " + createdRole.getAsMention() + ".")
                                .setColor(Color.GREEN)
                                .build();
                        event.getHook().editOriginal("").setEmbeds(messageEmbed).queue();
                    });
        }
    }
}
