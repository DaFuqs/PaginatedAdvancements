package de.dafuqs.paginatedadvancements.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import de.dafuqs.paginatedadvancements.PaginatedAdvancementsClient;
import de.dafuqs.paginatedadvancements.accessors.AdvancementProgressAccessor;
import de.dafuqs.paginatedadvancements.accessors.AdvancementWidgetAccessor;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementTabType;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

import static de.dafuqs.paginatedadvancements.client.PaginatedAdvancementScreen.*;

public class PaginatedAdvancementTab extends AdvancementTab {
	
	private final MinecraftClient client;
	private final PaginatedAdvancementScreen screen;
	private final int index;
	private int pinnedIndex;
	private final Advancement root;
	private final AdvancementDisplay display;
	private final ItemStack icon;
	private final Text title;
	private final AdvancementWidget rootWidget;
	private final Map<Advancement, AdvancementWidget> widgets = Maps.newLinkedHashMap();
	private double originX;
	private double originY;
	private int minPanX = 2147483647;
	private int minPanY = 2147483647;
	private int maxPanX = -2147483648;
	private int maxPanY = -2147483648;
	private float alpha;
	private boolean initialized;
	
	private @Nullable AdvancementWidget hoveredWidget;
	
	public PaginatedAdvancementTab(MinecraftClient client, PaginatedAdvancementScreen screen, int index, int pinnedIndex, Advancement root, AdvancementDisplay display) {
		super(client, screen, AdvancementTabType.ABOVE, index, root, display);
		this.client = client;
		this.screen = screen;
		this.index = index;
		this.pinnedIndex = pinnedIndex;
		this.root = root;
		this.display = display;
		this.icon = display.getIcon();
		this.title = display.getTitle();
		this.rootWidget = new AdvancementWidget(this, client, root, display);
		this.addWidget(this.rootWidget, root);
	}
	
	public AdvancementTabType getType() {
		return AdvancementTabType.ABOVE;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public Advancement getRoot() {
		return this.root;
	}
	
	public Text getTitle() {
		return this.title;
	}
	
	public AdvancementDisplay getDisplay() {
		return this.display;
	}
	
	public void drawBackground(MatrixStack matrices, int x, int y, boolean selected, int atIndex) {
		PaginatedAdvancementTabType.drawBackground(matrices, this, x, y, selected, atIndex);
	}
	
	public void drawIcon(int x, int y, ItemRenderer itemRenderer, int atIndex) {
		PaginatedAdvancementTabType.drawIcon(x, y, atIndex, itemRenderer, this.icon);
	}
	
	public void drawPinnedBackground(MatrixStack matrices, int x, int y, boolean selected, int maxPinnedIndex) {
		if(this.pinnedIndex <= maxPinnedIndex) {
			PinnedAdvancementTabType.drawBackground(matrices, this, x, y, selected, this.pinnedIndex);
		}
	}
	
	public void drawPinnedIcon(int x, int y, ItemRenderer itemRenderer, int maxPinnedIndex) {
		if(this.pinnedIndex <= maxPinnedIndex) {
			PinnedAdvancementTabType.drawIcon(x, y, this.pinnedIndex, itemRenderer, this.icon);
		}
	}
	
	public void render(MatrixStack matrices, int startX, int startY, int endX, int endY) {
		int advancementTreeWindowWidth  = endX -startX - 10;
		int advancementTreeWindowHeight = endY -startY - 20;
		
		if (!this.initialized) {
			// the center of the advancement tree render at the start
			this.originX = (double)((((advancementTreeWindowWidth) / 2)) - (this.maxPanX + this.minPanX) / 2);
			this.originY = (double)((((advancementTreeWindowHeight) / 2)) - (this.maxPanY + this.minPanY) / 2);
			this.initialized = true;
		}
		matrices.push();
		matrices.translate(0.0D, 0.0D, 950.0D);
		RenderSystem.enableDepthTest();
		RenderSystem.colorMask(false, false, false, false);
		fill(matrices, 4680, 2260, -4680, -2260, -16777216);
		RenderSystem.colorMask(true, true, true, true);
		matrices.translate(0.0D, 0.0D, -950.0D);
		RenderSystem.depthFunc(518);
		fill(matrices, advancementTreeWindowWidth, advancementTreeWindowHeight, 0, 0, -16777216);
		RenderSystem.depthFunc(515);
		Identifier identifier = this.display.getBackground();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, Objects.requireNonNullElse(identifier, TextureManager.MISSING_IDENTIFIER));
		
		int i = MathHelper.floor(this.originX);
		int j = MathHelper.floor(this.originY);
		int k = i % 16;
		int l = j % 16;
		
		int textureCountX = (advancementTreeWindowWidth) / 16 + 1;
		int textureCountY = (advancementTreeWindowHeight) / 16 + 1;
		for(int m = -1; m < textureCountX; ++m) {
			for(int n = -1; n < textureCountY; ++n) {
				drawTexture(matrices, k + 16 * m, l + 16 * n, 0.0F, 0.0F, 16, 16, 16, 16);
			}
		}
		
		this.rootWidget.renderLines(matrices, i, j, true);
		this.rootWidget.renderLines(matrices, i, j, false);
		this.rootWidget.renderWidgets(matrices, i, j);
		RenderSystem.depthFunc(518);
		matrices.translate(0.0D, 0.0D, -950.0D);
		RenderSystem.colorMask(false, false, false, false);
		fill(matrices, 4680, 2260, -4680, -2260, -16777216);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.depthFunc(515);
		matrices.pop();
	}
	
