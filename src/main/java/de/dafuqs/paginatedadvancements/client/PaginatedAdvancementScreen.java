package de.dafuqs.paginatedadvancements.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
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
	
	private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
	private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");
	
	private static final Text SAD_LABEL_TEXT = new TranslatableText("advancements.sad_label");
	private static final Text EMPTY_TEXT = new TranslatableText("advancements.empty");
	private static final Text ADVANCEMENTS_TEXT = new TranslatableText("gui.advancements");
	
	private final ClientAdvancementManager advancementHandler;
	private final Map<Advancement, PaginatedAdvancementTab> tabs = Maps.newLinkedHashMap();
	@Nullable private PaginatedAdvancementTab selectedTab;
	private boolean movingTab;
	
	public static final int ADDITIONAL_PADDING_TOP = 20; // to account for the advancement tabs
	public static final int BORDER_PADDING = 32;
	public static final int ELEMENT_WIDTH = 15;
	public static final int TOP_ELEMENT_HEIGHT = 22;
	public static final int BOTTOM_ELEMENT_HEIGHT = 15;
	
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
			this.advancementHandler.selectTab((this.tabs.values().iterator().next()).getRoot(), true);
		} else {
			this.advancementHandler.selectTab(this.selectedTab == null ? null : this.selectedTab.getRoot(), true);
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
	public void drawWindow(MatrixStack matrices, int minWidth, int minHeight, int maxWidth, int maxHeight) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WINDOW_TEXTURE);
		

		
		drawFrame(matrices, minWidth, minHeight, maxWidth, maxHeight);
		
		// draw tabs
		if (this.tabs.size() > 1) {
			drawTabs(matrices, minWidth, minHeight);
		}
		
		this.textRenderer.draw(matrices, ADVANCEMENTS_TEXT, minWidth + 8, minHeight + 6, 4210752);
	}
	
	private void drawTabs(MatrixStack matrices, int minWidth, int minHeight) {
		RenderSystem.setShaderTexture(0, TABS_TEXTURE);
		Iterator<PaginatedAdvancementTab> tabIterator = this.tabs.values().iterator();
		
		PaginatedAdvancementTab advancementTab;
		while(tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			advancementTab.drawBackground(matrices, minWidth, minHeight, advancementTab == this.selectedTab);
		}
		
		RenderSystem.defaultBlendFunc();
		tabIterator = this.tabs.values().iterator();
		while(tabIterator.hasNext()) {
			advancementTab = tabIterator.next();
			advancementTab.drawIcon(minWidth, minHeight, this.itemRenderer);
		}
		
		RenderSystem.disableBlend();
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
		PaginatedAdvancementTab advancementTab = PaginatedAdvancementTab.create(this.client, this, this.tabs.size(), root);
		if (advancementTab != null) {
			this.tabs.put(root, advancementTab);
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
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.tabs.values()) {
				if (((AdvancementTab) paginatedAdvancementTab).isClickOnTab(BORDER_PADDING, BORDER_PADDING + ADDITIONAL_PADDING_TOP, mouseX, mouseY)) {
					this.advancementHandler.selectTab(((AdvancementTab) paginatedAdvancementTab).getRoot(), true);
					break;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
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
				int startX = BORDER_PADDING;
				int startY = BORDER_PADDING + ADDITIONAL_PADDING_TOP;
				int endX = this.width - BORDER_PADDING;
				int endY = this.height - BORDER_PADDING;
				
				this.selectedTab.move(deltaX, deltaY, startX, startY, endX, endY);
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
			for (PaginatedAdvancementTab paginatedAdvancementTab : this.tabs.values()) {
				if (((AdvancementTab) paginatedAdvancementTab).isClickOnTab(startX, startY, mouseX, mouseY)) {
					this.renderTooltip(matrices, ((AdvancementTab) paginatedAdvancementTab).getTitle(), mouseX, mouseY);
				}
			}
		}
	}
	
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int startX = BORDER_PADDING;
		int startY = BORDER_PADDING + ADDITIONAL_PADDING_TOP;
		int endX = this.width - BORDER_PADDING;
		int endY = this.height - BORDER_PADDING;
		
		this.renderBackground(matrices);
		this.drawAdvancementTree(matrices, startX, startY, endX, endY);
		this.drawWindow(matrices, startX, startY, endX, endY);
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
			//////////////////////////////////////
			// TODO FROM HERE
			//////////////////////////////////////
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
