package de.dafuqs.paginatedadvancements.client;

import com.mojang.blaze3d.systems.*;
import de.dafuqs.paginatedadvancements.frames.*;
import de.dafuqs.paginatedadvancements.mixin.*;
import net.fabricmc.api.*;
import net.minecraft.advancement.*;
import net.minecraft.client.*;
import net.minecraft.client.font.*;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.client.util.math.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;

import java.util.*;

@Environment(EnvType.CLIENT)
public class PaginatedAdvancementWidget extends AdvancementWidget {
	
	private static final Identifier VANILLA_WIDGETS_TEXTURE = new Identifier("textures/gui/advancements/widgets.png");
	
	public PaginatedAdvancementWidget(AdvancementTab tab, MinecraftClient client, Advancement advancement, AdvancementDisplay display) {
		super(tab, client, advancement, display);
	}
	
	@Override
	public void renderWidgets(MatrixStack matrices, int x, int y) {
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		
		if (!accessor.getDisplay().isHidden() || accessor.getProgress() != null && accessor.getProgress().isDone()) {
			float f = accessor.getProgress() == null ? 0.0F : accessor.getProgress().getProgressBarPercentage();
			AdvancementObtainedStatus advancementObtainedStatus;
			if (f >= 1.0F) {
				advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
			} else {
				advancementObtainedStatus = AdvancementObtainedStatus.UNOBTAINED;
			}
			
			@Nullable FrameWrapper frameWrapper = AdvancementFrameDataLoader.get(accessor.getAdvancement().getId());
			if (frameWrapper == null) {
				RenderSystem.setShaderTexture(0, VANILLA_WIDGETS_TEXTURE);
				drawTexture(matrices, x + accessor.getX() + 3, y + accessor.getY(), accessor.getDisplay().getFrame().getTextureV(), 128 + advancementObtainedStatus.getSpriteIndex() * 26, 26, 26);
			} else {
				RenderSystem.setShaderTexture(0, frameWrapper.getTextureSheet());
				drawTexture(matrices, x + accessor.getX() + 3, y + accessor.getY(), frameWrapper.getTextureV(), frameWrapper.getTextureU() + advancementObtainedStatus.getSpriteIndex() * 26, 26, 26);
			}
			MinecraftClient.getInstance().getItemRenderer().renderInGui(matrices, accessor.getDisplay().getIcon(), x + accessor.getX() + 8, y + accessor.getY() + 5);
		}
		
		for (AdvancementWidget advancementWidget : accessor.getChildren()) {
			advancementWidget.renderWidgets(matrices, x, y);
		}
	}
	
