package de.dafuqs.paginatedadvancements.client;

import com.google.common.collect.*;
import de.dafuqs.paginatedadvancements.*;
import de.dafuqs.paginatedadvancements.mixin.*;
import net.minecraft.*;
import net.minecraft.advancements.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.advancements.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static de.dafuqs.paginatedadvancements.client.PaginatedAdvancementScreen.*;

public class PaginatedAdvancementTab extends AdvancementTab {
	
	private final Minecraft client;
	private final PaginatedAdvancementScreen screen;
	private final int index;
	private int pinnedIndex;
	private final AdvancementNode root;
	private final DisplayInfo display;
	private final ItemStack icon;
	private final Component title;
	private final AdvancementWidget rootWidget;
	private final Map<AdvancementHolder, AdvancementWidget> widgets = Maps.newLinkedHashMap();
	
	private double originX;
	private double originY;
	private int minPanX = 2147483647;
	private int minPanY = 2147483647;
	private int maxPanX = -2147483648;
	private int maxPanY = -2147483648;
	private float alpha;
	private boolean initialized;
	
	private @Nullable AdvancementWidget hoveredWidget;
	
	public PaginatedAdvancementTab(Minecraft client, PaginatedAdvancementScreen screen, int index, int pinnedIndex, AdvancementNode root, DisplayInfo display) {
		super(client, screen, AdvancementTabType.ABOVE, index, root, display);
		this.client = client;
		this.screen = screen;
		this.index = index;
		this.pinnedIndex = pinnedIndex;
		this.root = root;
		this.display = display;
		this.icon = display.getIcon();
		this.title = display.getTitle();
		this.rootWidget = new PaginatedAdvancementWidget(this, client, root, display);
		this.addWidget(this.rootWidget, root.holder());
	}
	
