package de.dafuqs.paginatedadvancements.client;

import com.mojang.blaze3d.systems.*;
import de.dafuqs.paginatedadvancements.frames.*;
import de.dafuqs.paginatedadvancements.mixin.*;
import net.fabricmc.api.*;
import net.minecraft.advancement.*;
import net.minecraft.client.*;
import net.minecraft.client.font.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;

import java.util.*;

@Environment(EnvType.CLIENT)
public class PaginatedAdvancementWidget extends AdvancementWidget {
	
	private static final Identifier TITLE_BOX_TEXTURE = new Identifier("advancements/title_box");
	protected List<OrderedText> description;
	
	protected @Nullable FrameWrapper frameWrapper;
	private final MinecraftClient client;
	private int debugScrollAmount;
	
	public PaginatedAdvancementWidget(AdvancementTab tab, MinecraftClient client, PlacedAdvancement placedAdvancement, AdvancementDisplay display) {
		super(tab, client, placedAdvancement, display);
		this.client = client;
		
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		this.frameWrapper = AdvancementFrameDataLoader.get(accessor.getAdvancement().getAdvancementEntry().id());
		int requirementCount = placedAdvancement.getAdvancement().requirements().getLength();
		int k = requirementCount > 1 ? client.textRenderer.getWidth("  ") + client.textRenderer.getWidth("0") * String.valueOf(requirementCount).length() * 2 + client.textRenderer.getWidth("/") : 0;
		OrderedText title = Language.getInstance().reorder(client.textRenderer.trimToWidth(display.getTitle(), 163));
		int l = 29 + client.textRenderer.getWidth(title) + k;
		if (this.frameWrapper != null) {
			this.description = Language.getInstance().reorder(accessor.invokeWrapDescription(Texts.setStyleIfAbsent(display.getDescription().copy(), Style.EMPTY.withColor(frameWrapper.getTitleFormat())), l));
		} else {
			this.description = accessor.getDescription();
		}
		OrderedText orderedText;
		for (Iterator<OrderedText> it = this.description.iterator(); it.hasNext(); l = Math.max(l, client.textRenderer.getWidth(orderedText))) {
			orderedText = it.next();
		}
		
		setDebugScrollAmount(0);
	}
	
	@Override
	public void renderWidgets(DrawContext context, int x, int y) {
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		
		if (!accessor.getDisplay().isHidden() || accessor.getProgress() != null && accessor.getProgress().isDone()) {
			float f = accessor.getProgress() == null ? 0.0F : accessor.getProgress().getProgressBarPercentage();
			AdvancementObtainedStatus advancementObtainedStatus;
			if (f >= 1.0F) {
				advancementObtainedStatus = AdvancementObtainedStatus.OBTAINED;
			} else {
				advancementObtainedStatus = AdvancementObtainedStatus.UNOBTAINED;
			}
			
			Identifier advancementID = accessor.getAdvancement().getAdvancementEntry().id();
			@Nullable FrameWrapper frameWrapper = AdvancementFrameDataLoader.get(advancementID);
			if (frameWrapper != null) {
				context.drawGuiTexture(frameWrapper.getTexture(advancementObtainedStatus, accessor.getDisplay().getFrame()), x + accessor.getX() + 3, y + accessor.getY(), 26, 26);
				context.drawItemWithoutEntity(accessor.getDisplay().getIcon(), x + accessor.getX() + 8 + frameWrapper.getItemOffsetX(), y + accessor.getY() + 5 + frameWrapper.getItemOffsetY());
			} else {
				context.drawGuiTexture(advancementObtainedStatus.getFrameTexture(accessor.getDisplay().getFrame()), x + accessor.getX() + 3, y + accessor.getY(), 26, 26);
				context.drawItemWithoutEntity(accessor.getDisplay().getIcon(), x + accessor.getX() + 8, y + accessor.getY() + 5);
			}
		}
		
		for (AdvancementWidget advancementWidget : accessor.getChildren()) {
			advancementWidget.renderWidgets(context, x, y);
		}
	}
	
