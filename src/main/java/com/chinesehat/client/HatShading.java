package com.chinesehat.client;

/** Colour helpers shared by the 3D hat and the menu preview. All colours are 0xRRGGBB unless noted. */
final class HatShading {
	private HatShading() {
	}

	static int primary(HatStyle s, HatConfig c) {
		return c.customColors ? c.primary : s.primary;
	}

	static int secondary(HatStyle s, HatConfig c) {
		return c.customColors ? c.secondary : s.secondary;
	}

	static int accent(HatStyle s, HatConfig c) {
		return c.customColors ? c.secondary : s.accent;
	}

	/** Colour of one patch of the cone. angle in radians, v: 0 at the rim, 1 at the tip. */
	static int sample(HatStyle s, HatConfig c, float angle, float v, int ring, int sector, float timeTicks) {
		int p = primary(s, c);
		int q = secondary(s, c);
		int col;
		switch (s.gradient) {
			case ANGULAR -> col = lerp(p, q, 0.5f + 0.5f * (float) Math.cos(angle));
			case VERTICAL -> col = lerp(p, q, clamp01(v));
			case RAINBOW -> {
				// double + modulo keeps the colour animation smooth even after hours of playtime
				float drift = (float) (((double) timeTicks * 0.01 * c.rainbowSpeed) % 1.0);
				float h = angle / (float) (Math.PI * 2.0) + drift;
				h = h - (float) Math.floor(h);
				col = hsv(h, 0.85f, 1f);
			}
			default -> col = p;
		}
		if (s.weave && ((ring + sector) & 1) == 1) {
			col = mul(col, 0.84f);
		}
		return col;
	}

	/** Colour used for wireframe lines. */
	static int wireColor(HatStyle s, HatConfig c, float angle, float timeTicks) {
		if (s.gradient == HatStyle.Gradient.ANGULAR || s.gradient == HatStyle.Gradient.RAINBOW) {
			return sample(s, c, angle, 0.5f, 0, 0, timeTicks);
		}
		if (s.fillAlpha >= 200) {
			return accent(s, c);
		}
		return primary(s, c);
	}

	static float clamp01(float v) {
		return v < 0f ? 0f : (v > 1f ? 1f : v);
	}

	static int lerp(int a, int b, float t) {
		t = clamp01(t);
		int r = Math.round(((a >> 16) & 255) * (1f - t) + ((b >> 16) & 255) * t);
		int g = Math.round(((a >> 8) & 255) * (1f - t) + ((b >> 8) & 255) * t);
		int bl = Math.round((a & 255) * (1f - t) + (b & 255) * t);
		return (r << 16) | (g << 8) | bl;
	}

	static int mul(int rgb, float f) {
		int r = Math.min(255, Math.round(((rgb >> 16) & 255) * f));
		int g = Math.min(255, Math.round(((rgb >> 8) & 255) * f));
		int b = Math.min(255, Math.round((rgb & 255) * f));
		return (r << 16) | (g << 8) | b;
	}

	static int argb(int alpha, int rgb) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
	}

	static int hsv(float h, float s, float v) {
		float r = 0, g = 0, b = 0;
		int i = (int) (h * 6f);
		float f = h * 6f - i;
		float p = v * (1f - s);
		float q = v * (1f - f * s);
		float t = v * (1f - (1f - f) * s);
		switch (i % 6) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			default -> { r = v; g = p; b = q; }
		}
		return (Math.round(r * 255) << 16) | (Math.round(g * 255) << 8) | Math.round(b * 255);
	}
}
