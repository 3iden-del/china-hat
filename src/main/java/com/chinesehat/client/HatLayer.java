package com.chinesehat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Quaternionf;

/** Feature layer that draws the hat on top of a player's head. */
public final class HatLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final int FULL_BRIGHT = 0xF000F0;

	public HatLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
		super(parent);
	}

	@Override
	public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state,
			float yRot, float xRot) {
		final HatConfig c = HatConfig.INSTANCE;
		if (!c.enabled || state.isInvisible) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		boolean self = mc.player != null && state.id == mc.player.getId();
		if (self ? !c.showOnSelf : !c.showOnOthers) {
			return;
		}

		final HatStyle style = c.style();
		final float time = state.ageInTicks;
		final int lit = (c.fullbright || style.glow) ? FULL_BRIGHT : light;

		pose.pushPose();

		// move into the head's coordinate system (so the hat follows nods, turns and sneaking)
		ModelPart head = this.getParentModel().getHead();
		if (c.followHeadPitch) {
			head.translateAndRotate(pose);
		} else {
			pose.translate(head.x / 16f, head.y / 16f, head.z / 16f);
			pose.mulPose(new Quaternionf().rotationY(head.yRot));
		}
		// model space has +Y pointing down; flip it so +Y is up. The head now spans y = 0..0.5.
		pose.scale(1f, -1f, 1f);

		if (c.strap && style.strap && style.fillAlpha > 0) {
			collector.order(0).submitCustomGeometry(pose, RenderTypes.entityTranslucent(ChineseHatClient.WHITE),
					(p, vc) -> HatGeometry.strap(p, vc, style, c, lit));
		}

		pose.pushPose();
		pose.translate(0f, 0.5f + c.heightOffset, 0f);
		pose.mulPose(new Quaternionf().rotationXYZ((float) Math.toRadians(c.tiltX), 0f, (float) Math.toRadians(c.tiltZ)));
		if (c.spinSpeed != 0f) {
			pose.mulPose(new Quaternionf().rotationY((float) Math.toRadians(time * c.spinSpeed)));
		}
		pose.scale(c.scale, c.scale, c.scale);

		if (style.fillAlpha > 0) {
			collector.order(0).submitCustomGeometry(pose, RenderTypes.entityTranslucent(ChineseHatClient.WHITE),
					(p, vc) -> HatGeometry.fill(p, vc, style, c, time, lit));
		}
		if (style.wire || style.box) {
			collector.order(0).submitCustomGeometry(pose,
					c.opacity < 0.999f ? RenderTypes.linesTranslucent() : RenderTypes.lines(),
					(p, vc) -> HatGeometry.wire(p, vc, style, c, time));
		}

		pose.popPose();
		pose.popPose();
	}
}
