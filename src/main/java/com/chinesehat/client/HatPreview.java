package com.chinesehat.client;

import net.minecraft.client.gui.GuiGraphics;

/** Flat 2D preview of the hat, drawn with plain rectangles (used by the menu). */
final class HatPreview {
	private static final float TAU = HatGeometry.TAU;

	private HatPreview() {
	}

	static void draw(GuiGraphics g, int x, int y, int w, int h, HatConfig c) {
		final HatStyle s = c.style();
		final long ms = System.currentTimeMillis();
		final float time = (ms % 1_000_000L) / 50f;
		final float phase = (float) Math.toRadians(time * c.spinSpeed);

		// night-sky backdrop + ground
		g.fillGradient(x, y, x + w, y + h, 0xFF22335C, 0xFF0A0F1F);
		for (int i = 0; i < 18; i++) {
			int sx = x + 6 + (i * 53) % (w - 12);
			int sy = y + 5 + (i * 29) % (h / 2);
			g.fill(sx, sy, sx + 1, sy + 1, 0x99FFFFFF);
		}
		g.fill(x, y + h - 9, x + w, y + h, 0xFF1E3A26);

		final float ppb = 60f; // pixels per block
		final float ds = Math.min(c.scale, 1.5f);
		final int cx = x + w / 2;
		final int headTop = y + h - 78;

		// the player: legs, torso, head
		g.fill(cx - 15, headTop + 60, cx + 15, y + h - 9, 0xFF3A3F8C);
		g.fill(cx - 15, headTop + 30, cx + 15, headTop + 60, 0xFF2FA7B8);
		g.fill(cx - 24, headTop + 30, cx - 15, headTop + 58, 0xFFC58A64);
		g.fill(cx + 15, headTop + 30, cx + 24, headTop + 58, 0xFFC58A64);
		g.fill(cx - 15, headTop, cx + 15, headTop + 30, 0xFFC58A64);
		g.fill(cx - 15, headTop, cx + 15, headTop + 7, 0xFF4A2F1A);
		g.fill(cx - 10, headTop + 13, cx - 5, headTop + 17, 0xFFFFFFFF);
		g.fill(cx + 5, headTop + 13, cx + 10, headTop + 17, 0xFFFFFFFF);
		g.fill(cx - 8, headTop + 13, cx - 5, headTop + 17, 0xFF3A2A8F);
		g.fill(cx + 5, headTop + 13, cx + 8, headTop + 17, 0xFF3A2A8F);
		g.fill(cx - 5, headTop + 23, cx + 5, headTop + 25, 0xFF7A3E2C);

		if (!c.enabled) {
			return;
		}

		final float hb = HatGeometry.height(c);
		final float rPx = HatGeometry.RADIUS * ppb * ds;
		final float hPx = hb * ppb * ds;
		final float rimY = headTop - c.heightOffset * ppb;
		final float apexY = rimY - hPx;
		final int accent = HatShading.accent(s, c);

		// chin strap
		if (c.strap && s.strap && s.fillAlpha > 0) {
			int sc = HatShading.argb(255, accent);
			g.fill(cx - 17, headTop + 2, cx - 15, headTop + 29, sc);
			g.fill(cx + 15, headTop + 2, cx + 17, headTop + 29, sc);
			g.fill(cx - 17, headTop + 28, cx + 17, headTop + 31, sc);
		}

		// solid cone, one scanline at a time
		final int a8 = Math.round(s.fillAlpha * c.opacity);
		if (a8 > 0) {
			final float lx = -0.3f, ly = 0.8f, lz = -0.5f;
			final float ll = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);
			for (int yy = Math.round(apexY); yy <= Math.round(rimY); yy++) {
				float yh = (rimY - yy) / (ppb * ds);
				float t = Math.max(0f, 1f - yh / hb);
				float r01 = (float) Math.pow(t, 1.0 / HatGeometry.CURVE);
				float rpx = Math.max(0.6f, rPx * r01);
				int cols = Math.max(2, Math.round(rpx / 3f));
				int ring = Math.min(HatGeometry.RINGS - 1, (int) ((1f - r01) * HatGeometry.RINGS));
				float slope = (hb * HatGeometry.CURVE / HatGeometry.RADIUS)
						* (float) Math.pow(Math.max(r01, 0.04f), HatGeometry.CURVE - 1f);
				for (int i = 0; i < cols; i++) {
					float u0 = -1f + 2f * i / cols;
					float u1 = -1f + 2f * (i + 1) / cols;
					float um = (u0 + u1) * 0.5f;
					float a = -(float) Math.acos(Math.max(-1f, Math.min(1f, um))) + phase;
					int sector = Math.floorMod((int) Math.floor(a / TAU * c.segments), c.segments);
					int rgb = HatShading.sample(s, c, a, 1f - r01, ring, sector, time);
					float nz = -(float) Math.sqrt(Math.max(0f, 1f - um * um)) * slope;
					float nx = um * slope;
					float nl = (float) Math.sqrt(nx * nx + 1f + nz * nz);
					float dot = (nx * lx + 1f * ly + nz * lz) / (nl * ll);
					float shade = s.glow ? 1f : 0.55f + 0.45f * Math.max(0f, dot);
					int xl = cx + Math.round(u0 * rpx);
					int xr = cx + Math.round(u1 * rpx);
					if (xr <= xl) {
						xr = xl + 1;
					}
					g.fill(xl, yy, xr, yy + 1, HatShading.argb(a8, HatShading.mul(rgb, shade)));
				}
			}
			// rim (front arc only, the back arc is hidden behind the cone)
			int rimAlpha = Math.round(Math.max(s.fillAlpha, 120) * c.opacity);
			int rimCol = HatShading.argb(rimAlpha, accent);
			for (int d = 0; d < 120; d++) {
				float a = (float) Math.PI * d / 119f;
				int px = Math.round(cx + rPx * (float) Math.cos(a));
				int py = Math.round(rimY + rPx * 0.16f * (float) Math.sin(a));
				g.fill(px - 1, py - 1, px + 1, py + 2, rimCol);
			}
			if (s.knob && c.knob) {
				int kc = HatShading.argb(Math.round(Math.max(s.fillAlpha, 200) * c.opacity), accent);
				g.fill(cx - 3, Math.round(apexY) - 4, cx + 3, Math.round(apexY) + 1, kc);
				g.fill(cx - 2, Math.round(apexY) - 8, cx + 2, Math.round(apexY) - 3, kc);
			}
		}