	public AdvancementTabType getType() {
		return AdvancementTabType.ABOVE;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public AdvancementNode getRootNode() {
		return this.root;
	}
	
	public Component getTitle() {
		return this.title;
	}
	
	public DisplayInfo getDisplay() {
		return this.display;
	}
	
	public void drawBackground(GuiGraphics context, int x, int y, boolean selected, int atIndex) {
		PaginatedAdvancementTabType.drawBackground(context, x, y, selected, atIndex);
	}
	
	public void drawIcon(GuiGraphics context, int x, int y, int index) {
		PaginatedAdvancementTabType.drawIcon(context, x, y, index, this.icon);
	}
	
	public void drawPinnedBackground(GuiGraphics context, int x, int y, boolean selected, int maxPinnedIndex) {
		if (this.pinnedIndex <= maxPinnedIndex) {
			PinnedAdvancementTabType.drawBackground(context, x, y, selected, this.pinnedIndex);
		}
	}
	
	public void drawPinnedIcon(GuiGraphics context, int x, int y, int maxPinnedIndex) {
		if (this.pinnedIndex <= maxPinnedIndex) {
			PinnedAdvancementTabType.drawIcon(context, x, y, this.pinnedIndex, this.icon);
		}
	}
	
	public void render(GuiGraphics context, int startX, int startY, int endX, int endY) {
		startX = startX + 9;
		startY = startY + 18;
		int advancementTreeWindowWidth = endX - startX + 32;
		int advancementTreeWindowHeight = endY - startY + 61;
		
		if (!this.initialized) {
			// the center of the advancement tree render at the start
			this.originX = (double) ((((advancementTreeWindowWidth) / 2)) - (this.maxPanX + this.minPanX) / 2) - 22;
			this.originY = (double) ((((advancementTreeWindowHeight) / 2)) - (this.maxPanY + this.minPanY) / 2) - 32;
			this.initialized = true;
		}
		
		context.enableScissor(startX, startY, advancementTreeWindowWidth, advancementTreeWindowHeight);
		context.pose().pushMatrix();
		context.pose().translate(startX, startY);
		ResourceLocation identifier = this.display.getBackground().map(ClientAsset::texturePath).orElse(TextureManager.INTENTIONAL_MISSING_TEXTURE);
		
		int i = Mth.floor(this.originX);
		int j = Mth.floor(this.originY);
		int k = i % 16;
		int l = j % 16;
		
		int textureCountX = (advancementTreeWindowWidth) / 16 + 1;
		int textureCountY = (advancementTreeWindowHeight) / 16 + 2;
		for (int m = -1; m < textureCountX; ++m) {
			for (int n = -1; n < textureCountY; ++n) {
				context.blit(RenderPipelines.GUI_TEXTURED, identifier, k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
			}
		}
		
		this.rootWidget.drawConnectivity(context, i, j, true);
		this.rootWidget.drawConnectivity(context, i, j, false);
		this.rootWidget.draw(context, i, j);
		
		context.pose().popMatrix();
		context.disableScissor();
	}
	
	public void drawWidgetTooltip(GuiGraphics context, int mouseX, int mouseY, int startX, int startY, int endXWindow, int endY) {
		context.pose().pushMatrix();
		context.pose().translate(0.0F, 0.0F);
		
		// tinting the background slightly darker
		// (this is the vanilla default, but able to be disabled via config)
		if (PaginatedAdvancementsClient.CONFIG.FadeOutBackgroundOnAdvancementHover) {
			context.fill(0, 0, endXWindow - startX - 18, endY - startY - 26, Mth.floor(this.alpha * 255.0F) << 24);
		}
		
		boolean hoversWidget = false;
		int i = Mth.floor(this.originX);
		int j = Mth.floor(this.originY);
		if (mouseX > 0 && mouseX < endXWindow - startX - 10 && mouseY > 0 && mouseY < endY - startY) {
			for (AdvancementWidget advancementWidget : this.widgets.values()) {
				if (advancementWidget.isMouseOver(i, j, mouseX, mouseY)) {
					hoversWidget = true;
					advancementWidget.drawHover(context, i, j, this.alpha, startX, startY);
					
					this.hoveredWidget = advancementWidget;
					
					break;
				}
			}
		}
		
		context.pose().popMatrix();
		if (hoversWidget) {
			this.alpha = Mth.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
		} else {
			this.hoveredWidget = null;
			this.alpha = Mth.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
		}
	}
	
	public void drawDebugInfo(GuiGraphics context, int startX, int startY, int endX, int endY) {
		if (this.hoveredWidget != null) {
			AdvancementWidgetAccessor advancementWidgetAccessor = (AdvancementWidgetAccessor) this.hoveredWidget;
			AdvancementProgress progress = advancementWidgetAccessor.getProgress();
			
			startX = startX - 36;
			endX = endX - 46;
			endY = endY - 60;
			startY = startY - 72;
			
			List<MutableComponent> requirements = getRequirements(startX, endX - 10, advancementWidgetAccessor.getAdvancementNode().advancement(), progress);
			
			boolean overflow = false;
			int displayedRequirementLines;
			if (!hasShiftDown()) {
				overflow = requirements.size() > PaginatedAdvancementsClient.CONFIG.MaxCriterionEntries;
				displayedRequirementLines = Math.min(requirements.size(), PaginatedAdvancementsClient.CONFIG.MaxCriterionEntries);
			} else {
				displayedRequirementLines = requirements.size();
			}
			
			startY = Math.max(startY, endY - Math.max(18, 8 + 10 * displayedRequirementLines) - (PaginatedAdvancementsClient.CONFIG.ShowAdvancementIDInDebugTooltip ? 10 : 0));
			
			drawDebugFrame(context, startX, startY, endX, endY);
			
			// the title
			int requirementY = startY + 15;
			if (PaginatedAdvancementsClient.CONFIG.ShowAdvancementIDInDebugTooltip) {
				Component idText = Component.literal("ID: " + advancementWidgetAccessor.getAdvancementNode().holder().id().toString() + " ").append(Component.translatable("text.paginated_advancements.copy_to_clipboard"));
				context.drawString(this.client.font, idText, startX + 5, startY + 5, 0xFF_FFFFFF, true);
			} else {
				requirementY = startY + 5;
			}
			
			// the requirements
			if (overflow) {
				drawRequirementsWithOverflow(context, startX + 5, requirementY, endX - 5, endY, requirements, displayedRequirementLines);
			} else {
				drawRequirements(context, startX + 5, requirementY, endX - 5, endY, requirements);
			}
		}
	}
	
	private List<MutableComponent> getRequirements(int startX, int endX, Advancement advancement, AdvancementProgress progress) {
		Iterable<String> obtainedCriteria = progress == null ? List.of() : progress.getCompletedCriteria();
		List<List<String>> requirements = advancement.requirements().requirements();
		
		List<MutableComponent> requirementsDone = new ArrayList<>();
		List<MutableComponent> requirementsLeft = new ArrayList<>();
		
		for (List<String> requirementGroup : requirements) {
			List<MutableComponent> lines = new ArrayList<>();
			lines.add(Component.translatable("text.paginated_advancements.group").withStyle(ChatFormatting.DARK_RED));
			boolean anyDone = false;
			for (String requirementString : requirementGroup) {
				ChatFormatting formatting = ChatFormatting.DARK_RED;
				for (String s : obtainedCriteria) {
					if (s.equals(requirementString)) {
						formatting = ChatFormatting.DARK_GREEN;
						anyDone = true;
						break;
					}
				}
				int newWidth = client.font.width(lines.get(lines.size() - 1)) + client.font.width(requirementString);
				if (newWidth > endX - startX) {
					String indent = "";
					while (client.font.width(indent) < client.font.width(Component.translatable("text.paginated_advancements.group"))) {
						indent += " ";
					}
					lines.add(Component.literal(indent).withStyle(ChatFormatting.DARK_RED));
				}
				lines.get(lines.size() - 1).append(Component.literal(requirementString + " ").withStyle(formatting));
			}
			
			if (anyDone) {
				for (MutableComponent line : lines) {
					line.withStyle(ChatFormatting.DARK_GREEN);
					requirementsDone.add(line);
				}
			} else {
				requirementsLeft.addAll(lines);
			}
		}
		
		List<MutableComponent> combined = new ArrayList<>();
		combined.addAll(requirementsLeft);
		combined.addAll(requirementsDone);
		
		return combined;
	}
	
	protected void drawDebugFrame(GuiGraphics context, int startX, int startY, int endX, int endY) {
		context.pose().pushMatrix();
		
		int TOP_ELEMENT_HEIGHT = 15;
		
		// corners
		context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, startX, startY, 0, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT, 256, 256); // top left
		context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, endX - ELEMENT_WIDTH, startY, 237, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT, 256, 256); // top right
		context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, startX, endY - BOTTOM_ELEMENT_HEIGHT, 0, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT, 256, 256); // bottom left
		context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, endX - ELEMENT_WIDTH, endY - BOTTOM_ELEMENT_HEIGHT, 237, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT, 256, 256); // bottom right
		
		// left + right sides
		int maxTopHeightInOneDrawCall = 100;
		int middleHeight = endY - startY - TOP_ELEMENT_HEIGHT - BOTTOM_ELEMENT_HEIGHT;
		int currentY = startY + TOP_ELEMENT_HEIGHT;
		while (middleHeight > 0) {
			int currentDrawHeight = Math.min(middleHeight, maxTopHeightInOneDrawCall);
			
			context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, startX, currentY, 0, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, endX - ELEMENT_WIDTH, currentY, 237, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight, 256, 256);
			
			middleHeight -= currentDrawHeight;
			currentY += currentDrawHeight;
		}
		
		// top + bottom
		int maxTopWidthInOneDrawCall = 220;
		int middleWidth = endX - startX - ELEMENT_WIDTH - ELEMENT_WIDTH;
		int currentX = startX + ELEMENT_WIDTH;
		while (middleWidth > 0) {
			int currentDrawWidth = Math.min(middleWidth, maxTopWidthInOneDrawCall);
			
			context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, currentX, startY, ELEMENT_WIDTH, 0, currentDrawWidth, TOP_ELEMENT_HEIGHT, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, currentX, endY - BOTTOM_ELEMENT_HEIGHT, ELEMENT_WIDTH, 125, currentDrawWidth, BOTTOM_ELEMENT_HEIGHT, 256, 256);
			
			middleWidth -= currentDrawWidth;
			currentX += currentDrawWidth;
		}
		
		// center
		int centerStartX = startX + 6;
		int centerEndX = endX - 6;
		int centerStartY = startY + 3;
		int centerEndY = endY - 6;
		
		int drawStartY = centerStartY;
		int drawHeight = centerEndY - centerStartY;
		while (drawHeight > 0) {
			int drawStartX = centerStartX;
			int currentHeight = Math.min(drawHeight, 10);
			int drawWidth = centerEndX - centerStartX;
			while (drawWidth > 0) {
				int currentWidth = Math.min(200, drawWidth);
				context.blit(RenderPipelines.GUI_TEXTURED, PaginatedAdvancementScreen.WINDOW_TEXTURE, drawStartX, drawStartY, 4, 4, currentWidth, currentHeight, 256, 256);
				drawWidth -= currentWidth;
				drawStartX += currentWidth;
			}
			drawHeight -= currentHeight;
			drawStartY += currentHeight;
		}
		
		context.pose().popMatrix();
	}
	
	protected void drawRequirementsWithOverflow(GuiGraphics context, int startX, int startY, int endX, int endY, List<MutableComponent> requirements, int lines) {
		for (int i = 0; i < lines; i++) {
			if (i == lines - 1) {
				context.drawString(this.client.font, Component.translatable("text.paginated_advancements.expand_debug"), startX, startY, 0xff999999, false);
			} else {
				context.drawString(this.client.font, requirements.get(i), startX, startY, 0xff00ff00, false);
			}
			startY += 10;
		}
	}
	
	protected void drawRequirements(GuiGraphics context, int startX, int startY, int endX, int endY, List<MutableComponent> requirements) {
		int scrollAmount = 0;
		assert this.hoveredWidget != null;
		if (this.hoveredWidget instanceof PaginatedAdvancementWidget paginatedAdvancementWidget) {
			scrollAmount = paginatedAdvancementWidget.getDebugScrollAmount();
			// clamp scroll amount
			int maxLines = (endY - startY) / 10;
			scrollAmount = Math.max(0, Math.min(requirements.size() - maxLines, scrollAmount));
			paginatedAdvancementWidget.setDebugScrollAmount(scrollAmount);
		}
		
		if (scrollAmount > 0) {
			context.drawString(this.client.font, Component.translatable("text.paginated_advancements.scroll_debug"), startX, startY, 0xff_999999, false);
			scrollAmount += 1;
			startY += 10;
		}
		for (int i = scrollAmount; i < requirements.size(); i++) {
			if (startY + 10 >= endY) break;
			else if (startY + 20 >= endY && i + 1 != requirements.size()) {
				context.drawString(this.client.font, Component.translatable("text.paginated_advancements.scroll_debug"), startX, startY, 0xff_999999, false);
				break;
			}
			context.drawString(this.client.font, requirements.get(i), startX, startY, 0xff_00ff00, false);
			startY += 10;
		}
	}
	
	public boolean scrollDebug(int diff) {
		if (this.hoveredWidget != null && this.hoveredWidget instanceof PaginatedAdvancementWidget paginatedAdvancementWidget) {
			int value = paginatedAdvancementWidget.getDebugScrollAmount();
			paginatedAdvancementWidget.setDebugScrollAmount(value + diff);
			return true;
		}
		return false;
	}
	
	public int getPaginatedDisplayedPage(int maxDisplayedTabs) {
		return this.index / maxDisplayedTabs;
	}
	
	public int getPaginatedDisplayedPosition(int maxDisplayedTabs, int currentPage) {
		return 1 + this.index - maxDisplayedTabs * currentPage; // +1 because pos 0 is taken by the back button
	}
	
	public boolean isClickOnTab(int screenX, int screenY, double mouseX, double mouseY, boolean paginated, int maxDisplayedTabs, int currentPage) {
		if (paginated) {
			// check if the tab is on another page
			if (getPaginatedDisplayedPage(maxDisplayedTabs) != currentPage) {
				return false;
			}
			
			int pageIndex = getPaginatedDisplayedPosition(maxDisplayedTabs, currentPage);
			if (pageIndex <= maxDisplayedTabs) {
				return PaginatedAdvancementTabType.isClickOnTab(screenX, screenY, pageIndex, mouseX, mouseY);
			}
		} else {
			return PaginatedAdvancementTabType.isClickOnTab(screenX, screenY, this.index, mouseX, mouseY);
		}
		return false;
	}
	
	public boolean isClickOnPinnedTab(int screenX, int screenY, double mouseX, double mouseY, int maxPinnedTabs) {
		if (this.pinnedIndex > -1 && this.pinnedIndex <= maxPinnedTabs) {
			return PinnedAdvancementTabType.isClickOnTab(screenX, screenY, this.pinnedIndex, mouseX, mouseY);
		}
		return false;
	}
	
	@Nullable
	public static PaginatedAdvancementTab create(Minecraft client, PaginatedAdvancementScreen screen, int index, int pinnedIndex, AdvancementNode root) {
		if (root.advancement().display().isPresent()) {
			return new PaginatedAdvancementTab(client, screen, index, pinnedIndex, root, root.advancement().display().get());
		}
		return null;
	}
	
	public void move(double offsetX, double offsetY, int endX, int endY) {
		if (this.maxPanX - this.minPanX > endX) {
			this.originX = Mth.clamp(this.originX + offsetX, -(this.maxPanX - endX), 0.0D);
		}
		if (this.maxPanY - this.minPanY > endY) {
			this.originY = Mth.clamp(this.originY + offsetY, -(this.maxPanY - endY), 0.0D);
		}
	}
	
	public void addAdvancement(AdvancementNode advancement) {
		Optional<DisplayInfo> optional = advancement.advancement().display();
		if (optional.isPresent()) {
			AdvancementWidget advancementWidget = new PaginatedAdvancementWidget(this, this.client, advancement, optional.get());
			this.addWidget(advancementWidget, advancement.holder());
		}
	}
	
	private void addWidget(AdvancementWidget widget, AdvancementHolder advancement) {
		this.widgets.put(advancement, widget);
		for (AdvancementWidget advancementWidget : this.widgets.values()) {
			advancementWidget.attachToParent();
		}
		calculatePan();
	}
	
	public void calculatePan() {
		for (AdvancementWidget widget : this.widgets.values()) {
			int widgetStartX = widget.getX();
			int widgetEndX = widgetStartX + 28;
			int widgetStartY = widget.getY();
			int widgetEndY = widgetStartY + 27;
			this.minPanX = Math.min(this.minPanX, widgetStartX);
			this.maxPanX = Math.max(this.maxPanX, widgetEndX);
			this.minPanY = Math.min(this.minPanY, widgetStartY);
			this.maxPanY = Math.max(this.maxPanY, widgetEndY);
		}
	}
	
	@Nullable
	@Override
	public AdvancementWidget getWidget(AdvancementHolder advancement) {
		return this.widgets.get(advancement);
	}
	
	public PaginatedAdvancementScreen getScreen() {
		return this.screen;
	}
	
	public void setPinIndex(int index) {
		this.pinnedIndex = index;
	}
	
	public int getPinIndex() {
		return this.pinnedIndex;
	}
	
	public void copyHoveredAdvancementID() {
		if (this.hoveredWidget != null) {
			AdvancementWidgetAccessor awa = (AdvancementWidgetAccessor) this.hoveredWidget;
			Minecraft.getInstance().keyboardHandler.setClipboard(awa.getAdvancementNode().holder().id().toString());
			Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("text.paginated_advancements.copied_to_clipboard"), false);
		}
	}
	
}
