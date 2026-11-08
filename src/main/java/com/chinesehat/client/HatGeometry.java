package com.chinesehat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Builds the hat. Local space: +Y is up, the rim centre is the origin, the tip is at (0, height, 0).
 * Units are blocks (the player's head is 0.5 blocks wide).
 */
final class HatGeometry {
	static final float RADIUS = 0.62f;
	static final float BASE_HEIGHT = 0.40f;
	/** < 1 gives the gently flared, concave profile of a real douli. */
	static final float CURVE = 0.88f;
	static final int RINGS = 8;
	static final float SHELL = 0.022f;
	static final float TAU = (float) (Math.PI * 2.0);

	private HatGeometry() {
	}

	static float height(HatConfig c) {
		return BASE_HEIGHT * c.pointiness;
	}

	/** Height of the surface at radius fraction r01 (1 = rim, 0 = tip). */
	static float profile(float r01, float h) {
		return h * (1f - (float) Math.pow(Math.max(0f, r01), CURVE));
	}

	// ------------------------------------------------------------------ solid parts

	/** The solid cone (outer shell, inner shell, rim band, finial). */
	static void fill(PoseStack.Pose pose, VertexConsumer vc, HatStyle s, HatConfig c, float time, int light) {
		int alpha = Math.round(s.fillAlpha * c.opacity);
		if (alpha <= 0) {
			return;
		}
		final int n = c.segments;
		final float h = height(c);

		float[] cs = new float[n + 1];
		float[] sn = new float[n + 1];
		for (int j = 0; j <= n; j++) {
			float a = TAU * j / n;
			cs[j] = (float) Math.cos(a);
			sn[j] = (float) Math.sin(a);
		}
		float[] rr = new float[RINGS + 1];
		float[] yy = new float[RINGS + 1];
		float[] nm = new float[RINGS + 1]; // horizontal part of the normal
		float[] ny = new float[RINGS + 1]; // vertical part of the normal
		for (int k = 0; k <= RINGS; k++) {
			float r01 = 1f - (float) k / RINGS;
			rr[k] = RADIUS * r01;
			yy[k] = profile(r01, h);
			float slope = (h * CURVE / RADIUS) * (float) Math.pow(Math.max(r01, 0.04f), CURVE - 1f);
			float inv = 1f / (float) Math.sqrt(slope * slope + 1f);
			nm[k] = slope * inv;
			ny[k] = inv;
		}

		for (int shell = 0; shell < 2; shell++) {
			float dy = shell == 0 ? 0f : -SHELL;
			float side = shell == 0 ? 1f : -1f;
			float dim = shell == 0 ? 1f : 0.62f;
			int shellAlpha = shell == 0 ? alpha : Math.round(alpha * 0.92f);
			for (int k = 0; k < RINGS; k++) {
				for (int j = 0; j < n; j++) {
					float mid = TAU * (j + 0.5f) / n;
					int rgb = HatShading.sample(s, c, mid, (k + 0.5f) / RINGS, k, j, time);
					int argb = HatShading.argb(shellAlpha, HatShading.mul(rgb, dim));
					vert(vc, pose, rr[k] * cs[j], yy[k] + dy, rr[k] * sn[j], argb, light,
							side * cs[j] * nm[k], side * ny[k], side * sn[j] * nm[k]);
					vert(vc, pose, rr[k] * cs[j + 1], yy[k] + dy, rr[k] * sn[j + 1], argb, light,
							side * cs[j + 1] * nm[k], side * ny[k], side * sn[j + 1] * nm[k]);
					vert(vc, pose, rr[k + 1] * cs[j + 1], yy[k + 1] + dy, rr[k + 1] * sn[j + 1], argb, light,
							side * cs[j + 1] * nm[k + 1], side * ny[k + 1], side * sn[j + 1] * nm[k + 1]);
					vert(vc, pose, rr[k + 1] * cs[j], yy[k + 1] + dy, rr[k + 1] * sn[j], argb, light,
							side * cs[j] * nm[k + 1], side * ny[k + 1], side * sn[j] * nm[k + 1]);
				}
			}
		}

		// rim band joining the two shells
		int accent = HatShading.accent(s, c);
		int rimAlpha = Math.round(Math.max(s.fillAlpha, 120) * c.opacity);
		int rimArgb = HatShading.argb(rimAlpha, accent);
		float bandTop = 0.004f;
		float bandBottom = -SHELL - 0.006f;
		for (int j = 0; j < n; j++) {
			float x0 = RADIUS * cs[j], z0 = RADIUS * sn[j];
			float x1 = RADIUS * cs[j + 1], z1 = RADIUS * sn[j + 1];
			vert(vc, pose, x0, bandTop, z0, rimArgb, light, cs[j], 0f, sn[j]);
			vert(vc, pose, x1, bandTop, z1, rimArgb, light, cs[j + 1], 0f, sn[j + 1]);
			vert(vc, pose, x1, bandBottom, z1, rimArgb, light, cs[j + 1], 0f, sn[j + 1]);
			vert(vc, pose, x0, bandBottom, z0, rimArgb, light, cs[j], 0f, sn[j]);
		}

		// finial on the tip
		if (s.knob && c.knob) {
			int knobArgb = HatShading.argb(Math.round(Math.max(s.fillAlpha, 200) * c.opacity), accent);
			prism(vc, pose, knobArgb, light, 0f, h - 0.006f, h + 0.02f, 0f, 0.05f, 10);
			prism(vc, pose, knobArgb, light, 0f, h + 0.02f, h + 0.07f, 0f, 0.028f, 10);
			prism(vc, pose, knobArgb, light, 0f, h + 0.07f, h + 0.105f, 0f, 0.042f, 10);
		}
	}

	/** Chin strap, drawn in head space (not scaled with the hat). Head: x/z in [-0.25, 0.25], y in [0, 0.5]. */
	static void strap(PoseStack.Pose pose, VertexConsumer vc, HatStyle s, HatConfig c, int light) {
		int argb = HatShading.argb(Math.round(255 * c.opacity), HatShading.accent(s, c));
		float o = 0.014f;
		// left & right ribbons beside the ears
		cuboid(vc, pose, argb, light, -0.25f - o, 0.03f, -0.035f, -0.25f, 0.49f, 0.035f);
		cuboid(vc, pose, argb, light, 0.25f, 0.03f, -0.035f, 0.25f + o, 0.49f, 0.035f);
		// band across the chin
		cuboid(vc, pose, argb, light, -0.25f - o, -0.004f, -0.25f - o, 0.25f + o, 0.05f, -0.25f);
	}

	// ------------------------------------------------------------------ wireframe parts

	static void wire(PoseStack.Pose pose, VertexConsumer vc, HatStyle s, HatConfig c, float time) {
		int alpha = Math.round(255f * c.opacity);
		float w = c.lineWidth * (s == HatStyle.NEON ? 1.6f : 1f);
		final float h = height(c);
		final int n = c.segments;

		if (s.wire) {
			// rings (rim, then every other ring up to the tip)
			for (int k = 0; k < RINGS; k += 2) {
				float r01 = 1f - (float) k / RINGS;
				float r = RADIUS * r01;
				float y = profile(r01, h);
				for (int j = 0; j < n; j++) {
					float a0 = TAU * j / n;
					float a1 = TAU * (j + 1) / n;
					int col = HatShading.argb(alpha, HatShading.wireColor(s, c, (a0 + a1) * 0.5f, time));
					line(vc, pose,
							r * (float) Math.cos(a0), y, r * (float) Math.sin(a0),
							r * (float) Math.cos(a1), y, r * (float) Math.sin(a1), col, w);
				}
			}
			// ribs from rim to tip, following the curved profile
			int step = Math.max(1, n / 14);
			for (int j = 0; j < n; j += step) {
				float a = TAU * j / n;
				float ca = (float) Math.cos(a);
				float sa = (float) Math.sin(a);
				int col = HatShading.argb(alpha, HatShading.wireColor(s, c, a, time));
				for (int k = 0; k < RINGS; k++) {
					float r0 = RADIUS * (1f - (float) k / RINGS);
					float r1 = RADIUS * (1f - (float) (k + 1) / RINGS);
					float y0 = profile(1f - (float) k / RINGS, h);
					float y1 = profile(1f - (float) (k + 1) / RINGS, h);
					line(vc, pose, r0 * ca, y0, r0 * sa, r1 * ca, y1, r1 * sa, col, w);
				}
			}
		}

		if (s.box) {
			int boxCol = HatShading.argb(alpha, HatShading.primary(s, c));
			int eyeCol = HatShading.argb(alpha, HatShading.secondary(s, c));
			int lookCol = HatShading.argb(alpha, HatShading.accent(s, c));
			float x0 = -RADIUS, x1 = RADIUS, z0 = -RADIUS, z1 = RADIUS, y0 = 0f, y1 = h;
			// 12 edges of the hitbox
			line(vc, pose, x0, y0, z0, x1, y0, z0, boxCol, w);
			line(vc, pose, x1, y0, z0, x1, y0, z1, boxCol, w);
			line(vc, pose, x1, y0, z1, x0, y0, z1, boxCol, w);
			line(vc, pose, x0, y0, z1, x0, y0, z0, boxCol, w);
			line(vc, pose, x0, y1, z0, x1, y1, z0, boxCol, w);
			line(vc, pose, x1, y1, z0, x1, y1, z1, boxCol, w);
			line(vc, pose, x1, y1, z1, x0, y1, z1, boxCol, w);
			line(vc, pose, x0, y1, z1, x0, y1, z0, boxCol, w);
			line(vc, pose, x0, y0, z0, x0, y1, z0, boxCol, w);
			line(vc, pose, x1, y0, z0, x1, y1, z0, boxCol, w);
			line(vc, pose, x1, y0, z1, x1, y1, z1, boxCol, w);
			line(vc, pose, x0, y0, z1, x0, y1, z1, boxCol, w);
			// "eye height" rectangle and "look vector" like the vanilla F3+B hitbox
			float ye = h * 0.35f;
			line(vc, pose, x0, ye, z0, x1, ye, z0, eyeCol, w);
			line(vc, pose, x1, ye, z0, x1, ye, z1, eyeCol, w);
			line(vc, pose, x1, ye, z1, x0, ye, z1, eyeCol, w);
			line(vc, pose, x0, ye, z1, x0, ye, z0, eyeCol, w);
			line(vc, pose, 0f, ye, 0f, 0f, ye, -RADIUS, lookCol, w * 1.5f);
		}
	}

	// ------------------------------------------------------------------ primitives

	private static void vert(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float z, int argb, int light,
			float nx, float ny, float nz) {
		vc.addVertex(pose, x, y, z)
				.setColor(argb)
				.setUv(0.5f, 0.5f)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.setNormal(pose, nx, ny, nz);
	}

	private static void quad(VertexConsumer vc, PoseStack.Pose pose, int argb, int light, float nx, float ny, float nz,
			float x0, float y0, float z0, float x1, float y1, float z1,
			float x2, float y2, float z2, float x3, float y3, float z3) {
		vert(vc, pose, x0, y0, z0, argb, light, nx, ny, nz);
		vert(vc, pose, x1, y1, z1, argb, light, nx, ny, nz);
		vert(vc, pose, x2, y2, z2, argb, light, nx, ny, nz);
		vert(vc, pose, x3, y3, z3, argb, light, nx, ny, nz);
	}

	private static void cuboid(VertexConsumer vc, PoseStack.Pose pose, int argb, int light,
			float x0, float y0, float z0, float x1, float y1, float z1) {
		quad(vc, pose, argb, light, 0, 0, -1, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0); // north
		quad(vc, pose, argb, light, 0, 0, 1, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);  // south
		quad(vc, pose, argb, light, -1, 0, 0, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0); // west
		quad(vc, pose, argb, light, 1, 0, 0, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);  // east
		quad(vc, pose, argb, light, 0, 1, 0, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);  // top
		quad(vc, pose, argb, light, 0, -1, 0, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1); // bottom
	}

	private static void prism(VertexConsumer vc, PoseStack.Pose pose, int argb, int light,
			float cx, float y0, float y1, float cz, float radius, int sides) {
		for (int i = 0; i < sides; i++) {
			float a0 = TAU * i / sides;
			float a1 = TAU * (i + 1) / sides;
			float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
			float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
			float am = (a0 + a1) * 0.5f;
			float nx = (float) Math.cos(am), nz = (float) Math.sin(am);
			quad(vc, pose, argb, light, nx, 0, nz,
					cx + radius * c0, y0, cz + radius * s0,
					cx + radius * c1, y0, cz + radius * s1,
					cx + radius * c1, y1, cz + radius * s1,
					cx + radius * c0, y1, cz + radius * s0);
			// top cap
			quad(vc, pose, argb, light, 0, 1, 0,
					cx, y1, cz,
					cx + radius * c0, y1, cz + radius * s0,
					cx + radius * c1, y1, cz + radius * s1,
					cx + radius * c1, y1, cz + radius * s1);
		}
	}

	private static void line(VertexConsumer vc, PoseStack.Pose pose,
			float x0, float y0, float z0, float x1, float y1, float z1, int argb, float width) {
		float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len < 1.0e-6f) {
			return;
		}
		dx /= len;
		dy /= len;
		dz /= len;
		vc.addVertex(pose, x0, y0, z0).setColor(argb).setNormal(pose, dx, dy, dz).setLineWidth(width);
		vc.addVertex(pose, x1, y1, z1).setColor(argb).setNormal(pose, dx, dy, dz).setLineWidth(width);
	}
}