		// wireframe parts
		final int wAlpha = Math.round(255f * c.opacity);
		final int dot = Math.max(1, Math.round(c.lineWidth / 2f));
		if (s.wire) {
			for (int k = 0; k < HatGeometry.RINGS; k += 2) {
				float r01 = 1f - (float) k / HatGeometry.RINGS;
				float rp = rPx * r01;
				float ry = rimY - HatGeometry.profile(r01, hb) * ppb * ds;
				for (int d = 0; d < 90; d++) {
					float a = TAU * d / 90f;
					int col = HatShading.argb(wAlpha, HatShading.wireColor(s, c, a + phase, time));
					int px = Math.round(cx + rp * (float) Math.cos(a));
					int py = Math.round(ry + rp * 0.16f * (float) Math.sin(a));
					g.fill(px, py, px + dot, py + dot, col);
				}
			}
			int step = Math.max(1, c.segments / 14);
			for (int j = 0; j < c.segments; j += step) {
				float a = TAU * j / c.segments;
				int col = HatShading.argb(wAlpha, HatShading.wireColor(s, c, a + phase, time));
				line(g, cx + rPx * (float) Math.cos(a), rimY + rPx * 0.16f * (float) Math.sin(a), cx, apexY, col, dot);
			}
		}
		if (s.box) {
			int boxCol = HatShading.argb(wAlpha, HatShading.primary(s, c));
			int eyeCol = HatShading.argb(wAlpha, HatShading.secondary(s, c));
			int lookCol = HatShading.argb(wAlpha, accent);
			float d = rPx * 0.16f;
			float l = cx - rPx, r = cx + rPx;
			// front rectangle (lower) and back rectangle (higher) fake a little perspective
			rect(g, l, apexY + d, r, rimY + d, boxCol, dot);
			rect(g, l, apexY - d, r, rimY - d, boxCol, dot);
			line(g, l, apexY + d, l, apexY - d, boxCol, dot);
			line(g, r, apexY + d, r, apexY - d, boxCol, dot);
			line(g, l, rimY + d, l, rimY - d, boxCol, dot);
			line(g, r, rimY + d, r, rimY - d, boxCol, dot);
			float ye = rimY - hPx * 0.35f;
			rect(g, l, ye + d, r, ye + d, eyeCol, dot);
			rect(g, l, ye - d, r, ye - d, eyeCol, dot);
			line(g, cx, ye, cx - 16, ye + d + 6, lookCol, Math.max(dot, 2));
		}
	}

	private static void rect(GuiGraphics g, float x0, float y0, float x1, float y1, int col, int w) {
		line(g, x0, y0, x1, y0, col, w);
		line(g, x1, y0, x1, y1, col, w);
		line(g, x1, y1, x0, y1, col, w);
		line(g, x0, y1, x0, y0, col, w);
	}

	private static void line(GuiGraphics g, float x0, float y0, float x1, float y1, int col, int w) {
		float dx = x1 - x0;
		float dy = y1 - y0;
		int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy))));
		for (int i = 0; i <= steps; i++) {
			int px = Math.round(x0 + dx * i / steps);
			int py = Math.round(y0 + dy * i / steps);
			g.fill(px, py, px + w, py + w, col);
		}
	}
}
