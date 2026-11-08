package com.chinesehat.client;

/** Every hat variant. Colours are 0xRRGGBB. */
public enum HatStyle {
	//          name              description                                                    fill wire   box    gradient             primary   secondary accent    weave  glow   strap  knob
	BAMBOO("Bamboo Weave", "Classic woven straw hat with a red chin strap.",
			255, false, false, Gradient.VERTICAL, 0xE0C07A, 0xA87B3C, 0xB3261E, true, false, true, true),
	IMPERIAL_SILK("Imperial Silk", "Purple-to-gold silk with a golden rim and finial.",
			255, false, false, Gradient.ANGULAR, 0xB45CFF, 0xFFC857, 0xF2C14E, false, false, true, true),
	JADE("Carved Jade", "Translucent green jade with a woven pattern.",
			175, false, false, Gradient.VERTICAL, 0x3DDC97, 0x0E7A55, 0xD6FFE9, true, false, true, true),
	GLASS("Crystal Glass", "Mostly see-through glass with a bright rim.",
			80, false, false, Gradient.VERTICAL, 0xA8E8FF, 0xE6F8FF, 0xFFFFFF, false, false, false, true),
	HITBOX("Hitbox", "Wireframe cone inside an F3+B style hitbox with eye and look lines.",
			0, true, true, Gradient.NONE, 0xFFFFFF, 0xFF3B3B, 0x4D7CFF, false, true, false, false),
	HITBOX_SOLID("Solid Hitbox", "Like Hitbox, but the cone is a tinted, see-through solid.",
			55, true, true, Gradient.NONE, 0xFFFFFF, 0xFF3B3B, 0x4D7CFF, false, true, false, false),
	HITBOX_BOX("Box Only", "Just the hitbox cage around the hat, no cone.",
			0, false, true, Gradient.NONE, 0xFFFFFF, 0xFF3B3B, 0x4D7CFF, false, true, false, false),
	NEON("Neon Wire", "Glowing cyan-to-magenta wireframe over a faint tint.",
			28, true, false, Gradient.ANGULAR, 0x00F0FF, 0xFF2BD6, 0xFFFFFF, false, true, false, false),
	RAINBOW("Rainbow", "Animated chroma hat that cycles through every colour.",
			255, false, false, Gradient.RAINBOW, 0xFF0000, 0x0000FF, 0xFFFFFF, false, false, true, true),
	GHOST("Ghost", "A barely-there white tint with a soft glowing rim.",
			45, false, false, Gradient.NONE, 0xFFFFFF, 0xDDE6FF, 0xFFFFFF, false, true, false, false),
	VOID("Void", "Dark obsidian cone with glowing purple wire ribs.",
			255, true, false, Gradient.VERTICAL, 0x0B0614, 0x5B21B6, 0xC77DFF, true, false, true, false);

	public enum Gradient { NONE, ANGULAR, VERTICAL, RAINBOW }

	public final String displayName;
	public final String description;
	/** 0 = no solid surface, 255 = fully opaque. */
	public final int fillAlpha;
	public final boolean wire;
	public final boolean box;
	public final Gradient gradient;
	public final int primary;
	public final int secondary;
	public final int accent;
	public final boolean weave;
	public final boolean glow;
	public final boolean strap;
	public final boolean knob;

	HatStyle(String displayName, String description, int fillAlpha, boolean wire, boolean box, Gradient gradient,
			int primary, int secondary, int accent, boolean weave, boolean glow, boolean strap, boolean knob) {
		this.displayName = displayName;
		this.description = description;
		this.fillAlpha = fillAlpha;
		this.wire = wire;
		this.box = box;
		this.gradient = gradient;
		this.primary = primary;
		this.secondary = secondary;
		this.accent = accent;
		this.weave = weave;
		this.glow = glow;
		this.strap = strap;
		this.knob = knob;
	}
}
