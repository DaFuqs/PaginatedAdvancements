package de.dafuqs.paginatedadvancements.client;

import com.google.common.collect.*;
import com.mojang.blaze3d.systems.*;
import de.dafuqs.paginatedadvancements.*;
import net.minecraft.advancement.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.client.network.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class PaginatedAdvancementScreen extends AdvancementsScreen implements ClientAdvancementManager.Listener {
	
	public static final Identifier PAGINATION_TEXTURE = new Identifier("paginatedadvancements", "textures/gui/buttons.png");
	public static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
	
	private static final Text SAD_LABEL_TEXT = Text.translatable("advancements.sad_label");
	private static final Text EMPTY_TEXT = Text.translatable("advancements.empty");
	private static final Text ADVANCEMENTS_TEXT = Text.translatable("gui.advancements");
	
	private final ClientAdvancementManager advancementHandler;
	private final Map<AdvancementEntry, PaginatedAdvancementTab> tabs = Maps.newLinkedHashMap();
	private final Map<AdvancementEntry, PaginatedAdvancementTab> pinnedTabs = Maps.newLinkedHashMap();
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
	
	public PaginatedAdvancementScreen(ClientAdvancementManager advancementHandler) {
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
					if(advancementTab.getRoot().getAdvancementEntry().id().equals(savedTabIdentifier)) {
						this.advancementHandler.selectTab(advancementTab.getRoot().getAdvancementEntry(), true);
						tabSelected = true;
						break;
					}
				}
			}
			if(!tabSelected) {
				// vanilla default behavior: just open some random tab
				this.advancementHandler.selectTab((this.tabs.values().iterator().next()).getRoot().getAdvancementEntry(), true);
			}
		} else {
			this.advancementHandler.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot().getAdvancementEntry(), true);
		}
		
		// initialize pinned tabs
		if(!this.tabs.isEmpty() && PaginatedAdvancementsClient.hasPins()) {
			for(String pinnedTabString : PaginatedAdvancementsClient.getPinnedTabs()) {
				Identifier pinnedTabIdentifier = Identifier.tryParse(pinnedTabString);
				for(PaginatedAdvancementTab advancementTab : this.tabs.values()) {
					if(advancementTab.getRoot().getAdvancementEntry().id().equals(pinnedTabIdentifier)) {
						this.pinnedTabs.put(advancementTab.getRoot().getAdvancementEntry(), advancementTab);
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
	public void drawWindow(DrawContext context, int mouseX, int mouseY, int minWidth, int minHeight, int maxWidth, int maxHeight) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
		
		drawFrame(context, minWidth, minHeight, maxWidth, maxHeight);
		context.drawText(client.textRenderer, ADVANCEMENTS_TEXT, minWidth + 8, minHeight + 6, 4210752, false);
	}
	
	public void drawPinButtonAndHeader(DrawContext context, int mouseX, int mouseY, int startX, int startY, int endX, int endY, boolean hasPins) {
		if (this.selectedTab != null && PaginatedAdvancementsClient.CONFIG.PinningEnabled) {
			if (isClickOnFavouritesButton(mouseX, mouseY, startY, endX)) {
				if (PaginatedAdvancementsClient.isPinned(this.selectedTab.getRoot().getAdvancementEntry().id())) {
					context.drawTexture(PAGINATION_TEXTURE, endX - FAVOURITES_BUTTON_OFFSET_X, startY + FAVOURITES_BUTTON_OFFSET_Y, FAVOURITES_BUTTON_WIDTH, 46 + FAVOURITES_BUTTON_HEIGHT, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT);
				} else {
					context.drawTexture(PAGINATION_TEXTURE, endX - FAVOURITES_BUTTON_OFFSET_X, startY + FAVOURITES_BUTTON_OFFSET_Y, 0, 46 + FAVOURITES_BUTTON_HEIGHT, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT);
				}
			} else {
				if (PaginatedAdvancementsClient.isPinned(this.selectedTab.getRoot().getAdvancementEntry().id())) {
					context.drawTexture(PAGINATION_TEXTURE, endX - FAVOURITES_BUTTON_OFFSET_X, startY + FAVOURITES_BUTTON_OFFSET_Y, FAVOURITES_BUTTON_WIDTH, 46, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT);
				} else {
					context.drawTexture(PAGINATION_TEXTURE, endX - FAVOURITES_BUTTON_OFFSET_X, startY + FAVOURITES_BUTTON_OFFSET_Y, 0, 46, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT);
				}
			}
			
			if(hasPins) {
				// draw pinned tab header
				context.drawTexture(PAGINATION_TEXTURE, endX + PinnedAdvancementTabType.getTabX() + 1, startY + 6, 46, 0, 32, 15);
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
	
	private void renderPaginatedTabs(DrawContext context, int startX, int startY, int endXTitle, int endXWindow, boolean paginated) {
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
		
		RenderSystem.defaultBlendFunc();
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
		RenderSystem.disableBlend();
	}
	
	private void renderPinnedTabs(DrawContext context, int startX, int startY, int endX, int endY) {
		int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);
		
		Iterator<PaginatedAdvancementTab> tabIterator = this.pinnedTabs.values().iterator();
		
		PaginatedAdvancementTab advancementTab;
		while (tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			advancementTab.drawPinnedBackground(context, endX, startY, advancementTab == this.selectedTab, maxPinnedTabs);
		}
		
		RenderSystem.defaultBlendFunc();
		tabIterator = this.pinnedTabs.values().iterator();
		while(tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			advancementTab.drawPinnedIcon(context, endX, startY, maxPinnedTabs);
		}
		RenderSystem.disableBlend();
	}
	
	// instead of drawing the full texture here, we cut it into pieces and draw
	// the top, sides and more piece by piece, making the size variable with the mc window size
	public void drawPaginationButtons(DrawContext context, int mouseX, int mouseY, int startX, int endX) {
		if (isClickOnBackTab(mouseX, mouseY, startX, endX)) {
			// hover
			context.drawTexture(PAGINATION_TEXTURE, startX + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 0, 23, 23, 23);
		} else {
			// no hover
			context.drawTexture(PAGINATION_TEXTURE, startX + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 0, 0, 23, 23);
		}
		
		if (isClickOnForwardTab(mouseX, mouseY, startX, endX)) {
			// hover
			context.drawTexture(PAGINATION_TEXTURE, endX - startX + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 23, 23, 23, 23);
		} else {
			// no hover
			context.drawTexture(PAGINATION_TEXTURE, endX - startX + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 23, 0, 23, 23);
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
	
	private void drawFrame(DrawContext context, int startX, int startY, int endX, int endY) {
		// corners
		context.drawTexture(WINDOW_TEXTURE, startX, startY, 0, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT); // top left
		context.drawTexture(WINDOW_TEXTURE, endX - ELEMENT_WIDTH, startY, 237, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT); // top right
		context.drawTexture(WINDOW_TEXTURE, startX, endY - BOTTOM_ELEMENT_HEIGHT, 0, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT); // bottom left
		context.drawTexture(WINDOW_TEXTURE, endX - ELEMENT_WIDTH, endY - BOTTOM_ELEMENT_HEIGHT, 237, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT); // bottom right
		
		// left + right sides
		int maxTopHeightInOneDrawCall = 100;
		int middleHeight = endY - startY - TOP_ELEMENT_HEIGHT - BOTTOM_ELEMENT_HEIGHT;
		int currentY = startY + TOP_ELEMENT_HEIGHT;
		while (middleHeight > 0) {
			int currentDrawHeight = Math.min(middleHeight, maxTopHeightInOneDrawCall);
			
			context.drawTexture(WINDOW_TEXTURE, startX, currentY, 0, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight);
			context.drawTexture(WINDOW_TEXTURE, endX - ELEMENT_WIDTH, currentY, 237, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight);
			
			middleHeight -= currentDrawHeight;
			currentY += currentDrawHeight;
		}
		
		// top + bottom
		int maxTopWidthInOneDrawCall = 220;
		int middleWidth = endX - startX - ELEMENT_WIDTH - ELEMENT_WIDTH;
		int currentX = startX + ELEMENT_WIDTH;
		while (middleWidth > 0) {
			int currentDrawWidth = Math.min(middleWidth, maxTopWidthInOneDrawCall);
			
			context.drawTexture(WINDOW_TEXTURE, currentX, startY, ELEMENT_WIDTH, 0, currentDrawWidth, TOP_ELEMENT_HEIGHT);
			context.drawTexture(WINDOW_TEXTURE, currentX, endY - BOTTOM_ELEMENT_HEIGHT, ELEMENT_WIDTH, 125, currentDrawWidth, BOTTOM_ELEMENT_HEIGHT);
			
			middleWidth -= currentDrawWidth;
			currentX += currentDrawWidth;
		}
	}
	
	@Override
	public void onRootAdded(PlacedAdvancement root) {
		int pinnedIndex = PaginatedAdvancementsClient.getPinIndex(root.getAdvancementEntry().id());
		PaginatedAdvancementTab advancementTab = PaginatedAdvancementTab.create(this.client, this, this.tabs.size(), pinnedIndex, root);
		if (advancementTab != null) {
			this.tabs.put(root.getAdvancementEntry(), advancementTab);
			if(PaginatedAdvancementsClient.isPinned(root.getAdvancementEntry().id())) {
				this.pinnedTabs.put(root.getAdvancementEntry(), advancementTab);
			}
		}
	}
	
	@Override
	public void onRootRemoved(PlacedAdvancement root) {
	}
	
	@Override
	public void onDependentAdded(PlacedAdvancement dependent) {
		PaginatedAdvancementTab advancementTab = this.getTab(dependent);
		if (advancementTab != null) {
			advancementTab.addAdvancement(dependent);
		}
	}
	
	@Override
	public void onDependentRemoved(PlacedAdvancement dependent) {
	}
	
	@Nullable
	private PaginatedAdvancementTab getTab(PlacedAdvancement advancement) {
		PlacedAdvancement placedAdvancement = advancement.getRoot();
		return this.tabs.get(placedAdvancement.getAdvancementEntry());
	}
	
	@Override
	public void onClear() {
		this.tabs.clear();
		this.pinnedTabs.clear();
		this.selectedTab = null;
	}
	
	@Override
	public void selectTab(@Nullable AdvancementEntry advancement) {
		this.selectedTab = this.tabs.get(advancement);
		if(this.selectedTab != null) {
			PaginatedAdvancementsClient.saveSelectedTab(this.selectedTab.getRoot().getAdvancementEntry().id());
		}
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			int startX = BORDER_PADDING;
			int endXWindow = !this.pinnedTabs.isEmpty() ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH : this.width - BORDER_PADDING;
			int endXTitle = this.width - BORDER_PADDING;
			int startY = BORDER_PADDING + ADDITIONAL_PADDING_TOP;
			int endY = this.height - BORDER_PADDING;
			
			boolean isPaginated = isPaginated(startX, endXWindow);
			
			if(this.selectedTab != null && isClickOnFavouritesButton(mouseX, mouseY, startY, endXWindow)) {
				Identifier pageIdentifier = this.selectedTab.getRoot().getAdvancementEntry().id();
				if(PaginatedAdvancementsClient.isPinned(pageIdentifier)) {
					unpinTab(pageIdentifier);
				} else {
					pinTab(pageIdentifier);
				}
			}
			
			if(isPaginated) {
				if (isClickOnBackTab(mouseX, mouseY, startX, endXTitle)) {
					pageBackward(startX, endXTitle, endXWindow);
				} else if (isClickOnForwardTab(mouseX, mouseY, startX, endXTitle)) {
					pageForward(startX, endXTitle, endXWindow);
				}
			}

			int maxDisplayedTabs = getMaxPaginatedTabsToRender(startX, endXTitle, endXWindow, isPaginated);
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.tabs.values()) {
				if (paginatedAdvancementTab.isClickOnTab(BORDER_PADDING, BORDER_PADDING + ADDITIONAL_PADDING_TOP, mouseX, mouseY, isPaginated, maxDisplayedTabs, currentPage)) {
					this.advancementHandler.selectTab(paginatedAdvancementTab.getRoot().getAdvancementEntry(), true);
					break;
				}
			}
			
			if(this.pinnedTabs.size() > 0) {
				int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);
				for (PaginatedAdvancementTab paginatedAdvancementTab : this.pinnedTabs.values()) {
					if (paginatedAdvancementTab.isClickOnPinnedTab(endXWindow, startY, mouseX, mouseY, maxPinnedTabs)) {
						this.advancementHandler.selectTab(paginatedAdvancementTab.getRoot().getAdvancementEntry(), true);
					}
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private void pinTab(Identifier pageIdentifier) {
		selectedTab.setPinIndex(this.pinnedTabs.size());
		this.pinnedTabs.put(selectedTab.getRoot().getAdvancementEntry(), selectedTab);
		PaginatedAdvancementsClient.pinTab(pageIdentifier);
	}
	
	private void unpinTab(Identifier pageIdentifier) {
		int oldPinIndex = selectedTab.getPinIndex();
		selectedTab.setPinIndex(-1);
		this.pinnedTabs.remove(selectedTab.getRoot().getAdvancementEntry());
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
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (this.client.options.advancementsKey.matchesKey(keyCode, scanCode)) {
			this.client.setScreen(null);
			this.client.mouse.lockCursor();
			return true;
		} else if (this.selectedTab != null && keyCode == InputUtil.GLFW_KEY_C && modifiers == 2) { // ctrl + c
			this.selectedTab.copyHoveredAdvancementID();
			return true;
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.selectedTab != null)
			return this.selectedTab.scrollDebug(-(int) verticalAmount);
		else
			return false;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (button != 0) {
			this.movingTab = false;
			return false;
		} else {
			if (!this.movingTab) {
				this.movingTab = true;
			} else if (this.selectedTab != null) {
				int endX = !this.pinnedTabs.isEmpty() ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH - 4 : this.width - BORDER_PADDING;
				int endY = this.height - BORDER_PADDING;
				
				this.selectedTab.move(deltaX, deltaY, endX - 60 + 5, endY - 84);
			}
			
			return true;
		}
	}
	
	private void drawWidgetTooltip(DrawContext context, int mouseX, int mouseY, int startX, int startY, int endXTitle, int endXWindow, int endY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.selectedTab != null) {
			context.getMatrices().push();
			context.getMatrices().translate((startX + 9), (startY + 18), 400.0D);
			this.selectedTab.drawWidgetTooltip(context, mouseX - startX - 9, mouseY - startY - 18, startX, startY, endXWindow, endY);
			
			context.getMatrices().translate(0, 0, 400.0D);
			if (PaginatedAdvancementsClient.CONFIG.shouldShowAdvancementDebug(this.client)) {
				this.selectedTab.drawDebugInfo(context, startX, startY, endXWindow, endY);
			}
			
			RenderSystem.disableDepthTest();
			context.getMatrices().pop();
			RenderSystem.applyModelViewMatrix();
		}
		
		if (this.tabs.size() > 1) {
			boolean isPaginated = isPaginated(startX, endXWindow);
			int maxDisplayedTabs = getMaxPaginatedTabsToRender(startX, endXTitle, endXWindow, isPaginated);
			
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.tabs.values()) {
				if (paginatedAdvancementTab.isClickOnTab(startX, startY, mouseX, mouseY, isPaginated, maxDisplayedTabs, currentPage)) {
					context.drawTooltip(this.textRenderer, paginatedAdvancementTab.getTitle(), mouseX, mouseY);
				}
			}
		}
		
		if(this.pinnedTabs.size() > 0) {
			int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.pinnedTabs.values()) {
				if (paginatedAdvancementTab.isClickOnPinnedTab(endXWindow, startY, mouseX, mouseY, maxPinnedTabs)) {
					context.drawTooltip(this.textRenderer, paginatedAdvancementTab.getTitle(), mouseX, mouseY);
				}
			}
		}
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		boolean hasPins = !this.pinnedTabs.isEmpty();
		int startX = BORDER_PADDING;
		int startY = BORDER_PADDING + ADDITIONAL_PADDING_TOP;
		int endXWindow = hasPins ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH : this.width - BORDER_PADDING;
		int endXTitle = this.width - BORDER_PADDING;
		int endY = this.height - BORDER_PADDING;
		
		clampCurrentPage(startX, endXTitle, endXWindow); // if the screen has been resized
		
		this.renderBackground(context, mouseX, mouseY, delta);
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
		this.drawPinButtonAndHeader(context, mouseX, mouseY, startX, startY, endXWindow, endY, hasPins);
		this.drawWidgetTooltip(context, mouseX, mouseY, startX, startY, endXTitle, endXWindow, endY);
	}
	
	private void drawAdvancementTree(DrawContext context, int startX, int startY, int endX, int endY) {
		PaginatedAdvancementTab advancementTab = this.selectedTab;
		if (advancementTab == null) {
			context.fill(startX + 9, startY + 18, endX, endY, -16777216);
			
			int textCenterX = startX + ((endX - startX) / 2);
			int textY = startY + ((endY - startY) / 2);
			context.drawCenteredTextWithShadow(this.textRenderer, EMPTY_TEXT, textCenterX, textY, -1);
			context.drawCenteredTextWithShadow(this.textRenderer, SAD_LABEL_TEXT, textCenterX, textY + 16, -1);
		} else {
			advancementTab.render(context, startX, startY, endX, endY);
		}
	}
	
	@Nullable
	public AdvancementWidget getAdvancementWidget(PlacedAdvancement advancement) {
		AdvancementTab advancementTab = this.getTab(advancement);
		return advancementTab == null ? null : advancementTab.getWidget(advancement.getAdvancementEntry());
	}
	
}
