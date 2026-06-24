package de.dafuqs.paginatedadvancements.client;

import com.google.common.collect.*;
import de.dafuqs.paginatedadvancements.*;
import net.minecraft.advancements.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.advancements.*;
import net.minecraft.client.input.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import org.jspecify.annotations.*;

import java.util.*;

public class PaginatedAdvancementScreen extends AdvancementsScreen implements ClientAdvancements.Listener {

	public static final Identifier PAGINATION_TEXTURE = PaginatedAdvancementsClient.locate("textures/gui/buttons.png");
	public static final Identifier WINDOW_TEXTURE = Identifier.withDefaultNamespace("textures/gui/advancements/window.png");

	private static final Component SAD_LABEL_TEXT = Component.translatable("advancements.sad_label");
	private static final Component EMPTY_TEXT = Component.translatable("advancements.empty");
	private static final Component ADVANCEMENTS_TEXT = Component.translatable("gui.advancements");

	private final ClientAdvancements advancementHandler;
	private final Map<AdvancementHolder, PaginatedAdvancementTab> tabs = Maps.newLinkedHashMap();
	private final Map<AdvancementHolder, PaginatedAdvancementTab> pinnedTabs = Maps.newLinkedHashMap();
	@Nullable private PaginatedAdvancementTab selectedTab;
	private boolean movingTab;

	// pagination
	private int currentPage = 0;

	public static final int ADDITIONAL_PADDING_TOP = 20; // to account for the advancement tabs at the top
	public static final int BORDER_PADDING = 32;
	public static final int ELEMENT_WIDTH = 15;
	public static final int TOP_ELEMENT_HEIGHT = 22;
	public static final int BOTTOM_ELEMENT_HEIGHT = 15;

	public static final int FAVOURITES_BUTTON_WIDTH = 18;
	public static final int FAVOURITES_BUTTON_HEIGHT = 18;

	public static final int FAVOURITES_BUTTON_OFFSET_X = 32;
	public static final int FAVOURITES_BUTTON_OFFSET_Y = 8;

	public PaginatedAdvancementScreen(ClientAdvancements advancementHandler) {
		super(advancementHandler);
		this.advancementHandler = advancementHandler;
	}

	@Override
	protected void init() {
		super.init();
		this.tabs.clear();
		this.selectedTab = null;
		this.advancementHandler.setListener(this);

		if (this.selectedTab == null && !this.tabs.isEmpty()) {
			boolean tabSelected = false;
			if(PaginatedAdvancementsClient.CONFIG.SaveLastSelectedTab && !PaginatedAdvancementsClient.CONFIG.LastSelectedTab.isEmpty()) {
				// search for the tab and if that is existent open that instead

				Identifier savedTabIdentifier = Identifier.tryParse(PaginatedAdvancementsClient.CONFIG.LastSelectedTab);
				for(AdvancementTab advancementTab : this.tabs.values()) {
					if(advancementTab.getRootNode().holder().id().equals(savedTabIdentifier)) {
						this.advancementHandler.setSelectedTab(advancementTab.getRootNode().holder(), true);
						tabSelected = true;
						break;
					}
				}
			}
			if(!tabSelected) {
				// vanilla default behavior: just open some random tab
				this.advancementHandler.setSelectedTab((this.tabs.values().iterator().next()).getRootNode().holder(), true);
			}
		} else {
			this.advancementHandler.setSelectedTab(this.selectedTab == null ? null : this.selectedTab.getRootNode().holder(), true);
		}

		// initialize pinned tabs
		if(!this.tabs.isEmpty() && PaginatedAdvancementsClient.hasPins()) {
			for(String pinnedTabString : PaginatedAdvancementsClient.getPinnedTabs()) {
				Identifier pinnedTabIdentifier = Identifier.tryParse(pinnedTabString);
				for(PaginatedAdvancementTab advancementTab : this.tabs.values()) {
					if(advancementTab.getRootNode().holder().id().equals(pinnedTabIdentifier)) {
						this.pinnedTabs.put(advancementTab.getRootNode().holder(), advancementTab);
						break;
					}
				}
			}
		}
	}

	@Override
	public void removed() {
		super.removed();
	}