	public void drawWidgetTooltip(MatrixStack matrices, int mouseX, int mouseY, int startX, int startY, int endX, int endY) {
		matrices.push();
		matrices.translate(0.0D, 0.0D, -200.0D);
		
		// tinting the background slightly darker
		// (this is the vanilla default, but able to be disabled via config)
		if(PaginatedAdvancementsClient.CONFIG.FadeOutBackgroundOnAdvancementHover) {
			fill(matrices, 0, 0, endX - startX - 18, endY - startY - 26, MathHelper.floor(this.alpha * 255.0F) << 24);
		}
		
		boolean hoversWidget = false;
		int i = MathHelper.floor(this.originX);
		int j = MathHelper.floor(this.originY);
		if (mouseX > 0 && mouseX < endX - startX - 10 && mouseY > 0 && mouseY < endY - startY) {
			for (AdvancementWidget advancementWidget : this.widgets.values()) {
				if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
					hoversWidget = true;
					advancementWidget.drawTooltip(matrices, i, j, this.alpha, startX, startY);
					
					if(MinecraftClient.getInstance().options.advancedItemTooltips) {
						renderDebugCriteria(matrices, advancementWidget, i, j, this.alpha, startX, startY);
					}
					this.hoveredWidget = advancementWidget;
					
					break;
				}
			}
		}
		
