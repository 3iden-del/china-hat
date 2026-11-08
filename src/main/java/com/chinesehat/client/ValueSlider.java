package com.chinesehat.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;

/** A labelled slider mapped onto [min, max] with optional step snapping. */
final class ValueSlider extends AbstractSliderButton {
	private final String label;
	private final double min;
	private final double max;
	private final double step;
	private final String format;
	private final DoubleConsumer setter;

	ValueSlider(int x, int y, int width, int height, String label, double min, double max, double step,
			String format, double current, DoubleConsumer setter) {
		super(x, y, width, height, Component.empty(), (Math.max(min, Math.min(max, current)) - min) / (max - min));
		this.label = label;
		this.min = min;
		this.max = max;
		this.step = step;
		this.format = format;
		this.setter = setter;
		updateMessage();
	}

	private double real() {
		double v = min + (max - min) * this.value;
		if (step > 0) {
			v = Math.round(v / step) * step;
		}
		return Math.max(min, Math.min(max, v));
	}

	@Override
	protected void updateMessage() {
		setMessage(Component.literal(label + ": " + String.format(format, real())));
	}

	@Override
	protected void applyValue() {
		setter.accept(real());
	}
}
