package com.arsenarsen.lavaplayerbridge.bindings;

import com.arsenarsen.lavaplayerbridge.libraries.Library;
import com.arsenarsen.lavaplayerbridge.player.Provider;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProvider;

public class BindingDiscordClientImpl {
    public static Library createLibrary(Object o){
        IDiscordClient client = (IDiscordClient) o;
        return new Library(o) {
            @Override
            public void setProvider(String guildId, Provider provider) {
                client.getGuildByID(Long.parseLong(guildId)).getAudioManager().setAudioProvider(new IAudioProvider() {
                    // Here we have an ugly piece of code to make a link between this library and Discord4J
                    @Override
                    public boolean isReady() {
                        return provider.isReady();
                    }

                    @Override
                    public byte[] provide() {
                        return provider.provide();
                    }

                    @Override
                    public AudioEncodingType getAudioEncodingType() {
                        return AudioEncodingType.OPUS;
                    }
                });
            }

            @Override
            public boolean isValidGuild(String guildId) {
                return client.getGuildByID(Long.parseLong(guildId)) != null;
            }
        };
    }
}