	// instead of drawing the full texture here, we cut it into pieces and draw
	// the top, sides and more piece by piece, making the size variable with the mc window size
	public void drawWindow(GuiGraphicsExtractor context, int mouseX, int mouseY, int minWidth, int minHeight, int maxWidth, int maxHeight) {
		drawFrame(context, minWidth, minHeight, maxWidth, maxHeight);
		context.text(minecraft.font, ADVANCEMENTS_TEXT, minWidth + 8, minHeight + 6, 4210752, false);
	}

	public void drawPinButtonAndHeader(GuiGraphicsExtractor context, int mouseX, int mouseY, int startX, int startY, int endX, int endY, boolean hasPins) {
		if (this.selectedTab != null && PaginatedAdvancementsClient.CONFIG.PinningEnabled) {
			if (isClickOnFavouritesButton(mouseX, mouseY, startY, endX)) {
				if (PaginatedAdvancementsClient.isPinned(this.selectedTab.getRootNode().holder().id())) {
					context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, endX - FAVOURITES_BUTTON_OFFSET_X, startY + FAVOURITES_BUTTON_OFFSET_Y, FAVOURITES_BUTTON_WIDTH, 46 + FAVOURITES_BUTTON_HEIGHT, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT, 256, 256);
				} else {
					context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, endX - FAVOURITES_BUTTON_OFFSET_X, startY + FAVOURITES_BUTTON_OFFSET_Y, 0, 46 + FAVOURITES_BUTTON_HEIGHT, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT, 256, 256);
				}
			} else {
				if (PaginatedAdvancementsClient.isPinned(this.selectedTab.getRootNode().holder().id())) {
					context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, endX - FAVOURITES_BUTTON_OFFSET_X, startY + FAVOURITES_BUTTON_OFFSET_Y, FAVOURITES_BUTTON_WIDTH, 46, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT, 256, 256);
				} else {
					context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, endX - FAVOURITES_BUTTON_OFFSET_X, startY + FAVOURITES_BUTTON_OFFSET_Y, 0, 46, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT, 256, 256);
				}
			}

			if(hasPins) {
				// draw pinned tab header
				context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, endX + PinnedAdvancementTabType.getTabX() + 1, startY + 6, 46, 0, 32, 15, 256, 256);
			}
		}
	}

	public boolean isClickOnFavouritesButton(double mouseX, double mouseY, int minHeight, int maxWidth) {
		return PaginatedAdvancementsClient.CONFIG.PinningEnabled
				&& mouseX > maxWidth - FAVOURITES_BUTTON_OFFSET_X
				&& mouseX < maxWidth - FAVOURITES_BUTTON_OFFSET_X + FAVOURITES_BUTTON_WIDTH
				&& mouseY > minHeight + FAVOURITES_BUTTON_OFFSET_Y
				&& mouseY < minHeight + FAVOURITES_BUTTON_OFFSET_Y + FAVOURITES_BUTTON_HEIGHT;
	}

	public int getMaxPaginatedTabsToRender(int startX, int endXTitle, int endXWindow, boolean paginated) {
		if(paginated) {
			int usableWidth = endXTitle - startX;
			return ((usableWidth - 58) / PaginatedAdvancementTabType.getWidthWithSpacing()); // room for forward and back button
		} else {
			int usableWidth = endXWindow - startX;
			return ((usableWidth - 28) / PaginatedAdvancementTabType.getWidthWithSpacing());
		}
	}

	public int getMaxPinnedTabsToRender(int startY, int endY) {
		int usableHeight = endY - startY;
		return (usableHeight - 56 + PaginatedAdvancementTabType.getWidthWithSpacing()) / PinnedAdvancementTabType.getHeightWithSpacing(); // room for pin button + spacing
	}

	private boolean isPaginated(int startX, int endXWindow) {
		if(tabs.size() < 3) {
			return false; // fast fail. Does not make sense to paginate
		} else {
			return endXWindow - startX < tabs.size() * PaginatedAdvancementTabType.getWidthWithSpacing();
		}
	}

	private void renderPaginatedTabs(GuiGraphicsExtractor context, int startX, int startY, int endXTitle, int endXWindow, boolean paginated) {
		Iterator<PaginatedAdvancementTab> tabIterator = this.tabs.values().iterator();
		int maxAdvancementTabsToRender = getMaxPaginatedTabsToRender(startX, endXTitle, endXWindow, paginated);

		int index = 0;
		PaginatedAdvancementTab advancementTab;
		while (tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			if (paginated) {
				if(advancementTab.getPaginatedDisplayedPage(maxAdvancementTabsToRender) == this.currentPage) {
					int displayedPosition = advancementTab.getPaginatedDisplayedPosition(maxAdvancementTabsToRender, this.currentPage);
					advancementTab.drawBackground(context, startX, startY, advancementTab == this.selectedTab, displayedPosition);
				}
			} else {
				advancementTab.drawBackground(context, startX, startY, advancementTab == this.selectedTab, index);
				index++;
			}
		}

		index = 0;
		tabIterator = this.tabs.values().iterator();
		while(tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			if(paginated) {
				if(advancementTab.getPaginatedDisplayedPage(maxAdvancementTabsToRender) == this.currentPage) {
					int displayedPosition = advancementTab.getPaginatedDisplayedPosition(maxAdvancementTabsToRender, this.currentPage);
					advancementTab.drawIcon(context, startX, startY, displayedPosition);
				}
			} else {
				advancementTab.drawIcon(context, startX, startY, index);
				index++;
			}
		}
	}

	private void renderPinnedTabs(GuiGraphicsExtractor context, int startX, int startY, int endX, int endY) {
		int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);

		Iterator<PaginatedAdvancementTab> tabIterator = this.pinnedTabs.values().iterator();

		PaginatedAdvancementTab advancementTab;
		while (tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			advancementTab.drawPinnedBackground(context, endX, startY, advancementTab == this.selectedTab, maxPinnedTabs);
		}

		tabIterator = this.pinnedTabs.values().iterator();
		while(tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			advancementTab.drawPinnedIcon(context, endX, startY, maxPinnedTabs);
		}
	}

	// instead of drawing the full texture here, we cut it into pieces and draw
	// the top, sides and more piece by piece, making the size variable with the mc window size
	public void drawPaginationButtons(GuiGraphicsExtractor context, int mouseX, int mouseY, int startX, int endX) {
		if (isClickOnBackTab(mouseX, mouseY, startX, endX)) {
			// hover
			context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, startX + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 0, 23, 23, 23, 256, 256);
		} else {
			// no hover
			context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, startX + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 0, 0, 23, 23, 256, 256);
		}

		if (isClickOnForwardTab(mouseX, mouseY, startX, endX)) {
			// hover
			context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, endX - startX + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 23, 23, 23, 23, 256, 256);
		} else {
			// no hover
			context.blit(RenderPipelines.GUI_TEXTURED, PAGINATION_TEXTURE, endX - startX + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 23, 0, 23, 23, 256, 256);
		}
	}

	public static boolean isClickOnBackTab(double mouseX, double mouseY, int startX, int enX) {
		int buttonStartX = startX + 4;
		int buttonStartY = PaginatedAdvancementScreen.TOP_ELEMENT_HEIGHT + PaginatedAdvancementScreen.ADDITIONAL_PADDING_TOP - 15;
		return mouseX > buttonStartX && mouseX < buttonStartX + 23 && mouseY > buttonStartY && mouseY < buttonStartY + 23;
	}

	public static boolean isClickOnForwardTab(double mouseX, double mouseY, int startX, int endX) {
		int buttonStartX = endX - startX + 4;
		int buttonStartY = PaginatedAdvancementScreen.TOP_ELEMENT_HEIGHT + PaginatedAdvancementScreen.ADDITIONAL_PADDING_TOP - 15;
		return mouseX > buttonStartX && mouseX < buttonStartX + 23 && mouseY > buttonStartY && mouseY < buttonStartY + 23;
	}

	private void drawFrame(GuiGraphicsExtractor context, int startX, int startY, int endX, int endY) {
		// corners
		context.blit(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, startX, startY, 0, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT, 256, 256); // top left
		context.blit(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, endX - ELEMENT_WIDTH, startY, 237, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT, 256, 256); // top right
		context.blit(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, startX, endY - BOTTOM_ELEMENT_HEIGHT, 0, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT, 256, 256); // bottom left
		context.blit(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, endX - ELEMENT_WIDTH, endY - BOTTOM_ELEMENT_HEIGHT, 237, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT, 256, 256); // bottom right

		// left + right sides
		int maxTopHeightInOneDrawCall = 100;
		int middleHeight = endY - startY - TOP_ELEMENT_HEIGHT - BOTTOM_ELEMENT_HEIGHT;
		int currentY = startY + TOP_ELEMENT_HEIGHT;
		while (middleHeight > 0) {
			int currentDrawHeight = Math.min(middleHeight, maxTopHeightInOneDrawCall);

			context.blit(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, startX, currentY, 0, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, endX - ELEMENT_WIDTH, currentY, 237, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight, 256, 256);

			middleHeight -= currentDrawHeight;
			currentY += currentDrawHeight;
		}

		// top + bottom
		int maxTopWidthInOneDrawCall = 220;
		int middleWidth = endX - startX - ELEMENT_WIDTH - ELEMENT_WIDTH;
		int currentX = startX + ELEMENT_WIDTH;
		while (middleWidth > 0) {
			int currentDrawWidth = Math.min(middleWidth, maxTopWidthInOneDrawCall);

			context.blit(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, currentX, startY, ELEMENT_WIDTH, 0, currentDrawWidth, TOP_ELEMENT_HEIGHT, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, WINDOW_TEXTURE, currentX, endY - BOTTOM_ELEMENT_HEIGHT, ELEMENT_WIDTH, 125, currentDrawWidth, BOTTOM_ELEMENT_HEIGHT, 256, 256);

			middleWidth -= currentDrawWidth;
			currentX += currentDrawWidth;
		}
	}

	@Override
	public void onAddAdvancementRoot(AdvancementNode root) {
		int pinnedIndex = PaginatedAdvancementsClient.getPinIndex(root.holder().id());
		PaginatedAdvancementTab advancementTab = PaginatedAdvancementTab.create(this.minecraft, this, this.tabs.size(), pinnedIndex, root);
		if (advancementTab != null) {
			this.tabs.put(root.holder(), advancementTab);
			if(PaginatedAdvancementsClient.isPinned(root.holder().id())) {
				this.pinnedTabs.put(root.holder(), advancementTab);
			}
		}
	}

	@Override
	public void onRemoveAdvancementRoot(@NonNull AdvancementNode root) {
	}

	@Override
	public void onAddAdvancementTask(@NonNull AdvancementNode dependent) {
		PaginatedAdvancementTab advancementTab = this.getTab(dependent);
		if (advancementTab != null) {
			advancementTab.addAdvancement(dependent);
		}
	}

	@Override
	public void onRemoveAdvancementTask(@NonNull AdvancementNode dependent) {
	}

	@Nullable
	private PaginatedAdvancementTab getTab(AdvancementNode advancement) {
		AdvancementNode placedAdvancement = advancement.root();
		return this.tabs.get(placedAdvancement.holder());
	}

	@Override
	public void onAdvancementsCleared() {
		this.tabs.clear();
		this.pinnedTabs.clear();
		this.selectedTab = null;
	}

	@Override
	public void onSelectedTabChanged(@Nullable AdvancementHolder advancement) {
		this.selectedTab = this.tabs.get(advancement);
		if(this.selectedTab != null) {
			PaginatedAdvancementsClient.saveSelectedTab(this.selectedTab.getRootNode().holder().id());
		}
	}

    @Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (click.button() == 0) {
			int startX = BORDER_PADDING;
			int endXWindow = !this.pinnedTabs.isEmpty() ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH : this.width - BORDER_PADDING;
			int endXTitle = this.width - BORDER_PADDING;
			int startY = BORDER_PADDING + ADDITIONAL_PADDING_TOP;
			int endY = this.height - BORDER_PADDING;

			boolean isPaginated = isPaginated(startX, endXWindow);

			if(this.selectedTab != null && isClickOnFavouritesButton(click.x(), click.y(), startY, endXWindow)) {
				Identifier pageIdentifier = this.selectedTab.getRootNode().holder().id();
				if(PaginatedAdvancementsClient.isPinned(pageIdentifier)) {
					unpinTab(pageIdentifier);
				} else {
					pinTab(pageIdentifier);
				}
			}

			if(isPaginated) {
				if (isClickOnBackTab(click.x(), click.y(), startX, endXTitle)) {
					pageBackward(startX, endXTitle, endXWindow);
				} else if (isClickOnForwardTab(click.x(), click.y(), startX, endXTitle)) {
					pageForward(startX, endXTitle, endXWindow);
				}
			}

			int maxDisplayedTabs = getMaxPaginatedTabsToRender(startX, endXTitle, endXWindow, isPaginated);
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.tabs.values()) {
				if (paginatedAdvancementTab.isClickOnTab(BORDER_PADDING, BORDER_PADDING + ADDITIONAL_PADDING_TOP, click.x(), click.y(), isPaginated, maxDisplayedTabs, currentPage)) {
					this.advancementHandler.setSelectedTab(paginatedAdvancementTab.getRootNode().holder(), true);
					break;
				}
			}

			if (!this.pinnedTabs.isEmpty()) {
				int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);
				for (PaginatedAdvancementTab paginatedAdvancementTab : this.pinnedTabs.values()) {
					if (paginatedAdvancementTab.isClickOnPinnedTab(endXWindow, startY, click.x(), click.y(), maxPinnedTabs)) {
						this.advancementHandler.setSelectedTab(paginatedAdvancementTab.getRootNode().holder(), true);
					}
				}
			}
		}
		return super.mouseClicked(click, doubled);
	}

	private void pinTab(Identifier pageIdentifier) {
		selectedTab.setPinIndex(this.pinnedTabs.size());
		this.pinnedTabs.put(selectedTab.getRootNode().holder(), selectedTab);
		PaginatedAdvancementsClient.pinTab(pageIdentifier);
	}

	private void unpinTab(Identifier pageIdentifier) {
		int oldPinIndex = selectedTab.getPinIndex();
		selectedTab.setPinIndex(-1);
		this.pinnedTabs.remove(selectedTab.getRootNode().holder());
		PaginatedAdvancementsClient.unpinTab(pageIdentifier);

		// move all pinned tabs with a pin index > this up by 1 to fill its place
		for(PaginatedAdvancementTab tab : this.pinnedTabs.values()) {
			int currentPinIndex = tab.getPinIndex();
			if(currentPinIndex > oldPinIndex) {
				tab.setPinIndex(currentPinIndex -1);
			}
		}
	}

	public int getMaxPageIndex(int startX, int endXTitle, int endXWindow) {
		int maxDisplayedTabsPerPage = getMaxPaginatedTabsToRender(startX, endXTitle, endXWindow, true);
		return (this.tabs.size() - 1) / maxDisplayedTabsPerPage;
	}

	public void clampCurrentPage(int startX, int endXTitle, int endXWindow) {
		this.currentPage = Math.min(this.currentPage, getMaxPageIndex(startX, endXTitle, endXWindow));
	}

	public void pageForward(int startX, int endXTitle, int endXWindow) {
		this.currentPage++;
		this.currentPage = this.currentPage % (getMaxPageIndex(startX, endXTitle, endXWindow) + 1);
	}

	public void pageBackward(int startX, int endXTitle, int endXWindow) {
		int maxPageIndex = getMaxPageIndex(startX, endXTitle, endXWindow);
		if(this.currentPage == 0) {
			this.currentPage = maxPageIndex;
		} else {
			this.currentPage--;
		}
	}

    @Override
	public boolean keyPressed(@NonNull KeyEvent input) {
		if (this.minecraft.options.keyAdvancements.matches(input)) {
			this.minecraft.gui.setScreen(null);
			this.minecraft.mouseHandler.grabMouse();
			return true;
		} else if (this.selectedTab != null && input.isCopy()) { // ctrl + c
			this.selectedTab.copyHoveredAdvancementID();
			return true;
		} else {
			return super.keyPressed(input);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.selectedTab != null)
			return this.selectedTab.scrollDebug(-(int) verticalAmount);
		else
			return false;
	}

    @Override
	public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
		if (click.button() != 0) {
			this.movingTab = false;
			return false;
		} else {
			if (!this.movingTab) {
				this.movingTab = true;
			} else if (this.selectedTab != null) {
				int endX = !this.pinnedTabs.isEmpty() ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH - 4 : this.width - BORDER_PADDING;
				int endY = this.height - BORDER_PADDING;

				this.selectedTab.move(offsetX, offsetY, endX - 60 + 5, endY - 84);
			}

			return true;
		}
	}

	private void drawWidgetTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY, int startX, int startY, int endXTitle, int endXWindow, int endY) {
		if (this.selectedTab != null) {
			context.pose().pushMatrix();
			context.pose().translate((startX + 9), (startY + 18));//, 400.0D);
			this.selectedTab.drawWidgetTooltip(context, mouseX - startX - 9, mouseY - startY - 18, startX, startY, endXWindow, endY);

			context.pose().translate(0, 0);//, 400.0D);
			if (PaginatedAdvancementsClient.CONFIG.shouldShowAdvancementDebug(this.minecraft)) {
				this.selectedTab.drawDebugInfo(context, startX, startY, endXWindow, endY);
			}

			context.pose().popMatrix();
		}

		if (this.tabs.size() > 1) {
			boolean isPaginated = isPaginated(startX, endXWindow);
			int maxDisplayedTabs = getMaxPaginatedTabsToRender(startX, endXTitle, endXWindow, isPaginated);

			for (PaginatedAdvancementTab paginatedAdvancementTab : this.tabs.values()) {
				if (paginatedAdvancementTab.isClickOnTab(startX, startY, mouseX, mouseY, isPaginated, maxDisplayedTabs, currentPage)) {
					context.setTooltipForNextFrame(this.font, paginatedAdvancementTab.getTitle(), mouseX, mouseY);
				}
			}
		}

		if (!this.pinnedTabs.isEmpty()) {
			int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.pinnedTabs.values()) {
				if (paginatedAdvancementTab.isClickOnPinnedTab(endXWindow, startY, mouseX, mouseY, maxPinnedTabs)) {
					context.setTooltipForNextFrame(this.font, paginatedAdvancementTab.getTitle(), mouseX, mouseY);
				}
			}
		}
	}

	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		boolean hasPins = !this.pinnedTabs.isEmpty();
		int startX = BORDER_PADDING;
		int startY = BORDER_PADDING + ADDITIONAL_PADDING_TOP;
		int endXWindow = hasPins ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH : this.width - BORDER_PADDING;
		int endXTitle = this.width - BORDER_PADDING;
		int endY = this.height - BORDER_PADDING;

		clampCurrentPage(startX, endXTitle, endXWindow); // if the screen has been resized

		this.drawAdvancementTree(context, startX, startY, endXWindow, endY);
		this.drawWindow(context, mouseX, mouseY, startX, startY, endXWindow, endY);

		if (this.tabs.size() > 1) {
			if (isPaginated(startX, endXWindow)) { // overflows
				// draw forward and back button tabs, fill the rest with the remaining tabs
				drawPaginationButtons(context, mouseX, mouseY, startX, endXTitle);
				renderPaginatedTabs(context, startX, startY, endXTitle, endXWindow, true);
			} else {
				renderPaginatedTabs(context, startX, startY, endXTitle, endXWindow, false);
			}
		}
		if (hasPins) {
			renderPinnedTabs(context, startX, startY, endXWindow, endY);
		}
		this.drawWidgetTooltip(context, mouseX, mouseY, startX, startY, endXTitle, endXWindow, endY);
		this.drawPinButtonAndHeader(context, mouseX, mouseY, startX, startY, endXWindow, endY, hasPins);
	}

	private void drawAdvancementTree(GuiGraphicsExtractor context, int startX, int startY, int endX, int endY) {
		PaginatedAdvancementTab advancementTab = this.selectedTab;
		if (advancementTab == null) {
			context.fill(startX + 9, startY + 18, endX, endY, -16777216);

			int textCenterX = startX + ((endX - startX) / 2);
			int textY = startY + ((endY - startY) / 2);
			context.centeredText(this.font, EMPTY_TEXT, textCenterX, textY, -1);
			context.centeredText(this.font, SAD_LABEL_TEXT, textCenterX, textY + 16, -1);
		} else {
			advancementTab.render(context, startX, startY, endX, endY);
		}
	}

	@Nullable
	public AdvancementWidget getAdvancementWidget(@NonNull AdvancementNode advancement) {
		AdvancementTab advancementTab = this.getTab(advancement);
		return advancementTab == null ? null : advancementTab.getWidget(advancement.holder());
	}

}
