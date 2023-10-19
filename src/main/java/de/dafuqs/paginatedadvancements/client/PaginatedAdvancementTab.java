package de.dafuqs.paginatedadvancements.client;

import com.google.common.collect.*;
import de.dafuqs.paginatedadvancements.*;
import de.dafuqs.paginatedadvancements.mixin.*;
import net.minecraft.advancement.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.client.texture.*;
import net.minecraft.item.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static de.dafuqs.paginatedadvancements.client.PaginatedAdvancementScreen.*;

public class PaginatedAdvancementTab extends AdvancementTab {
	
	private final MinecraftClient client;
	private final PaginatedAdvancementScreen screen;
	private final int index;
	private int pinnedIndex;
	private final PlacedAdvancement root;
	private final AdvancementDisplay display;
	private final ItemStack icon;
	private final Text title;
	private final AdvancementWidget rootWidget;
	private final Map<AdvancementEntry, AdvancementWidget> widgets = Maps.newLinkedHashMap();
	
	private double originX;
	private double originY;
	private int minPanX = 2147483647;
	private int minPanY = 2147483647;
	private int maxPanX = -2147483648;
	private int maxPanY = -2147483648;
	private float alpha;
	private boolean initialized;
	
	private @Nullable AdvancementWidget hoveredWidget;
	
	public PaginatedAdvancementTab(MinecraftClient client, PaginatedAdvancementScreen screen, int index, int pinnedIndex, PlacedAdvancement root, AdvancementDisplay display) {
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
		this.addWidget(this.rootWidget, root.getAdvancementEntry());
	}
	
	public AdvancementTabType getType() {
		return AdvancementTabType.ABOVE;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public PlacedAdvancement getRoot() {
		return this.root;
	}
	
	public Text getTitle() {
		return this.title;
	}
	
	public AdvancementDisplay getDisplay() {
		return this.display;
	}
	
	public void drawBackground(DrawContext context, int x, int y, boolean selected, int atIndex) {
		PaginatedAdvancementTabType.drawBackground(context, x, y, selected, atIndex);
	}
	
	public void drawIcon(DrawContext context, int x, int y, int index) {
		PaginatedAdvancementTabType.drawIcon(context, x, y, index, this.icon);
	}
	
	public void drawPinnedBackground(DrawContext context, int x, int y, boolean selected, int maxPinnedIndex) {
		if (this.pinnedIndex <= maxPinnedIndex) {
			PinnedAdvancementTabType.drawBackground(context, x, y, selected, this.pinnedIndex);
		}
	}
	
	public void drawPinnedIcon(DrawContext context, int x, int y, int maxPinnedIndex) {
		if (this.pinnedIndex <= maxPinnedIndex) {
			PinnedAdvancementTabType.drawIcon(context, x, y, this.pinnedIndex, this.icon);
		}
	}
	
	public void render(DrawContext context, int startX, int startY, int endX, int endY) {
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
		context.getMatrices().push();
		context.getMatrices().translate(startX, startY, 0.0F);
		Identifier identifier = Objects.requireNonNullElse(this.display.getBackground(), TextureManager.MISSING_IDENTIFIER);
		
		int i = MathHelper.floor(this.originX);
		int j = MathHelper.floor(this.originY);
		int k = i % 16;
		int l = j % 16;
		
		int textureCountX = (advancementTreeWindowWidth) / 16 + 1;
		int textureCountY = (advancementTreeWindowHeight) / 16 + 2;
		for (int m = -1; m < textureCountX; ++m) {
			for (int n = -1; n < textureCountY; ++n) {
				context.drawTexture(identifier, k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
			}
		}
		
		this.rootWidget.renderLines(context, i, j, true);
		this.rootWidget.renderLines(context, i, j, false);
		this.rootWidget.renderWidgets(context, i, j);
		
		context.getMatrices().pop();
		context.disableScissor();
	}
	
	public void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int startX, int startY, int endXWindow, int endY) {
		context.getMatrices().push();
		context.getMatrices().translate(0.0F, 0.0F, -200.0F);
		
		// tinting the background slightly darker
		// (this is the vanilla default, but able to be disabled via config)
		if (PaginatedAdvancementsClient.CONFIG.FadeOutBackgroundOnAdvancementHover) {
			context.fill(0, 0, endXWindow - startX - 18, endY - startY - 26, MathHelper.floor(this.alpha * 255.0F) << 24);
		}
		
		boolean hoversWidget = false;
		int i = MathHelper.floor(this.originX);
		int j = MathHelper.floor(this.originY);
		if (mouseX > 0 && mouseX < endXWindow - startX - 10 && mouseY > 0 && mouseY < endY - startY) {
			for (AdvancementWidget advancementWidget : this.widgets.values()) {
				if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
					hoversWidget = true;
					advancementWidget.drawTooltip(context, i, j, this.alpha, startX, startY);
					
					this.hoveredWidget = advancementWidget;
					
					break;
				}
			}
		}
		
		context.getMatrices().pop();
		if (hoversWidget) {
			this.alpha = MathHelper.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
		} else {
			this.hoveredWidget = null;
			this.alpha = MathHelper.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
		}
	}
	