	@Override
	public void drawTooltip(MatrixStack matrices, int originX, int originY, float alpha, int x, int y) {
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		
		boolean bl = x + originX + accessor.getX() + accessor.getWidth() + 26 >= accessor.getTab().getScreen().width;
		String string = accessor.getProgress() == null ? null : accessor.getProgress().getProgressBarFraction();
		int i = string == null ? 0 : textRenderer.getWidth(string);
		int var10000 = 113 - originY - accessor.getY() - 26;
		int var10002 = accessor.getDescription().size();
		
		boolean bl2 = var10000 <= 6 + var10002 * 9;
		float f = accessor.getProgress() == null ? 0.0F : accessor.getProgress().getProgressBarPercentage();
		int j = MathHelper.floor(f * (float) accessor.getWidth());
		AdvancementObtainedStatus advancementObtainedStatus;
		AdvancementObtainedStatus advancementObtainedStatus2;
		AdvancementObtainedStatus advancementObtainedStatus3;
		if (f >= 1.0F) {
			j = accessor.getWidth() / 2;
			advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
			advancementObtainedStatus2 = AdvancementObtainedStatus.OBTAINED;
			advancementObtainedStatus3 = AdvancementObtainedStatus.OBTAINED;
		} else if (j < 2) {
			j = accessor.getWidth() / 2;
			advancementObtainedStatus = AdvancementObtainedStatus.UNOBTAINED;
			advancementObtainedStatus2 = AdvancementObtainedStatus.UNOBTAINED;
			advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
		} else if (j > accessor.getWidth() - 2) {
			j = accessor.getWidth() / 2;
			advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
			advancementObtainedStatus2 = AdvancementObtainedStatus.OBTAINED;
			advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
		} else {
			advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
			advancementObtainedStatus2 = AdvancementObtainedStatus.UNOBTAINED;
			advancementObtainedStatus3 = AdvancementObtainedStatus.UNOBTAINED;
		}
		
		int k = accessor.getWidth() - j;
		RenderSystem.setShaderTexture(0, VANILLA_WIDGETS_TEXTURE);
		
		RenderSystem.enableBlend();
		int l = originY + accessor.getY();
		int m;
		if (bl) {
			m = originX + accessor.getX() - accessor.getX() + 26 + 6;
		} else {
			m = originX + accessor.getX();
		}
		
		int n = 32 + accessor.getDescription().size() * 9;
		if (!accessor.getDescription().isEmpty()) {
			if (bl2) {
				drawNineSlicedTexture(matrices, m, l + 26 - n, accessor.getWidth(), n, 10, 200, 26, 0, 52);
			} else {
				drawNineSlicedTexture(matrices, m, l, accessor.getWidth(), n, 10, 200, 26, 0, 52);
			}
		}
		
		drawTexture(matrices, m, l, 0, advancementObtainedStatus.getSpriteIndex() * 26, j, 26);
		drawTexture(matrices, m + j, l, 200 - k, advancementObtainedStatus2.getSpriteIndex() * 26, k, 26);
		
		@Nullable FrameWrapper frameWrapper = AdvancementFrameDataLoader.get(accessor.getAdvancement().getId());
		if (frameWrapper == null) {
			drawTexture(matrices, originX + accessor.getX() + 3, originY + accessor.getY(), accessor.getDisplay().getFrame().getTextureV(), 128 + advancementObtainedStatus3.getSpriteIndex() * 26, 26, 26);
		} else {
			RenderSystem.setShaderTexture(0, frameWrapper.getTextureSheet());
			drawTexture(matrices, originX + accessor.getX() + 3, originY + accessor.getY(), frameWrapper.getTextureV(), frameWrapper.getTextureU() + advancementObtainedStatus3.getSpriteIndex() * 26, 26, 26);
			RenderSystem.setShaderTexture(0, VANILLA_WIDGETS_TEXTURE);
		}
		
		if (bl) {
			textRenderer.drawWithShadow(matrices, accessor.getTitle(), (float) (m + 5), (float) (originY + accessor.getY() + 9), -1);
			if (string != null) {
				textRenderer.drawWithShadow(matrices, string, (float) (originX + accessor.getX() - i), (float) (originY + accessor.getY() + 9), -1);
			}
		} else {
			textRenderer.drawWithShadow(matrices, accessor.getTitle(), (float) (originX + accessor.getX() + 32), (float) (originY + accessor.getY() + 9), -1);
			if (string != null) {
				textRenderer.drawWithShadow(matrices, string, (float) (originX + accessor.getX() + accessor.getWidth() - i - 5), (float) (originY + accessor.getY() + 9), -1);
			}
		}
		
		float var10003;
		int o;
		int var10004;
		TextRenderer var21;
		OrderedText var22;
		if (bl2) {
			for (o = 0; o < accessor.getDescription().size(); ++o) {
				var21 = textRenderer;
				var22 = accessor.getDescription().get(o);
				var10003 = (float) (m + 5);
				var10004 = l + 26 - n + 7;
				Objects.requireNonNull(textRenderer);
				var21.draw(matrices, var22, var10003, (float) (var10004 + o * 9), -5592406);
			}
		} else {
			for (o = 0; o < accessor.getDescription().size(); ++o) {
				var21 = textRenderer;
				var22 = accessor.getDescription().get(o);
				var10003 = (float) (m + 5);
				var10004 = originY + accessor.getY() + 9 + 17;
				Objects.requireNonNull(textRenderer);
				var21.draw(matrices, var22, var10003, (float) (var10004 + o * 9), -5592406);
			}
		}
		
		MinecraftClient.getInstance().getItemRenderer().renderInGui(matrices, accessor.getDisplay().getIcon(), originX + accessor.getX() + 8, originY + accessor.getY() + 5);
	}
	
}
