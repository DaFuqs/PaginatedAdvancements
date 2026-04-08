package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.frames.AdvancementFrameDataLoader;
import de.dafuqs.paginatedadvancements.frames.FrameWrapper;
import de.dafuqs.paginatedadvancements.mixin.AdvancementWidgetAccessor;
import net.fabricmc.api.*;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementWidgetType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class PaginatedAdvancementWidget extends AdvancementWidget {
	
	private static final Identifier TITLE_BOX_TEXTURE = Identifier.withDefaultNamespace("advancements/title_box");
	protected List<FormattedCharSequence> description;
	
	protected @Nullable FrameWrapper frameWrapper;
	private final Minecraft client;
	private int debugScrollAmount;
	
	public PaginatedAdvancementWidget(AdvancementTab tab, Minecraft client, AdvancementNode placedAdvancement, DisplayInfo display) {
		super(tab, client, placedAdvancement, display);
		this.client = client;
		
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		this.frameWrapper = AdvancementFrameDataLoader.get(accessor.getAdvancementNode().holder().id());
		
		int i = Math.max(accessor.getTitleLines().stream().mapToInt(client.font::width).max().orElse(0), 80);
		int j = this.getMaxProgressWidth();
		int l = 29 + i + j;
		
		if (this.frameWrapper != null) {
			this.description = Language.getInstance().getVisualOrder(accessor.invokeFindOptimalLines(ComponentUtils.mergeStyles(display.getDescription().copy(), frameWrapper.getTitleStyle()), l));
		} else {
			this.description = accessor.getDescription();
		}
		FormattedCharSequence orderedText;
		for (Iterator<FormattedCharSequence> it = this.description.iterator(); it.hasNext(); l = Math.max(l, client.font.width(orderedText))) {
			orderedText = it.next();
		}
		
		setDebugScrollAmount(0);
	}
	
	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor context, int x, int y) {
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		
		if (!accessor.getDisplay().isHidden() || accessor.getProgress() != null && accessor.getProgress().isDone()) {
			float f = accessor.getProgress() == null ? 0.0F : accessor.getProgress().getPercent();
			AdvancementWidgetType advancementObtainedStatus;
			if (f >= 1.0F) {
				advancementObtainedStatus = AdvancementWidgetType.OBTAINED;
			} else {
				advancementObtainedStatus = AdvancementWidgetType.UNOBTAINED;
			}
			
			Identifier advancementID = accessor.getAdvancementNode().holder().id();
			@Nullable FrameWrapper frameWrapper = AdvancementFrameDataLoader.get(advancementID);
			if (frameWrapper != null) {
				context.blitSprite(RenderPipelines.GUI_TEXTURED, frameWrapper.getTexture(advancementObtainedStatus, accessor.getDisplay().getType()), x + accessor.getX() + 3, y + accessor.getY(), 26, 26);
				context.fakeItem(accessor.getDisplay().getIcon().create(), x + accessor.getX() + 8 + frameWrapper.getItemOffsetX(), y + accessor.getY() + 5 + frameWrapper.getItemOffsetY());
			} else {
				context.blitSprite(RenderPipelines.GUI_TEXTURED, advancementObtainedStatus.frameSprite(accessor.getDisplay().getType()), x + accessor.getX() + 3, y + accessor.getY(), 26, 26);
				context.fakeItem(accessor.getDisplay().getIcon().create(), x + accessor.getX() + 8, y + accessor.getY() + 5);
			}
		}
		
		for (AdvancementWidget advancementWidget : accessor.getChildren()) {
			advancementWidget.extractRenderState(context, x, y);
		}
	}
	
	@Override
	public void extractHover(@NonNull GuiGraphicsExtractor context, int originX, int originY, float alpha, int x, int y) {
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		Font textRenderer = client.font;
		
		boolean shouldRenderToTheLeft = x + originX + accessor.getX() + accessor.getWidth() + 26 >= accessor.getTab().getScreen().width;
		AdvancementProgress progress = accessor.getProgress();
		Component progressText = progress == null ? null : progress.getProgressText();
		String string = progressText == null ? null : progressText.getString();
		int i = progressText == null ? 0 : this.client.font.width(progressText);
		int var10000 = 113 - originY - accessor.getY() - 26;
		int var10002 = description.size();
		
		boolean bl2 = var10000 <= 6 + var10002 * 9;
		float f = progress == null ? 0.0F : progress.getPercent();
		int j = Mth.floor(f * (float) accessor.getWidth());
		AdvancementWidgetType advancementObtainedStatus;
		AdvancementWidgetType advancementObtainedStatus2;
		AdvancementWidgetType advancementObtainedStatus3;
		if (f >= 1.0F) {
			j = accessor.getWidth() / 2;
			advancementObtainedStatus = AdvancementWidgetType.OBTAINED;
			advancementObtainedStatus2 = AdvancementWidgetType.OBTAINED;
			advancementObtainedStatus3 = AdvancementWidgetType.OBTAINED;
		} else if (j < 2) {
			j = accessor.getWidth() / 2;
			advancementObtainedStatus = AdvancementWidgetType.UNOBTAINED;
			advancementObtainedStatus2 = AdvancementWidgetType.UNOBTAINED;
			advancementObtainedStatus3 = AdvancementWidgetType.UNOBTAINED;
		} else if (j > accessor.getWidth() - 2) {
			j = accessor.getWidth() / 2;
			advancementObtainedStatus = AdvancementWidgetType.OBTAINED;
			advancementObtainedStatus2 = AdvancementWidgetType.OBTAINED;
			advancementObtainedStatus3 = AdvancementWidgetType.UNOBTAINED;
		} else {
			advancementObtainedStatus = AdvancementWidgetType.OBTAINED;
			advancementObtainedStatus2 = AdvancementWidgetType.UNOBTAINED;
			advancementObtainedStatus3 = AdvancementWidgetType.UNOBTAINED;
		}
		
		int k = accessor.getWidth() - j;
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
				context.blitSprite(RenderPipelines.GUI_TEXTURED, TITLE_BOX_TEXTURE, startX, l + 26 - n, accessor.getWidth(), n);
			} else {
				context.blitSprite(RenderPipelines.GUI_TEXTURED, TITLE_BOX_TEXTURE, startX, l, accessor.getWidth(), n);
			}
		}
		
		context.blitSprite(RenderPipelines.GUI_TEXTURED, advancementObtainedStatus.boxSprite(), 200, 26, 0, 0, startX, l, j, 26);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, advancementObtainedStatus2.boxSprite(), 200, 26, 200 - k, 0, startX + j, l, k, 26);
		
		if (this.frameWrapper != null) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, this.frameWrapper.getTexture(advancementObtainedStatus3, accessor.getDisplay().getType()), originX + accessor.getX() + 3, originY + accessor.getY(), 26, 26);
		} else {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, advancementObtainedStatus3.frameSprite(accessor.getDisplay().getType()), originX + accessor.getX() + 3, originY + accessor.getY(), 26, 26);
		}
		
		if (shouldRenderToTheLeft) {
			this.extractMultilineText(context, accessor.getTitleLines(), startX + 5, originY + accessor.getY() + 9, -1);
			if (string != null) {
				context.text(textRenderer, string, originX + accessor.getX() - i, originY + accessor.getY() + 9, -1);
			}
		} else {
			this.extractMultilineText(context, accessor.getTitleLines(), originX + accessor.getX() + 32, originY + accessor.getY() + 9, -1);
			if (string != null) {
				context.text(textRenderer, string, originX + accessor.getX() + accessor.getWidth() - i - 5, originY + accessor.getY() + 9, -1);
			}
		}
		
		int o;
		FormattedCharSequence orderedDescription;
		if (bl2) {
			for (o = 0; o < description.size(); ++o) {
				orderedDescription = description.get(o);
				context.text(textRenderer, orderedDescription, startX + 5, l + 26 - n + 7 + o * 9, -5592406, false);
			}
		} else {
			for (o = 0; o < description.size(); ++o) {
				orderedDescription = description.get(o);
				context.text(textRenderer, orderedDescription, startX + 5, originY + accessor.getY() + 9 + 17 + o * 9, -5592406, false);
			}
		}
		
		if (frameWrapper == null) {
			context.fakeItem(accessor.getDisplay().getIcon().create(), originX + accessor.getX() + 8, originY + accessor.getY() + 5);
		} else {
			context.fakeItem(accessor.getDisplay().getIcon().create(), originX + accessor.getX() + 8 + frameWrapper.getItemOffsetX(), originY + accessor.getY() + 5 + frameWrapper.getItemOffsetY());
		}
	}
	
	public int getDebugScrollAmount() {
		return debugScrollAmount;
	}
	
	public void setDebugScrollAmount(int debugScrollAmount) {
		this.debugScrollAmount = debugScrollAmount;
	}
}
