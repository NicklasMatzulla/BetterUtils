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

import de.nicklasmatzulla.betterutils.BetterUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminCommand extends ListenerAdapter {

    private static final String[] options = new String[]{"statistics"};

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild())
            return;
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR))
            return;
        if (event.getName().equals("admin")) {
            event.deferReply(true).queue();
            final String function = event.getOption("function", "", OptionMapping::getAsString);
            switch (function) {
                case "statistics" -> {
                    final ShardManager shardManager = BetterUtils.getInstance().getShardManager();
                    final List<Guild> guilds = shardManager.getGuilds();
                    int memberCount = 0;
                    for (final Guild guild : guilds) {
                        memberCount += guild.getMemberCount();
                    }
                    final List<String> sortedGuildList = guilds.stream()
                            .map(guild -> guild.getName() + " (" + guild.getMemberCount() + ")")
                            .sorted(Comparator.comparingInt(guild -> Integer.parseInt(guild.toString().substring(guild.toString().lastIndexOf("(") + 1, guild.toString().lastIndexOf(")")))).reversed())
                            .toList();
                    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        for (String entry : sortedGuildList) {
                            byteArrayOutputStream.write(entry.getBytes(StandardCharsets.UTF_8));
                            byteArrayOutputStream.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    byte[] guildsInfoFileBytes = byteArrayOutputStream.toByteArray();
                    final MessageEmbed messageEmbed = new EmbedBuilder()
                            .setAuthor("DiscordUtils")
                            .setTitle("Server overview")
                            .setDescription("These are BetterUtils' statistics.")
                            .addField("Server count", String.valueOf(guilds.size()), true)
                            .addField("Total members", String.valueOf(memberCount), true)
                            .setColor(Color.WHITE)
                            .build();
                    event.getHook().editOriginal("").setEmbeds(messageEmbed).setFiles(FileUpload.fromData(guildsInfoFileBytes, "guildsInfo.txt")).queue();
                }
                default -> {
                    final MessageEmbed messageEmbed = new EmbedBuilder()
                            .setAuthor("DiscordUtils")
                            .setTitle("Error")
                            .setDescription("The command was not found.")
                            .setColor(Color.RED)
                            .build();
                    event.getHook().editOriginal("").setEmbeds(messageEmbed).queue();
                }
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("admin") && event.getFocusedOption().getName().equals("function")) {
            final List<Command.Choice> options = Stream.of(AdminCommand.options)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                    .map(word -> new Command.Choice(word, word))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }
}