	@Override
	public void drawTooltip(DrawContext context, int originX, int originY, float alpha, int x, int y) {
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		TextRenderer textRenderer = client.textRenderer;
		
		boolean shouldRenderToTheLeft = x + originX + accessor.getX() + accessor.getWidth() + 26 >= accessor.getTab().getScreen().width;
		AdvancementProgress progress = accessor.getProgress();
		Text progressText = progress == null ? null : progress.getProgressBarFraction();
		String string = progressText == null ? null : progressText.getString();
		int i = progressText == null ? 0 : this.client.textRenderer.getWidth(progressText);
		int var10000 = 113 - originY - accessor.getY() - 26;
		int var10002 = description.size();
		
		boolean bl2 = var10000 <= 6 + var10002 * 9;
		float f = accessor.getProgress() == null ? 0.0F : progress.getProgressBarPercentage();
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
		RenderSystem.enableBlend();
		int l = originY + accessor.getY();
		int startX;
		if (shouldRenderToTheLeft) {
			startX = originX + accessor.getX() - accessor.getWidth() + 26 + 6;
		} else {
			startX = originX + accessor.getX();
		}
		
		int n = 32 + this.description.size() * 9;
		if (!this.description.isEmpty()) {
			if (bl2) {
				context.drawGuiTexture(TITLE_BOX_TEXTURE, startX, l + 26 - n, accessor.getWidth(), n);
			} else {
				context.drawGuiTexture(TITLE_BOX_TEXTURE, startX, l, accessor.getWidth(), n);
			}
		}
		
		context.drawGuiTexture(advancementObtainedStatus.getBoxTexture(), 200, 26, 0, 0, startX, l, j, 26);
		context.drawGuiTexture(advancementObtainedStatus2.getBoxTexture(), 200, 26, 200 - k, 0, startX + j, l, k, 26);
		
		if (this.frameWrapper != null) {
			context.drawGuiTexture(this.frameWrapper.getTexture(advancementObtainedStatus3, accessor.getDisplay().getFrame()), originX + accessor.getX() + 3, originY + accessor.getY(), 26, 26);
		} else {
			context.drawGuiTexture(advancementObtainedStatus3.getFrameTexture(accessor.getDisplay().getFrame()), originX + accessor.getX() + 3, originY + accessor.getY(), 26, 26);
		}
		
		if (shouldRenderToTheLeft) {
			context.drawTextWithShadow(textRenderer, accessor.getTitle(), startX + 5, originY + accessor.getY() + 9, -1);
			if (string != null) {
				context.drawTextWithShadow(textRenderer, string, originX + accessor.getX() - i, originY + accessor.getY() + 9, -1);
			}
		} else {
			context.drawTextWithShadow(textRenderer, accessor.getTitle(), originX + accessor.getX() + 32, originY + accessor.getY() + 9, -1);
			if (string != null) {
				context.drawTextWithShadow(textRenderer, string, originX + accessor.getX() + accessor.getWidth() - i - 5, originY + accessor.getY() + 9, -1);
			}
		}
		
		int o;
		OrderedText orderedDescription;
		if (bl2) {
			for (o = 0; o < description.size(); ++o) {
				orderedDescription = description.get(o);
				context.drawText(textRenderer, orderedDescription, startX + 5, l + 26 - n + 7 + o * 9, -5592406, false);
			}
		} else {
			for (o = 0; o < description.size(); ++o) {
				orderedDescription = description.get(o);
				context.drawText(textRenderer, orderedDescription, startX + 5, originY + accessor.getY() + 9 + 17 + o * 9, -5592406, false);
			}
		}
		
		if (frameWrapper == null) {
			context.drawItemWithoutEntity(accessor.getDisplay().getIcon(), originX + accessor.getX() + 8, originY + accessor.getY() + 5);
		} else {
			context.drawItemWithoutEntity(accessor.getDisplay().getIcon(), originX + accessor.getX() + 8 + frameWrapper.getItemOffsetX(), originY + accessor.getY() + 5 + frameWrapper.getItemOffsetY());
		}
	}
	
	public int getDebugScrollAmount() {
		return debugScrollAmount;
	}
	
	public void setDebugScrollAmount(int debugScrollAmount) {
		this.debugScrollAmount = debugScrollAmount;
	}
}
