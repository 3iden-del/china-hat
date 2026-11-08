package com.chinesehat.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** All user settings. Saved to config/chinesehat.json. */
public final class HatConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static HatConfig INSTANCE = new HatConfig();

	// --- visibility ---
	public boolean enabled = true;
	public boolean showOnSelf = true;
	public boolean showOnOthers = false;

	// --- look ---
	public String style = HatStyle.IMPERIAL_SILK.name();
	public float scale = 1.0f;          // overall size
	public float pointiness = 1.0f;     // cone height multiplier
	public float heightOffset = 0.0f;   // blocks above (+) / below (-) the head
	public float tiltX = 0f;            // degrees, forward / back
	public float tiltZ = 0f;            // degrees, sideways
	public float spinSpeed = 0f;        // degrees per tick
	public float opacity = 1.0f;        // global opacity multiplier
	public int segments = 28;           // roundness of the cone

	// --- options ---
	public boolean followHeadPitch = true;
	public boolean strap = true;
	public boolean knob = true;
	public boolean fullbright = false;
	public float lineWidth = 2.0f;
	public float rainbowSpeed = 1.0f;

	// --- custom colours (RGB) ---
	public boolean customColors = false;
	public int primary = 0xB45CFF;
	public int secondary = 0xFFC857;

	public HatStyle style() {
		try {
			return HatStyle.valueOf(style);
		} catch (IllegalArgumentException | NullPointerException e) {
			return HatStyle.IMPERIAL_SILK;
		}
	}

	public void clamp() {
		scale = Math.max(0.4f, Math.min(2.5f, scale));
		pointiness = Math.max(0.3f, Math.min(2.5f, pointiness));
		heightOffset = Math.max(-0.4f, Math.min(0.6f, heightOffset));
		tiltX = Math.max(-45f, Math.min(45f, tiltX));
		tiltZ = Math.max(-45f, Math.min(45f, tiltZ));
		spinSpeed = Math.max(-30f, Math.min(30f, spinSpeed));
		opacity = Math.max(0.05f, Math.min(1f, opacity));
		segments = Math.max(8, Math.min(48, segments));
		lineWidth = Math.max(1f, Math.min(8f, lineWidth));
		rainbowSpeed = Math.max(0.05f, Math.min(8f, rainbowSpeed));
		primary &= 0xFFFFFF;
		secondary &= 0xFFFFFF;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("chinesehat.json");
	}

	public static void load() {
		Path f = file();
		try {
			if (Files.exists(f)) {
				HatConfig loaded = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), HatConfig.class);
				if (loaded != null) {
					INSTANCE = loaded;
				}
			}
		} catch (Exception e) {
			System.err.println("[ChineseHat] Could not read config, using defaults: " + e);
		}
		INSTANCE.clamp();
		save();
	}

	public static void save() {
		try {
			INSTANCE.clamp();
			Files.createDirectories(file().getParent());
			Files.writeString(file(), GSON.toJson(INSTANCE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("[ChineseHat] Could not save config: " + e);
		}
	}

	public static void reset() {
		INSTANCE = new HatConfig();
		save();
	}
}
