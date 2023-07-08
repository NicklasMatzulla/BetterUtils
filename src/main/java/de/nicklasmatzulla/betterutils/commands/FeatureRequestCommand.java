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
import de.nicklasmatzulla.betterutils.config.SettingsConfiguration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;

public class FeatureRequestCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild())
            return;
        if (event.getName().equals("featurerequest")) {
            final TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                    .setPlaceholder("Title of the function")
                    .setMaxLength(128)
                    .setRequired(true)
                    .build();
            final TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Describe what the function should do")
                    .setMinLength(120)
                    .setRequired(true)
                    .build();
            final Modal modal = Modal.create("featurerequest", "Feature request")
                    .addComponents(ActionRow.of(title))
                    .addComponents(ActionRow.of(description))
                    .build();
            event.replyModal(modal).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("featurerequest")) {
            event.deferReply(true).queue();
            final String title = event.getValue("title").getAsString();
            final String description = event.getValue("description").getAsString();

            final Long featureRequestChannelId = SettingsConfiguration.getInstance().getFeatureRequestChannelId();
            final TextChannel featureRequestChannel = BetterUtils.getInstance().getShardManager().getTextChannelById(featureRequestChannelId);

            final MessageEmbed featureRequestEmbed = new EmbedBuilder()
                    .setAuthor("DiscordUtils")
                    .setTitle("Feature request\n" + title)
                    .setDescription(description)
                    .addField("Requested by", event.getUser().getName(), true)
                    .setColor(Color.GREEN)
                    .build();
            featureRequestChannel.sendMessageEmbeds(featureRequestEmbed).queue();

            final MessageEmbed messageEmbed = new EmbedBuilder()
                    .setAuthor("DiscordUtils")
                    .setTitle("Feature request successfully created")
                    .setDescription("Your feature request " + title + " has been successfully submitted.")
                    .setColor(Color.GREEN)
                    .build();
            event.getHook().editOriginal("").setEmbeds(messageEmbed).queue();
        }
    }
}
