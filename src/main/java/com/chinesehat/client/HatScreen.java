package com.chinesehat.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/** The /hat configuration menu. */
public final class HatScreen extends Screen {
	private static final int PANEL_W = 440;
	private static final int PANEL_H = 280;
	private static final int LEFT_W = 236;
	private static final int PREVIEW_W = 166;
	private static final int PREVIEW_H = 164;

	private enum Tab {
		STYLE("Style"), SHAPE("Shape"), COLORS("Colors"), OPTIONS("Options");

		final String label;

		Tab(String label) {
			this.label = label;
		}
	}

	private static Tab currentTab = Tab.STYLE;

	private final Screen parent;
	private int px;
	private int py;
	private int x0;
	private int y0;

	public HatScreen(Screen parent) {
		super(Component.literal("Chinese Hat"));
		this.parent = parent;
	}

	// ------------------------------------------------------------------ layout

	@Override
	protected void init() {
		final HatConfig c = HatConfig.INSTANCE;
		px = (this.width - PANEL_W) / 2;
		py = (this.height - PANEL_H) / 2;
		x0 = px + 14;
		y0 = py + 48;

		// tabs
		Tab[] tabs = Tab.values();
		for (int i = 0; i < tabs.length; i++) {
			final Tab t = tabs[i];
			Component label = t == currentTab
					? Component.literal(t.label).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
					: Component.literal(t.label);
			addRenderableWidget(Button.builder(label, b -> {
				currentTab = t;
				this.rebuildWidgets();
			}).bounds(x0 + i * 74, py + 24, 70, 18).build());
		}

		switch (currentTab) {
			case STYLE -> buildStyleTab(c);
			case SHAPE -> buildShapeTab(c);
			case COLORS -> buildColorsTab(c);
			case OPTIONS -> buildOptionsTab(c);
		}

		// footer
		addRenderableWidget(Button.builder(Component.literal("Reset all").withStyle(ChatFormatting.RED), b -> {
			HatConfig.reset();
			this.rebuildWidgets();
		}).bounds(x0, py + PANEL_H - 28, 100, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
				.bounds(px + PANEL_W - 14 - 100, py + PANEL_H - 28, 100, 20).build());
	}

	private void buildStyleTab(HatConfig c) {
		HatStyle[] styles = HatStyle.values();
		int bw = (LEFT_W - 4) / 2;
		for (int i = 0; i < styles.length; i++) {
			final HatStyle s = styles[i];
			boolean selected = c.style() == s;
			Component label = selected
					? Component.literal("> " + s.displayName).withStyle(ChatFormatting.GOLD)
					: Component.literal(s.displayName);
			addRenderableWidget(Button.builder(label, b -> {
				c.style = s.name();
				this.rebuildWidgets();
			}).bounds(x0 + (i % 2) * (bw + 4), y0 + (i / 2) * 23, bw, 20).build());
		}
	}

	private void buildShapeTab(HatConfig c) {
		int y = y0;
		y = slider(y, "Size", 0.4, 2.5, 0.05, "%.2f", c.scale, v -> c.scale = (float) v);
		y = slider(y, "Pointiness", 0.3, 2.5, 0.05, "%.2f", c.pointiness, v -> c.pointiness = (float) v);
		y = slider(y, "Height offset", -0.4, 0.6, 0.01, "%.2f", c.heightOffset, v -> c.heightOffset = (float) v);
		y = slider(y, "Tilt forward/back", -45, 45, 1, "%.0f deg", c.tiltX, v -> c.tiltX = (float) v);
		y = slider(y, "Tilt sideways", -45, 45, 1, "%.0f deg", c.tiltZ, v -> c.tiltZ = (float) v);
		y = slider(y, "Spin speed", -30, 30, 0.5, "%.1f", c.spinSpeed, v -> c.spinSpeed = (float) v);
		slider(y, "Roundness", 8, 48, 1, "%.0f", c.segments, v -> c.segments = (int) Math.round(v));
	}

	private void buildColorsTab(HatConfig c) {
		int y = y0;
		toggle(x0, y, LEFT_W, "Custom colors", () -> c.customColors, v -> {
			if (v) {
				enableCustom(c);
			}
			c.customColors = v;
			this.rebuildWidgets();
		});
		y += 22;
		y = colorSlider(y, "Primary R", c, true, 16);
		y = colorSlider(y, "Primary G", c, true, 8);
		y = colorSlider(y, "Primary B", c, true, 0);
		y = colorSlider(y, "Secondary R", c, false, 16);
		y = colorSlider(y, "Secondary G", c, false, 8);
		y = colorSlider(y, "Secondary B", c, false, 0);
		y = slider(y, "Opacity", 0.05, 1.0, 0.01, "%.2f", c.opacity, v -> c.opacity = (float) v);

		// presets
		String[] names = {"Sun", "Sea", "Fire", "Mint", "Rose", "Mono"};
		int[][] cols = {
				{0xFF5E62, 0xFFC371}, {0x00C6FF, 0x0072FF}, {0xFF512F, 0xF09819},
				{0x11998E, 0x38EF7D}, {0xF953C6, 0xB91D73}, {0xFFFFFF, 0x8A8A8A}
		};
		int bw = (LEFT_W - 5 * 3) / 6;
		for (int i = 0; i < names.length; i++) {
			final int[] pair = cols[i];
			addRenderableWidget(Button.builder(Component.literal(names[i]), b -> {
				c.primary = pair[0];
				c.secondary = pair[1];
				c.customColors = true;
				this.rebuildWidgets();
			}).bounds(x0 + i * (bw + 3), y, bw, 20).build());
		}
	}

	private void buildOptionsTab(HatConfig c) {
		int bw = (LEFT_W - 4) / 2;
		int y = y0;
		toggle(x0, y, bw, "Hat", () -> c.enabled, v -> c.enabled = v);
		toggle(x0 + bw + 4, y, bw, "On me", () -> c.showOnSelf, v -> c.showOnSelf = v);
		y += 22;
		toggle(x0, y, bw, "On others", () -> c.showOnOthers, v -> c.showOnOthers = v);
		toggle(x0 + bw + 4, y, bw, "Head tilt", () -> c.followHeadPitch, v -> c.followHeadPitch = v);
		y += 22;
		toggle(x0, y, bw, "Chin strap", () -> c.strap, v -> c.strap = v);
		toggle(x0 + bw + 4, y, bw, "Top knob", () -> c.knob, v -> c.knob = v);
		y += 22;
		toggle(x0, y, LEFT_W, "Fullbright (ignore lighting)", () -> c.fullbright, v -> c.fullbright = v);
		y += 22;
		y = slider(y, "Wire width", 1, 8, 0.5, "%.1f", c.lineWidth, v -> c.lineWidth = (float) v);
		slider(y, "Rainbow speed", 0.05, 8, 0.05, "%.2f", c.rainbowSpeed, v -> c.rainbowSpeed = (float) v);
	}

	// ------------------------------------------------------------------ widget helpers

	private int slider(int y, String label, double min, double max, double step, String fmt, double cur,
			DoubleConsumer setter) {
		addRenderableWidget(new ValueSlider(x0, y, LEFT_W, 20, label, min, max, step, fmt, cur, setter));
		return y + 22;
	}

	private int colorSlider(int y, String label, HatConfig c, boolean primary, int shift) {
		HatStyle s = c.style();
		int eff = primary ? HatShading.primary(s, c) : HatShading.secondary(s, c);
		return slider(y, label, 0, 255, 1, "%.0f", (eff >> shift) & 255, v -> {
			enableCustom(c);
			int ch = (int) Math.round(v) & 255;
			if (primary) {
				c.primary = (c.primary & ~(255 << shift)) | (ch << shift);
			} else {
				c.secondary = (c.secondary & ~(255 << shift)) | (ch << shift);
			}
		});
	}

	/** Copies the current style's colours into the custom slots the first time the user edits colours. */
	private static void enableCustom(HatConfig c) {
		if (!c.customColors) {
			HatStyle s = c.style();
			c.primary = s.primary;
			c.secondary = s.secondary;
			c.customColors = true;
		}
	}

	private void toggle(int x, int y, int w, String label, BooleanSupplier get, Consumer<Boolean> set) {
		addRenderableWidget(Button.builder(toggleText(label, get.getAsBoolean()), b -> {
			boolean now = !get.getAsBoolean();
			set.accept(now);
			b.setMessage(toggleText(label, now));
		}).bounds(x, y, w, 20).build());
	}

	private static Component toggleText(String label, boolean on) {
		return Component.literal(label + ": ")
				.append(Component.literal(on ? "ON" : "OFF").withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
	}

	// ------------------------------------------------------------------ drawing

	@Override
	public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(g, mouseX, mouseY, partialTick);

		// panel
		g.fill(px - 1, py - 1, px + PANEL_W + 1, py + PANEL_H + 1, 0xFF7A4BB8);
		g.fill(px, py, px + PANEL_W, py + PANEL_H, 0xF2140E22);
		g.fillGradient(px, py, px + PANEL_W, py + 20, 0xFF4A2380, 0xFF22103F);
		g.fill(px, py + 20, px + PANEL_W, py + 21, 0xFFE0B04A);

		// preview frame
		int prx = px + PANEL_W - 14 - PREVIEW_W;
		g.fill(prx - 2, y0 - 2, prx + PREVIEW_W + 2, y0 + PREVIEW_H + 2, 0xFF3B2A5C);
		g.enableScissor(prx, y0, prx + PREVIEW_W, y0 + PREVIEW_H);
		HatPreview.draw(g, prx, y0, PREVIEW_W, PREVIEW_H, HatConfig.INSTANCE);
		g.disableScissor();
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);

		g.drawCenteredString(this.font, "CHINESE HAT", px + PANEL_W / 2, py + 6, 0xFFFFD27A);

		// style name + description under the preview
		HatStyle s = HatConfig.INSTANCE.style();
		int prx = px + PANEL_W - 14 - PREVIEW_W;
		int ty = y0 + PREVIEW_H + 6;
		g.drawString(this.font, s.displayName, prx, ty, 0xFFFFD27A);
		for (String line : wrap(s.description, PREVIEW_W)) {
			ty += 10;
			g.drawString(this.font, line, prx, ty, 0xFFB9B2CC);
		}

		g.drawString(this.font, "Commands: /hat  /hat toggle  /hat reset", x0 + 110, py + PANEL_H - 22, 0xFF7C7596);
	}

	private List<String> wrap(String text, int maxWidth) {
		List<String> lines = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		for (String word : text.split(" ")) {
			String test = cur.length() == 0 ? word : cur + " " + word;
			if (this.font.width(test) > maxWidth && cur.length() > 0) {
				lines.add(cur.toString());
				cur = new StringBuilder(word);
			} else {
				cur = new StringBuilder(test);
			}
		}
		if (cur.length() > 0) {
			lines.add(cur.toString());
		}
		return lines;
	}

	@Override
	public void onClose() {
		HatConfig.save();
		this.minecraft.setScreen(parent);
	}
}