		matrices.pop();
		if (hoversWidget) {
			this.alpha = MathHelper.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
		} else {
			this.hoveredWidget = null;
			this.alpha = MathHelper.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
		}
	}
	
	private void renderDebugCriteria(MatrixStack stack, AdvancementWidget widget, int originX, int originY, float alpha, int x, int y) {
		AdvancementWidgetAccessor advancementWidgetAccessor = ((AdvancementWidgetAccessor) widget);
		AdvancementProgress progress = advancementWidgetAccessor.getProgress();
		Iterable<String> obtainedCriteria = progress.getObtainedCriteria();
		
		AdvancementProgressAccessor advancementProgressAccessor = (AdvancementProgressAccessor) progress;
		String[][] requirements = advancementProgressAccessor.getRequirements();
		
		int startY = originY + 10;
		MinecraftClient.getInstance().textRenderer.draw(stack, "ID: " + advancementWidgetAccessor.getAdvancementID().toString(), originX, originY, 0xFFFFFF);
		
		for(String[] requirementGroup : requirements) {
			MutableText text = new LiteralText("Group: ").formatted(Formatting.RED);
			boolean anyDone = false;
			for(String requirementString : requirementGroup) {
				Formatting formatting = Formatting.RED;
				for(String s : obtainedCriteria) {
					if (s.equals(requirementString)) {
						formatting = Formatting.GREEN;
						anyDone = true;
						break;
					}
				}
				text.append(new LiteralText(requirementString + " ").formatted(formatting));
			}
			
			if(anyDone) {
				text.formatted(Formatting.GREEN);
			}
			
			MinecraftClient.getInstance().textRenderer.draw(stack, text, originX, startY, 0x00ff00);
			startY += 10;
		}
	}
	
	public void drawDebugFrame(MatrixStack matrices, int startX, int endX, int endY) {
		if(this.hoveredWidget != null) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, PaginatedAdvancementScreen.WINDOW_TEXTURE);
			
			AdvancementWidgetAccessor advancementWidgetAccessor = (AdvancementWidgetAccessor) this.hoveredWidget;
			AdvancementProgressAccessor advancementProgressAccessor = (AdvancementProgressAccessor) advancementWidgetAccessor.getProgress();
			
			startX = startX + 6;
			endX = endX - 6;
			endY = endY + 4;
			int startY = endY - BOTTOM_ELEMENT_HEIGHT - TOP_ELEMENT_HEIGHT - 12 * (1 + advancementProgressAccessor.getRequirements().length);
			
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
			
			// center
			int centerStartX = startX + 6;
			int centerEndX = endX - 6;
			int centerStartY = startY + 6;
			int centerEndY = endY - 6;
			
			int drawStartY = centerStartY;
			int drawHeight = centerEndY - centerStartY;
			while(drawHeight > 0) {
				int drawStartX = centerStartX;
				int currentHeight = Math.min(drawHeight, 10);
				int drawWidth = centerEndX - centerStartX;
				while(drawWidth > 0) {
					int currentWidth = Math.min(200, drawWidth);
					this.drawTexture(matrices, drawStartX, drawStartY, 4, 4, currentWidth, currentHeight);
					drawWidth -= currentWidth;
					drawStartX += currentWidth;
				}
				drawHeight -= currentHeight;
				drawStartY += currentHeight;
			}
		}
	}
	
	public int getPaginatedDisplayedPage(int maxDisplayedTabs) {
		return this.index / maxDisplayedTabs;
	}
	
	public int getPaginatedDisplayedPosition(int maxDisplayedTabs, int currentPage) {
		return 1 + this.index - maxDisplayedTabs * currentPage; // +1 because pos 0 is taken by the back button
	}
	
	public boolean isClickOnTab(int screenX, int screenY, double mouseX, double mouseY, boolean paginated, int maxDisplayedTabs, int currentPage) {
		if(paginated) {
			// check if the tab is on another page
			if(getPaginatedDisplayedPage(maxDisplayedTabs) != currentPage) {
				return false;
			}
			
			int pageIndex = getPaginatedDisplayedPosition(maxDisplayedTabs, currentPage);
			if(pageIndex <= maxDisplayedTabs) {
				return PaginatedAdvancementTabType.isClickOnTab(screenX, screenY, pageIndex, mouseX, mouseY);
			}
		} else {
			return PaginatedAdvancementTabType.isClickOnTab(screenX, screenY, this.index, mouseX, mouseY);
		}
		return false;
	}
	
	public boolean isClickOnPinnedTab(int screenX, int screenY, double mouseX, double mouseY, int maxPinnedTabs) {
		if(this.pinnedIndex > -1 && this.pinnedIndex <= maxPinnedTabs) {
			return PinnedAdvancementTabType.isClickOnTab(screenX, screenY, this.pinnedIndex, mouseX, mouseY);
		}
		return false;
	}
	
	@Nullable
	public static PaginatedAdvancementTab create(MinecraftClient client, PaginatedAdvancementScreen screen, int index, int pinnedIndex, Advancement root) {
		if (root.getDisplay() != null) {
			return new PaginatedAdvancementTab(client, screen, index, pinnedIndex, root, root.getDisplay());
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
	
	public void addAdvancement(Advancement advancement) {
		if (advancement.getDisplay() != null) {
			AdvancementWidget advancementWidget = new AdvancementWidget(this, this.client, advancement, advancement.getDisplay());
			this.addWidget(advancementWidget, advancement);
		}
	}
	
	private void addWidget(AdvancementWidget widget, Advancement advancement) {
		this.widgets.put(advancement, widget);
		for (AdvancementWidget advancementWidget : this.widgets.values()) {
			advancementWidget.addToTree();
		}
		calculatePan();
	}
	
	public void calculatePan() {
		for(AdvancementWidget widget : this.widgets.values()) {
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
	public AdvancementWidget getWidget(Advancement advancement) {
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
		if(this.hoveredWidget != null) {
			AdvancementWidgetAccessor awa = (AdvancementWidgetAccessor) this.hoveredWidget;
			MinecraftClient.getInstance().keyboard.setClipboard(awa.getAdvancementID().toString());
			MinecraftClient.getInstance().inGameHud.setOverlayMessage(new TranslatableText("text.paginated_advancements.copied_to_clipboard"), false);
		}
	}
	
}
