package de.dafuqs.paginatedadvancements.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import de.dafuqs.paginatedadvancements.PaginatedAdvancementsClient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

public class PaginatedAdvancementScreen extends AdvancementsScreen implements ClientAdvancementManager.Listener {
	
	private static final Identifier PAGINATION_TEXTURE = new Identifier("paginatedadvancements", "textures/gui/buttons.png");
	
	private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
	private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
	
	private static final Text SAD_LABEL_TEXT = new TranslatableText("advancements.sad_label");
	private static final Text EMPTY_TEXT = new TranslatableText("advancements.empty");
	private static final Text ADVANCEMENTS_TEXT = new TranslatableText("gui.advancements");
	
	private final ClientAdvancementManager advancementHandler;
	private final Map<Advancement, PaginatedAdvancementTab> tabs = Maps.newLinkedHashMap();
	private final Map<Advancement, PaginatedAdvancementTab> pinnedTabs = Maps.newLinkedHashMap();
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
					if(advancementTab.getRoot().getId().equals(savedTabIdentifier)) {
						this.advancementHandler.selectTab(advancementTab.getRoot(), true);
						tabSelected = true;
						break;
					}
				}
			}
			if(!tabSelected) {
				// vanilla default behavior: just open some random tab
				this.advancementHandler.selectTab((this.tabs.values().iterator().next()).getRoot(), true);
			}
		} else {
			this.advancementHandler.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot(), true);
		}
		
		// initialize pinned tabs
		if(!this.tabs.isEmpty() && PaginatedAdvancementsClient.hasPins()) {
			for(String pinnedTabString : PaginatedAdvancementsClient.getPinnedTabs()) {
				Identifier pinnedTabIdentifier = Identifier.tryParse(pinnedTabString);
				for(PaginatedAdvancementTab advancementTab : this.tabs.values()) {
					if(advancementTab.getRoot().getId().equals(pinnedTabIdentifier)) {
						this.pinnedTabs.put(advancementTab.getRoot(), advancementTab);
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void removed() {
		super.removed();
		this.advancementHandler.setListener(null);
		ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
		if (clientPlayNetworkHandler != null) {
			clientPlayNetworkHandler.sendPacket(AdvancementTabC2SPacket.close());
		}
	}
	
	// instead of drawing the full texture here, we cut it into pieces and draw
	// the top, sides and more piece by piece, making the size variable with the mc window size
	public void drawWindow(MatrixStack matrices, int mouseX, int mouseY, int minWidth, int minHeight, int maxWidth, int maxHeight, boolean hasPins) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
		
		drawFrame(matrices, minWidth, minHeight, maxWidth, maxHeight);
		
		// draw tabs
		if (this.tabs.size() > 1) {
			drawTabs(matrices, mouseX, mouseY, minWidth, minHeight, maxWidth, maxHeight, hasPins);
		}

		this.textRenderer.draw(matrices, ADVANCEMENTS_TEXT, minWidth + 8, minHeight + 6, 4210752);
	}
	
	public void drawPinButton(MatrixStack matrices, int mouseX, int mouseY, int startY, int startX, int endX, int endY, boolean hasPins) {
		if(this.selectedTab != null) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, PAGINATION_TEXTURE);
			
			if(isClickOnFavouritesButton(mouseX, mouseY, startX, endX)) {
				if (PaginatedAdvancementsClient.isPinned(this.selectedTab.getRoot().getId())) {
					this.drawTexture(matrices, endX - FAVOURITES_BUTTON_OFFSET_X, startX + FAVOURITES_BUTTON_OFFSET_Y, FAVOURITES_BUTTON_WIDTH, 46 + FAVOURITES_BUTTON_HEIGHT, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT);
				} else {
					this.drawTexture(matrices, endX - FAVOURITES_BUTTON_OFFSET_X, startX + FAVOURITES_BUTTON_OFFSET_Y, 0, 46 + FAVOURITES_BUTTON_HEIGHT, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT);
				}
			} else {
				if (PaginatedAdvancementsClient.isPinned(this.selectedTab.getRoot().getId())) {
					this.drawTexture(matrices, endX - FAVOURITES_BUTTON_OFFSET_X, startX + FAVOURITES_BUTTON_OFFSET_Y, FAVOURITES_BUTTON_WIDTH, 46, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT);
				} else {
					this.drawTexture(matrices, endX - FAVOURITES_BUTTON_OFFSET_X, startX + FAVOURITES_BUTTON_OFFSET_Y, 0, 46, FAVOURITES_BUTTON_WIDTH, FAVOURITES_BUTTON_HEIGHT);
				}
			}
			
			if(hasPins) {
				// draw pinned tab header
				this.drawTexture(matrices, endX + PinnedAdvancementTabType.getTabX() + 1, startY + 26, 46, 0, 32, 15);
			}
		}
	}
	
	public boolean isClickOnFavouritesButton(double mouseX, double mouseY, int minHeight, int maxWidth) {
		return mouseX > maxWidth - FAVOURITES_BUTTON_OFFSET_X && mouseX < maxWidth - FAVOURITES_BUTTON_OFFSET_X + FAVOURITES_BUTTON_WIDTH && mouseY > minHeight + FAVOURITES_BUTTON_OFFSET_Y && mouseY < minHeight + FAVOURITES_BUTTON_OFFSET_Y + FAVOURITES_BUTTON_HEIGHT;
	}
	
	public int getMaxPaginatedTabsToRender(int minWidth, int maxWidth, boolean paginated) {
		int usableWidth = maxWidth - minWidth;
		if(paginated) {
			return (usableWidth - 58) / PaginatedAdvancementTabType.getWidthWithSpacing(); // room for forward and back button
		} else {
			return usableWidth / PaginatedAdvancementTabType.getWidthWithSpacing();
		}
	}
	
	public int getMaxPinnedTabsToRender(int minHeight, int maxHeight) {
		int usableHeight = maxHeight - minHeight;
		return (usableHeight - 56) / PinnedAdvancementTabType.getHeightWithSpacing(); // room for pin button + spacing
	}
	
	private boolean isPaginated() {
		if(tabs.isEmpty()) {
			return false; // fast fail
		} else {
			int startX = BORDER_PADDING;
			int endX = this.width - BORDER_PADDING - BORDER_PADDING;
			return endX - startX < (tabs.size() -1) * PaginatedAdvancementTabType.getWidthWithSpacing();
		}
	}
	
	private void drawTabs(MatrixStack matrices, int mouseX, int mouseY, int minWidth, int minHeight, int maxWidth, int maxHeight, boolean hasPins) {
		clampCurrentPage(minWidth, maxWidth); // if the screen has been resized
		if(isPaginated()) { // overflows
			// draw forward and back button tabs, fill the rest with the remaining tabs
			drawPaginationButtons(matrices, mouseX, mouseY, minWidth, maxWidth);
			renderTabs(matrices, minWidth, minHeight, maxWidth, true);
		} else {
			renderTabs(matrices, minWidth, minHeight, maxWidth, false);
		}
		if(hasPins) {
			renderPinnedTabs(matrices, minWidth, minHeight, maxWidth, maxHeight);
		}
	}
	
	private void renderTabs(MatrixStack matrices, int startX, int startY, int endX, boolean paginated) {
		Iterator<PaginatedAdvancementTab> tabIterator = this.tabs.values().iterator();
		int maxAdvancementTabsToRender = getMaxPaginatedTabsToRender(startX, endX, paginated);
		
		RenderSystem.setShaderTexture(0, TABS_TEXTURE);
		
		int index = 0;
		PaginatedAdvancementTab advancementTab;
		while(tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			if(paginated) {
				if(advancementTab.getPaginatedDisplayedPage(maxAdvancementTabsToRender) == this.currentPage) {
					int displayedPosition = advancementTab.getPaginatedDisplayedPosition(maxAdvancementTabsToRender, this.currentPage);
					advancementTab.drawBackground(matrices, startX, startY, advancementTab == this.selectedTab, displayedPosition);
				}
			} else {
				advancementTab.drawBackground(matrices, startX, startY, advancementTab == this.selectedTab, index);
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
					advancementTab.drawIcon(startX, startY, this.itemRenderer, displayedPosition);
				}
			} else {
				advancementTab.drawIcon(startX, startY, this.itemRenderer, index);
				index++;
			}
		}
		RenderSystem.disableBlend();
	}
	
	private void renderPinnedTabs(MatrixStack matrices, int startX, int startY, int endX, int endY) {
		int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);
		
		Iterator<PaginatedAdvancementTab> tabIterator = this.pinnedTabs.values().iterator();
		RenderSystem.setShaderTexture(0, TABS_TEXTURE);

		PaginatedAdvancementTab advancementTab;
		while(tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			advancementTab.drawPinnedBackground(matrices, endX, startY, advancementTab == this.selectedTab, maxPinnedTabs);
		}
		
		RenderSystem.defaultBlendFunc();
		tabIterator = this.pinnedTabs.values().iterator();
		while(tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			advancementTab.drawPinnedIcon(endX, startY, this.itemRenderer, maxPinnedTabs);
		}
		RenderSystem.disableBlend();
	}
	
	// instead of drawing the full texture here, we cut it into pieces and draw
	// the top, sides and more piece by piece, making the size variable with the mc window size
	public void drawPaginationButtons(MatrixStack matrices, int mouseX, int mouseY, int minWidth, int maxWidth) {
		matrices.push();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, PAGINATION_TEXTURE);
		
		if(isClickOnBackTab(mouseX, mouseY, minWidth, maxWidth)) {
			// hover
			this.drawTexture(matrices, minWidth + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 0, 23, 23, 23);
		} else {
			// no hover
			this.drawTexture(matrices, minWidth + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 0, 0, 23, 23);
		}
		
		if(isClickOnForwardTab(mouseX, mouseY, minWidth, maxWidth)) {
			// hover
			this.drawTexture(matrices, maxWidth - minWidth + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 23, 23, 23, 23);
		} else {
			// no hover
			this.drawTexture(matrices, maxWidth - minWidth + 4, TOP_ELEMENT_HEIGHT + ADDITIONAL_PADDING_TOP - 15, 23, 0, 23, 23);
		}
		
		matrices.pop();
		RenderSystem.applyModelViewMatrix();
	}
	
	public static boolean isClickOnBackTab(double mouseX, double mouseY, int minWidth, int maxWidth) {
		int startX = minWidth + 4;
		int startY = PaginatedAdvancementScreen.TOP_ELEMENT_HEIGHT + PaginatedAdvancementScreen.ADDITIONAL_PADDING_TOP - 15;
		return mouseX > startX && mouseX < startX + 23 && mouseY > startY && mouseY < startY + 23;
	}
	
	public static boolean isClickOnForwardTab(double mouseX, double mouseY, int minWidth, int maxWidth) {
		int startX = maxWidth - minWidth + 4;
		int startY = PaginatedAdvancementScreen.TOP_ELEMENT_HEIGHT + PaginatedAdvancementScreen.ADDITIONAL_PADDING_TOP - 15;
		return mouseX > startX && mouseX < startX + 23 && mouseY > startY && mouseY < startY + 23;
	}
	
	private void drawFrame(MatrixStack matrices, int startX, int startY, int endX, int endY) {
		// corners
		this.drawTexture(matrices, startX, startY, 0, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT); // top left
		this.drawTexture(matrices, endX - ELEMENT_WIDTH, startY, 237, 0, ELEMENT_WIDTH, TOP_ELEMENT_HEIGHT); // top right
		this.drawTexture(matrices, startX, endY - BOTTOM_ELEMENT_HEIGHT, 0, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT); // bottom left
		this.drawTexture(matrices, endX - ELEMENT_WIDTH, endY - BOTTOM_ELEMENT_HEIGHT, 237, 125, ELEMENT_WIDTH, BOTTOM_ELEMENT_HEIGHT); // bottom right
		
		// left + right sides
		int maxTopHeightInOneDrawCall = 100;
		int middleHeight = endY - startY - TOP_ELEMENT_HEIGHT - BOTTOM_ELEMENT_HEIGHT;
		int currentY = startY + TOP_ELEMENT_HEIGHT;
		while (middleHeight > 0) {
			int currentDrawHeight = Math.min(middleHeight, maxTopHeightInOneDrawCall);
			
			this.drawTexture(matrices, startX, currentY, 0, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight);
			this.drawTexture(matrices, endX - ELEMENT_WIDTH, currentY, 237, TOP_ELEMENT_HEIGHT, ELEMENT_WIDTH, currentDrawHeight);
			
			middleHeight -= currentDrawHeight;
			currentY += currentDrawHeight;
		}
		
		// top + bottom
		int maxTopWidthInOneDrawCall = 220;
		int middleWidth = endX - startX - ELEMENT_WIDTH - ELEMENT_WIDTH;
		int currentX = startX + ELEMENT_WIDTH;
		while (middleWidth > 0) {
			int currentDrawWidth = Math.min(middleWidth, maxTopWidthInOneDrawCall);
			
			this.drawTexture(matrices, currentX, startY, ELEMENT_WIDTH, 0, currentDrawWidth, TOP_ELEMENT_HEIGHT);
			this.drawTexture(matrices, currentX, endY - BOTTOM_ELEMENT_HEIGHT, ELEMENT_WIDTH, 125, currentDrawWidth, BOTTOM_ELEMENT_HEIGHT);
			
			middleWidth -= currentDrawWidth;
			currentX += currentDrawWidth;
		}
	}
	
	@Override
	public void onRootAdded(Advancement root) {
		int pinnedIndex = PaginatedAdvancementsClient.getPinIndex(root.getId());
		PaginatedAdvancementTab advancementTab = PaginatedAdvancementTab.create(this.client, this, this.tabs.size(), pinnedIndex, root);
		if (advancementTab != null) {
			this.tabs.put(root, advancementTab);
			if(PaginatedAdvancementsClient.isPinned(root.getId())) {
				this.pinnedTabs.put(root, advancementTab);
			}
		}
	}
	
	@Override
	public void onRootRemoved(Advancement root) {
	}
	
	@Override
	public void onDependentAdded(Advancement dependent) {
		PaginatedAdvancementTab advancementTab = this.getTab(dependent);
		if (advancementTab != null) {
			advancementTab.addAdvancement(dependent);
		}
	}
	
	@Override
	public void onDependentRemoved(Advancement dependent) {
	}
	
	@Nullable
	private PaginatedAdvancementTab getTab(Advancement advancement) {
		while(advancement.getParent() != null) {
			advancement = advancement.getParent();
		}
		
		return this.tabs.get(advancement);
	}
	
	@Override
	public void onClear() {
		this.tabs.clear();
		this.pinnedTabs.clear();
		this.selectedTab = null;
	}
	
	@Override
	public void setProgress(Advancement advancement, AdvancementProgress progress) {
		AdvancementWidget advancementWidget = this.getAdvancementWidget(advancement);
		if (advancementWidget != null) {
			advancementWidget.setProgress(progress);
		}
	}
	
	@Nullable
	public AdvancementWidget getAdvancementWidget(Advancement advancement) {
		PaginatedAdvancementTab advancementTab = this.getTab(advancement);
		return advancementTab == null ? null : advancementTab.getWidget(advancement);
	}
	
	@Override
	public void selectTab(@Nullable Advancement advancement) {
		this.selectedTab = this.tabs.get(advancement);
		if(this.selectedTab != null) {
			PaginatedAdvancementsClient.saveSelectedTab(this.selectedTab.getRoot().getId());
		}
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean isPaginated = isPaginated();
		
		if (button == 0) {
			int startX = BORDER_PADDING;
			int endX = PaginatedAdvancementsClient.hasPins() ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH - 4 : this.width - BORDER_PADDING;
			int startY = BORDER_PADDING + ADDITIONAL_PADDING_TOP;
			int endY = this.height - BORDER_PADDING;
			
			if(this.selectedTab != null && isClickOnFavouritesButton(mouseX, mouseY, startY, endX)) {
				Identifier pageIdentifier = this.selectedTab.getRoot().getId();
				if(PaginatedAdvancementsClient.isPinned(pageIdentifier)) {
					unpinTab(pageIdentifier);
				} else {
					pinTab(pageIdentifier);
				}
			}
			
			if(isPaginated) {
				if (isClickOnBackTab(mouseX, mouseY, startX, endX)) {
					pageBackward(startX, endX);
				} else if (isClickOnForwardTab(mouseX, mouseY, startX, endX)) {
					pageForward(startX, endX);
				}
			}

			int maxDisplayedTabs = getMaxPaginatedTabsToRender(startX, endX, isPaginated);
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.tabs.values()) {
				if (paginatedAdvancementTab.isClickOnTab(BORDER_PADDING, BORDER_PADDING + ADDITIONAL_PADDING_TOP, mouseX, mouseY, isPaginated, maxDisplayedTabs, currentPage)) {
					this.advancementHandler.selectTab(paginatedAdvancementTab.getRoot(), true);
					break;
				}
			}
			
			if(this.pinnedTabs.size() > 0) {
				int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);
				for (PaginatedAdvancementTab paginatedAdvancementTab : this.pinnedTabs.values()) {
					if (paginatedAdvancementTab.isClickOnPinnedTab(endX, startY, mouseX, mouseY, maxPinnedTabs)) {
						this.advancementHandler.selectTab(paginatedAdvancementTab.getRoot(), true);
					}
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	private void pinTab(Identifier pageIdentifier) {
		selectedTab.setPinIndex(this.pinnedTabs.size());
		this.pinnedTabs.put(selectedTab.getRoot(), selectedTab);
		PaginatedAdvancementsClient.pinTab(pageIdentifier);
	}
	
	private void unpinTab(Identifier pageIdentifier) {
		int oldPinIndex = selectedTab.getPinIndex();
		selectedTab.setPinIndex(-1);
		this.pinnedTabs.remove(selectedTab.getRoot());
		PaginatedAdvancementsClient.unpinTab(pageIdentifier);
		
		// move all pinned tabs with a pin index > this up by 1 to fill its place
		for(PaginatedAdvancementTab tab : this.pinnedTabs.values()) {
			int currentPinIndex = tab.getPinIndex();
			if(currentPinIndex > oldPinIndex) {
				tab.setPinIndex(currentPinIndex -1);
			}
		}
	}
	
	public int getMaxPageIndex(int startX, int endX) {
		int maxDisplayedTabsPerPage = getMaxPaginatedTabsToRender(startX, endX, true);
		return (this.tabs.size() - 1) / maxDisplayedTabsPerPage;
	}
	
	public void clampCurrentPage(int startX, int endX) {
		this.currentPage = Math.min(this.currentPage, getMaxPageIndex(startX, endX));
	}
	
	public void pageForward(int startX, int endX) {
		this.currentPage++;
		this.currentPage = this.currentPage % (getMaxPageIndex(startX, endX) + 1);
	}
	
	public void pageBackward(int startX, int endX) {
		int maxPageIndex = getMaxPageIndex(startX, endX);
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
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}
	
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (button != 0) {
			this.movingTab = false;
			return false;
		} else {
			if (!this.movingTab) {
				this.movingTab = true;
			} else if (this.selectedTab != null) {
				int endX = PaginatedAdvancementsClient.hasPins() ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH - 4 : this.width - BORDER_PADDING;
				int endY = this.height - BORDER_PADDING;
				
				this.selectedTab.move(deltaX, deltaY, endX - 60 + 5, endY - 84);
			}
			
			return true;
		}
	}
	
	private void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int startX, int startY, int endX, int endY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.selectedTab != null) {
			MatrixStack matrixStack = RenderSystem.getModelViewStack();
			matrixStack.push();
			matrixStack.translate((startX + 9), (startY + 18), 400.0D);
			RenderSystem.applyModelViewMatrix();
			RenderSystem.enableDepthTest();
			this.selectedTab.drawWidgetTooltip(matrices, mouseX - startX - 9, mouseY - startY - 18, startX, startY, endX, endY);
			RenderSystem.disableDepthTest();
			matrixStack.pop();
			RenderSystem.applyModelViewMatrix();
		}
		
		if (this.tabs.size() > 1) {
			boolean isPaginated = isPaginated();
			int maxDisplayedTabs = getMaxPaginatedTabsToRender(startX, endX, isPaginated);
			
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.tabs.values()) {
				if (paginatedAdvancementTab.isClickOnTab(startX, startY, mouseX, mouseY, isPaginated, maxDisplayedTabs, currentPage)) {
					this.renderTooltip(matrices, paginatedAdvancementTab.getTitle(), mouseX, mouseY);
				}
			}
		}
		
		if(this.pinnedTabs.size() > 0) {
			int maxPinnedTabs = getMaxPinnedTabsToRender(startY, endY);
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.pinnedTabs.values()) {
				if (paginatedAdvancementTab.isClickOnPinnedTab(endX, startY, mouseX, mouseY, maxPinnedTabs)) {
					this.renderTooltip(matrices, paginatedAdvancementTab.getTitle(), mouseX, mouseY);
				}
			}
		}
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		boolean hasPins = PaginatedAdvancementsClient.hasPins();
		int startX = BORDER_PADDING;
		int startY = BORDER_PADDING + ADDITIONAL_PADDING_TOP;
		int endX = hasPins ? this.width - BORDER_PADDING - PinnedAdvancementTabType.WIDTH - 4 : this.width - BORDER_PADDING;
		int endY = this.height - BORDER_PADDING;
		
		this.renderBackground(matrices);
		this.drawAdvancementTree(matrices, startX, startY, endX, endY);
		this.drawWindow(matrices, mouseX, mouseY, startX, startY, endX, endY, hasPins);
		this.drawPinButton(matrices, mouseX, mouseY, startX, startY, endX, endY, hasPins);
		this.drawWidgetTooltip(matrices, mouseX, mouseY, startX, startY, endX, endY);
	}
	
	private void drawAdvancementTree(MatrixStack matrices, int startX, int startY, int endX, int endY) {
		PaginatedAdvancementTab advancementTab = this.selectedTab;
		if (advancementTab == null) {
			fill(matrices, startX + 9, startY + 18, endX, endY, -16777216);
			
			int textCenterX = startX + ((endX-startX) / 2);
			int textY = startY + ((endY-startY) / 2);
			drawCenteredText(matrices, this.textRenderer, EMPTY_TEXT, textCenterX, textY, -1);
			drawCenteredText(matrices, this.textRenderer, SAD_LABEL_TEXT, textCenterX, textY + 16, -1);
		} else {
			MatrixStack matrixStack = RenderSystem.getModelViewStack();
			matrixStack.push();
			matrixStack.translate((startX + 9), (startY + 18), 0.0D);
			RenderSystem.applyModelViewMatrix();
			advancementTab.render(matrices, startX, startY, endX, endY);
			matrixStack.pop();
			RenderSystem.applyModelViewMatrix();
			RenderSystem.depthFunc(515);
			RenderSystem.disableDepthTest();
		}
	}
	
}