	public void drawDebugInfo(DrawContext context, int startX, int startY, int endX, int endY) {
		if (this.hoveredWidget != null) {
			AdvancementWidgetAccessor advancementWidgetAccessor = (AdvancementWidgetAccessor) this.hoveredWidget;
			AdvancementProgress progress = advancementWidgetAccessor.getProgress();
			
			startX = startX + 5 - 41;
			endX = endX - 5 - 41;
			endY = endY + 5 - 65;
			startY = ((startY - 5 - 65) / 10) * 10 - 2;
			
			List<MutableText> requirements = getRequirements(startX, endX - 10, advancementWidgetAccessor.getAdvancement().getAdvancement(), progress);
			
			boolean overflow = false;
			int displayedLines;
			if (!hasShiftDown()) {
				overflow = requirements.size() > PaginatedAdvancementsClient.CONFIG.MaxCriterionEntries;
				displayedLines = Math.min(requirements.size(), PaginatedAdvancementsClient.CONFIG.MaxCriterionEntries);
			} else {
				displayedLines = requirements.size();
			}
			
			startY = Math.max(startY, endY - Math.max(28, 20 + 10 * displayedLines));
			
			drawDebugFrame(context, startX, startY, endX, endY);
			
			// the title
			Text idText = Text.literal("ID: " + advancementWidgetAccessor.getAdvancement().getAdvancementEntry().id().toString() + " ").append(Text.translatable("text.paginated_advancements.copy_to_clipboard"));
			context.drawText(this.client.textRenderer, idText, startX + 5, startY + 5, 0xFFFFFF, true);
			
			// the requirements
			if (overflow) {
				drawRequirementsWithOverflow(context, startX + 5, startY + 15, endX - 5, endY, requirements, displayedLines);
			} else {
				drawRequirements(context, startX + 5, startY + 15, endX - 5, endY, requirements);
			}
		}
	}
	
	private List<MutableText> getRequirements(int startX, int endX, Advancement advancement, AdvancementProgress progress) {
		Iterable<String> obtainedCriteria = progress == null ? List.of() : progress.getObtainedCriteria();
		String[][] requirements = advancement.requirements().requirements();
		
		List<MutableText> requirementsDone = new ArrayList<>();
		List<MutableText> requirementsLeft = new ArrayList<>();
		
		for (String[] requirementGroup : requirements) {
			List<MutableText> lines = new ArrayList<>();
			lines.add(Text.translatable("text.paginated_advancements.group").formatted(Formatting.DARK_RED));
			boolean anyDone = false;
			for (String requirementString : requirementGroup) {
				Formatting formatting = Formatting.DARK_RED;
				for (String s : obtainedCriteria) {
					if (s.equals(requirementString)) {
						formatting = Formatting.DARK_GREEN;
						anyDone = true;
						break;
					}
				}
				int newWidth = client.textRenderer.getWidth(lines.get(lines.size() - 1)) + client.textRenderer.getWidth(requirementString);
				if (newWidth > endX - startX) {
					String indent = "";
					while (client.textRenderer.getWidth(indent) < client.textRenderer.getWidth(Text.translatable("text.paginated_advancements.group"))) {
						indent += " ";
					}
					lines.add(Text.literal(indent).formatted(Formatting.DARK_RED));
				}
				lines.get(lines.size() - 1).append(Text.literal(requirementString + " ").formatted(formatting));
			}
			
			if (anyDone) {
				for (MutableText line : lines) {
					line.formatted(Formatting.DARK_GREEN);
					requirementsDone.add(line);
				}
			} else {
				requirementsLeft.addAll(lines);
			}
		}
		
		List<MutableText> combined = new ArrayList<>();
		combined.addAll(requirementsLeft);
		combined.addAll(requirementsDone);
		
		return combined;
	}
	
