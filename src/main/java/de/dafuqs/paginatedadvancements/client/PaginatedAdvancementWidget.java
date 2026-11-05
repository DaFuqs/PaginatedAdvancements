package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.frames.*;
import de.dafuqs.paginatedadvancements.mixin.*;
import net.fabricmc.api.*;
import net.minecraft.advancements.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.advancements.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import org.jspecify.annotations.*;

import java.util.*;

@Environment(EnvType.CLIENT)
public class PaginatedAdvancementWidget extends AdvancementWidget {
	
	private static final Identifier TITLE_BOX_TEXTURE = Identifier.withDefaultNamespace("advancements/title_box");
	protected List<FormattedCharSequence> description;
	
	protected @Nullable FrameWrapper frameWrapper;
	private final Minecraft client;
	private int debugScrollAmount;
	
	public PaginatedAdvancementWidget(Minecraft client, AdvancementNode placedAdvancement, DisplayInfo display) {
		super(client, placedAdvancement, display);
		this.client = client;
		
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		this.frameWrapper = AdvancementFrameDataLoader.get(accessor.getAdvancementNode().holder().id());
		
		if (this.frameWrapper != null) {
			int titleWidth = Math.max(accessor.getTitleLines().stream().mapToInt(client.font::width).max().orElse(0), 80);
			int descriptionWidth = 29 + titleWidth + this.getMaxProgressWidth();
			this.description = Language.getInstance().getVisualOrder(accessor.invokeFindOptimalLines(ComponentUtils.mergeStyles(display.description().copy(), frameWrapper.getTitleStyle()), descriptionWidth));
		} else {
			this.description = accessor.getDescription();
		}
	}
	
	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor context, int x, int y) {
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		DisplayInfo display = accessor.getDisplay();
		AdvancementProgress progress = accessor.getProgress();
		
		if (!display.hidden() || progress != null && progress.isDone()) {
			AdvancementWidgetType frameStatus = progress != null && progress.getPercent() >= 1.0F ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;
			FrameWrapper customFrame = this.frameWrapper;
			Identifier frameTexture = customFrame == null ? frameStatus.frameSprite(display.type()) : customFrame.getTexture(frameStatus, display.type());
			context.blitSprite(RenderPipelines.GUI_TEXTURED, frameTexture, x + accessor.getX() + 3, y + accessor.getY(), 26, 26);
			context.fakeItem(accessor.getIcon(), x + accessor.getX() + 8 + (customFrame == null ? 0 : customFrame.getItemOffsetX()), y + accessor.getY() + 5 + (customFrame == null ? 0 : customFrame.getItemOffsetY()));
		}
		
		for (AdvancementWidget advancementWidget : accessor.getChildren()) {
			advancementWidget.extractRenderState(context, x, y);
		}
	}
	
	@Override
	public void extractHover(@NonNull GuiGraphicsExtractor context, int originX, int originY, float alpha, int screenX, int screenY, int screenWidth) {
		AdvancementWidgetAccessor accessor = (AdvancementWidgetAccessor) this;
		DisplayInfo display = accessor.getDisplay();
		int widgetX = originX + accessor.getX();
		int widgetY = originY + accessor.getY();
		int tooltipWidth = accessor.getWidth();
		boolean renderToLeft = screenX + widgetX + tooltipWidth + 26 >= screenWidth;
		AdvancementProgress progress = accessor.getProgress();
		Component progressText = progress == null ? null : progress.getProgressText();
		String progressLabel = progressText == null ? null : progressText.getString();
		int progressTextWidth = progressText == null ? 0 : this.client.font.width(progressText);
		int titleBarHeight = accessor.getTitleLines().size() * 9 + 17;
		int titleTop = widgetY + (26 - titleBarHeight) / 2;
		int titleBottom = titleTop + titleBarHeight;
		int descriptionHeight = 6 + description.size() * 9;
		int availableHeight = client.getWindow().getGuiScaledHeight() - screenY - PaginatedAdvancementScreen.BORDER_PADDING - 26;
		boolean renderDescriptionAbove = titleBottom + descriptionHeight >= availableHeight;
		float progressPercent = progress == null ? 0.0F : progress.getPercent();
		int leftBoxWidth = Mth.floor(progressPercent * tooltipWidth);
		AdvancementWidgetType leftBoxStatus = AdvancementWidgetType.OBTAINED;
		AdvancementWidgetType rightBoxStatus = AdvancementWidgetType.UNOBTAINED;
		AdvancementWidgetType frameStatus = progressPercent >= 1.0F ? AdvancementWidgetType.OBTAINED : AdvancementWidgetType.UNOBTAINED;
		if (progressPercent >= 1.0F) {
			leftBoxWidth = tooltipWidth / 2;
			rightBoxStatus = AdvancementWidgetType.OBTAINED;
		} else if (leftBoxWidth < 2) {
			leftBoxWidth = tooltipWidth / 2;
			leftBoxStatus = AdvancementWidgetType.UNOBTAINED;
		} else if (leftBoxWidth > tooltipWidth - 2) {
			leftBoxWidth = tooltipWidth / 2;
			rightBoxStatus = AdvancementWidgetType.OBTAINED;
		}
		
		int rightBoxWidth = tooltipWidth - leftBoxWidth;
		int tooltipX = renderToLeft ? widgetX - tooltipWidth + 32 : widgetX;
		if (!this.description.isEmpty()) {
			int tooltipTop = renderDescriptionAbove ? titleTop - descriptionHeight : titleTop;
			context.blitSprite(RenderPipelines.GUI_TEXTURED, TITLE_BOX_TEXTURE, tooltipX, tooltipTop, tooltipWidth, titleBarHeight + descriptionHeight);
		}
		
		if (leftBoxStatus == rightBoxStatus) {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, leftBoxStatus.boxSprite(), tooltipX, titleTop, tooltipWidth, titleBarHeight);
		} else {
			context.blitSprite(RenderPipelines.GUI_TEXTURED, leftBoxStatus.boxSprite(), 200, titleBarHeight, 0, 0, tooltipX, titleTop, leftBoxWidth, titleBarHeight);
			context.blitSprite(RenderPipelines.GUI_TEXTURED, rightBoxStatus.boxSprite(), 200, titleBarHeight, 200 - rightBoxWidth, 0, tooltipX + leftBoxWidth, titleTop, rightBoxWidth, titleBarHeight);
		}
		
		Identifier frameTexture = frameWrapper == null ? frameStatus.frameSprite(display.type()) : frameWrapper.getTexture(frameStatus, display.type());
		context.blitSprite(RenderPipelines.GUI_TEXTURED, frameTexture, widgetX + 3, widgetY, 26, 26);
		
		this.extractMultilineText(context, accessor.getTitleLines(), tooltipX + (renderToLeft ? 5 : 32), titleTop + 9, -1);
		if (progressLabel != null) {
			int progressTextX = renderToLeft ? widgetX - progressTextWidth : widgetX + tooltipWidth - progressTextWidth - 5;
			context.text(client.font, progressLabel, progressTextX, titleTop + 9, -1);
		}
		
		int descriptionY = renderDescriptionAbove ? titleTop - description.size() * 9 + 1 : titleBottom;
		for (int lineIndex = 0; lineIndex < description.size(); lineIndex++) {
			context.text(client.font, description.get(lineIndex), tooltipX + 5, descriptionY + lineIndex * 9, 0xFFAAAAAA, false);
		}
		context.fakeItem(accessor.getIcon(), widgetX + 8 + (frameWrapper == null ? 0 : frameWrapper.getItemOffsetX()), widgetY + 5 + (frameWrapper == null ? 0 : frameWrapper.getItemOffsetY()));
	}
	
	public int getDebugScrollAmount() {
		return debugScrollAmount;
	}
	
	public void setDebugScrollAmount(int debugScrollAmount) {
		this.debugScrollAmount = debugScrollAmount;
	}
}
