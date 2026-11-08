package com.chinesehat.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ChineseHatClient implements ClientModInitializer {
	public static final String MOD_ID = "chinesehat";
	/** 16x16 white texture; the hat colours come from vertex colours. */
	public static final Identifier WHITE = Identifier.fromNamespaceAndPath(MOD_ID, "textures/white.png");

	private static boolean openRequested = false;

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void onInitializeClient() {
		HatConfig.load();

		// 1) put the hat layer on every player renderer (normal + slim arms)
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, helper, context) -> {
			if (entityRenderer instanceof AvatarRenderer) {
				helper.register(new HatLayer((AvatarRenderer) entityRenderer));
			}
		});

		// 2) /hat  (opens the menu), /hat toggle, /hat reset
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("hat")
						.executes(ctx -> {
							openRequested = true;
							return 1;
						})
						.then(ClientCommandManager.literal("toggle").executes(ctx -> {
							HatConfig.INSTANCE.enabled = !HatConfig.INSTANCE.enabled;
							HatConfig.save();
							ctx.getSource().sendFeedback(Component.literal(
									"Chinese hat " + (HatConfig.INSTANCE.enabled ? "enabled" : "disabled")));
							return 1;
						}))
						.then(ClientCommandManager.literal("reset").executes(ctx -> {
							HatConfig.reset();
							ctx.getSource().sendFeedback(Component.literal("Chinese hat settings reset"));
							return 1;
						}))));

		// 3) open the screen one tick later, otherwise the closing chat screen would replace it
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (openRequested) {
				openRequested = false;
				client.setScreen(new HatScreen(null));
			}
		});
	}
}