	protected void drawDebugFrame(DrawContext context, int startX, int startY, int endX, int endY) {
		context.getMatrices().push();
		
		// corners
		context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, startX, startY, 0, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT); // top left
		context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, endX - ELEMENT_WIDTH, startY, 237, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT); // top right
		context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, startX, endY - BOTTOM_ELEMENT_HEIGHT, 0, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT); // bottom left
		context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, endX - ELEMENT_WIDTH, endY - BOTTOM_ELEMENT_HEIGHT, 237, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT); // bottom right
		
		// left + right sides
		int maxTopHeightInOneDrawCall = 100;
		int middleHeight = endY - startY - TOP_ELEMENT_HEIGHT - BOTTOM_ELEMENT_HEIGHT;
		int currentY = startY + TOP_ELEMENT_HEIGHT;
		while (middleHeight > 0) {
			int currentDrawHeight = Math.min(middleHeight, maxTopHeightInOneDrawCall);
			
			context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, startX, currentY, 0, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight);
			context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, endX - ELEMENT_WIDTH, currentY, 237, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight);
			
			middleHeight -= currentDrawHeight;
			currentY += currentDrawHeight;
		}
		
		// top + bottom
		int maxTopWidthInOneDrawCall = 220;
		int middleWidth = endX - startX - ELEMENT_WIDTH - ELEMENT_WIDTH;
		int currentX = startX + ELEMENT_WIDTH;
		while (middleWidth > 0) {
			int currentDrawWidth = Math.min(middleWidth, maxTopWidthInOneDrawCall);
			
			context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, currentX, startY, ELEMENT_WIDTH, 0, currentDrawWidth, TOP_ELEMENT_HEIGHT);
			context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, currentX, endY - BOTTOM_ELEMENT_HEIGHT, ELEMENT_WIDTH, 125, currentDrawWidth, BOTTOM_ELEMENT_HEIGHT);
			
			middleWidth -= currentDrawWidth;
			currentX += currentDrawWidth;
		}
		
		// center
		int centerStartX = startX + 6;
		int centerEndX = endX - 6;
		int centerStartY = startY + 6;
		int centerEndY = endY - 6;
		
		int drawStartY = centerStartY;
		int drawHeight = centerEndY - centerStartY;
		while (drawHeight > 0) {
			int drawStartX = centerStartX;
			int currentHeight = Math.min(drawHeight, 10);
			int drawWidth = centerEndX - centerStartX;
			while (drawWidth > 0) {
				int currentWidth = Math.min(200, drawWidth);
				context.drawTexture(PaginatedAdvancementScreen.WINDOW_TEXTURE, drawStartX, drawStartY, 4, 4, currentWidth, currentHeight);
				drawWidth -= currentWidth;
				drawStartX += currentWidth;
			}
			drawHeight -= currentHeight;
			drawStartY += currentHeight;
		}
		context.getMatrices().pop();
	}
	
	protected void drawRequirementsWithOverflow(DrawContext context, int startX, int startY, int endX, int endY, List<MutableText> requirements, int lines) {
		for (int i = 0; i < lines; i++) {
			if (i == lines - 1) {
				context.drawText(this.client.textRenderer, Text.translatable("text.paginated_advancements.expand_debug"), startX, startY, 0x999999, false);
			} else {
				context.drawText(this.client.textRenderer, requirements.get(i), startX, startY, 0x00ff00, false);
			}
			startY += 10;
		}
	}
	
	protected void drawRequirements(DrawContext context, int startX, int startY, int endX, int endY, List<MutableText> requirements) {
		int scrollAmount = 0;
		assert this.hoveredWidget != null;
		if (this.hoveredWidget.getClass() == PaginatedAdvancementWidget.class) {
			scrollAmount = ((PaginatedAdvancementWidget) this.hoveredWidget).getDebugScrollAmount();
			// clamp scroll amount
			int maxLines = (endY - startY) / 10;
			scrollAmount = Math.max(0, Math.min(requirements.size() - maxLines, scrollAmount));
			((PaginatedAdvancementWidget) this.hoveredWidget).setDebugScrollAmount(scrollAmount);
		}
		
		if (scrollAmount > 0) {
			context.drawText(this.client.textRenderer, Text.translatable("text.paginated_advancements.scroll_debug"), startX, startY, 0x999999, false);
			scrollAmount += 1;
			startY += 10;
		}
		for (int i = scrollAmount; i < requirements.size(); i++) {
			if (startY + 10 >= endY) break;
			else if (startY + 20 >= endY && i + 1 != requirements.size()) {
				context.drawText(this.client.textRenderer, Text.translatable("text.paginated_advancements.scroll_debug"), startX, startY, 0x999999, false);
				break;
			}
			context.drawText(this.client.textRenderer, requirements.get(i), startX, startY, 0x00ff00, false);
			startY += 10;
		}
	}
	
	public boolean scrollDebug(int diff) {
		if (this.hoveredWidget != null && this.hoveredWidget.getClass() == PaginatedAdvancementWidget.class) {
			int value = ((PaginatedAdvancementWidget) hoveredWidget).getDebugScrollAmount();
			((PaginatedAdvancementWidget) hoveredWidget).setDebugScrollAmount(value + diff);
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
	public static PaginatedAdvancementTab create(MinecraftClient client, PaginatedAdvancementScreen screen, int index, int pinnedIndex, PlacedAdvancement root) {
		if (root.getAdvancement().display().isPresent()) {
			return new PaginatedAdvancementTab(client, screen, index, pinnedIndex, root, root.getAdvancement().display().get());
		}
		return null;
	}
	
	public void move(double offsetX, double offsetY, int endX, int endY) {
		if (this.maxPanX - this.minPanX > endX) {
			this.originX = MathHelper.clamp(this.originX + offsetX, -(this.maxPanX - endX), 0.0D);
		}
		if (this.maxPanY - this.minPanY > endY) {
			this.originY = MathHelper.clamp(this.originY + offsetY, -(this.maxPanY - endY), 0.0D);
		}
	}
	
	public void addAdvancement(PlacedAdvancement advancement) {
		Optional<AdvancementDisplay> optional = advancement.getAdvancement().display();
		if (optional.isPresent()) {
			AdvancementWidget advancementWidget = new PaginatedAdvancementWidget(this, this.client, advancement, optional.get());
			this.addWidget(advancementWidget, advancement.getAdvancementEntry());
		}
	}
	
	private void addWidget(AdvancementWidget widget, AdvancementEntry advancement) {
		this.widgets.put(advancement, widget);
		for (AdvancementWidget advancementWidget : this.widgets.values()) {
			advancementWidget.addToTree();
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
	public AdvancementWidget getWidget(AdvancementEntry advancement) {
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
			MinecraftClient.getInstance().keyboard.setClipboard(awa.getAdvancement().getAdvancementEntry().id().toString());
			MinecraftClient.getInstance().inGameHud.setOverlayMessage(Text.translatable("text.paginated_advancements.copied_to_clipboard"), false);
		}
	}
	
}
