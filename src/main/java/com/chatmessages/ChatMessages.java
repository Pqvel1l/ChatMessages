package com.chatmessages;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatMessages implements ModInitializer {
	private static final String CONFIG_PATH = "config/ChatMessages.json";
	private List<String> messages;
	private int interval;

	@Override
	public void onInitialize() {
		loadConfig();
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
	}

	private void loadConfig() {
		try {
			InputStreamReader reader = new InputStreamReader(Files.newInputStream(Paths.get(CONFIG_PATH)));
			JsonObject config = new Gson().fromJson(reader, JsonObject.class);
			messages = new Gson().fromJson(config.get("messages"), List.class);
			interval = config.get("interval").getAsInt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void onServerStart(MinecraftServer server) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendMessages(server);
			}
		}, 0, interval * 1000L);
	}

	private void sendMessages(MinecraftServer server) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			PlaceholderContext context = PlaceholderContext.of(player);
			for (String message : messages) {
				// Используем Placeholders для замены плейсхолдеров
				Text formattedMessage = Placeholders.parseText(Text.literal(message), context);
				player.sendMessage(formattedMessage, false);
			}
		}
	}
}
