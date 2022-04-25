package de.dafuqs.paginatedadvancements.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementTabType;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class PaginatedAdvancementTab extends AdvancementTab {
	
	public static final PaginatedAdvancementTabType PAGINATED_ADVANCEMENT_TAB_TYPE = new PaginatedAdvancementTabType();
	
	private final MinecraftClient client;
	private final PaginatedAdvancementScreen screen;
	private final int index;
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
	
	public PaginatedAdvancementTab(MinecraftClient client, PaginatedAdvancementScreen screen, int index, Advancement root, AdvancementDisplay display) {
		super(client, screen, AdvancementTabType.ABOVE, index, root, display);
		this.client = client;
		this.screen = screen;
		this.index = index;
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
	
	public void drawBackground(MatrixStack matrices, int x, int y, boolean selected) {
		PAGINATED_ADVANCEMENT_TAB_TYPE.drawBackground(matrices, this, x, y, selected, this.index);
	}
	
	public void drawIcon(int x, int y, ItemRenderer itemRenderer) {
		PAGINATED_ADVANCEMENT_TAB_TYPE.drawIcon(x, y, this.index, itemRenderer, this.icon);
	}
	
	public void render(MatrixStack matrices, int startX, int startY, int endX, int endY) {
		if (!this.initialized) {
			// the center of the advancement tree render at the start
			this.originX = (double)((((endX - startX) / 2)) - (this.maxPanX + this.minPanX) / 2);
			this.originY = (double)((((endY - startY) / 2)) - (this.maxPanY + this.minPanY) / 2);
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
		fill(matrices, endX - startX - 16, endY - startY - 26, 0, 0, -16777216);
		RenderSystem.depthFunc(515);
		Identifier identifier = this.display.getBackground();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, Objects.requireNonNullElse(identifier, TextureManager.MISSING_IDENTIFIER));
		
		int i = MathHelper.floor(this.originX);
		int j = MathHelper.floor(this.originY);
		int k = i % 16;
		int l = j % 16;
		
		int textureCountX = (endX - startX) / 16;
		int textureCountY = (endY - startY) / 16;
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
		fill(matrices, 0, 0, endX, endY, MathHelper.floor(this.alpha * 255.0F) << 24);
		boolean bl = false;
		int i = MathHelper.floor(this.originX);
		int j = MathHelper.floor(this.originY);
		if (mouseX > 0 && mouseX < endX - startX - 10 && mouseY > 0 && mouseY < endY - startY) {
			for (AdvancementWidget advancementWidget : this.widgets.values()) {
				if (advancementWidget.shouldRender(i, j, mouseX, mouseY)) {
					bl = true;
					advancementWidget.drawTooltip(matrices, i, j, this.alpha, startX, startY);
					break;
				}
			}
		}
		
		matrices.pop();
		if (bl) {
			this.alpha = MathHelper.clamp(this.alpha + 0.02F, 0.0F, 0.3F);
		} else {
			this.alpha = MathHelper.clamp(this.alpha - 0.04F, 0.0F, 1.0F);
		}
	}
	
	public boolean isClickOnTab(int screenX, int screenY, double mouseX, double mouseY) {
		return PAGINATED_ADVANCEMENT_TAB_TYPE.isClickOnTab(screenX, screenY, this.index, mouseX, mouseY);
	}
	
	@Nullable
	public static PaginatedAdvancementTab create(MinecraftClient client, PaginatedAdvancementScreen screen, int index, Advancement root) {
		if (root.getDisplay() != null) {
			for (AdvancementTabType advancementTabType : AdvancementTabType.values()) {
				if (index < advancementTabType.getTabCount()) {
					return new PaginatedAdvancementTab(client, screen, index, root, root.getDisplay());
				}
				
				index -= advancementTabType.getTabCount();
			}
		}
		return null;
	}
	
	public void move(double offsetX, double offsetY, int startX, int startY, int endX, int endY) {
		if (this.maxPanX - this.minPanX > endX) {
			this.originX = MathHelper.clamp(this.originX + offsetX + startX, (double)(-(this.maxPanX - endX)), 0.0D);
		}
		if (this.maxPanY - this.minPanY > endY) {
			this.originY = MathHelper.clamp(this.originY + offsetY + startY, (double)(-(this.maxPanY - endY)), 0.0D);
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
		int i = widget.getX();
		int j = i + 28;
		int k = widget.getY();
		int l = k + 27;
		this.minPanX = Math.min(this.minPanX, i);
		this.maxPanX = Math.max(this.maxPanX, j);
		this.minPanY = Math.min(this.minPanY, k);
		this.maxPanY = Math.max(this.maxPanY, l);
		
		for (AdvancementWidget advancementWidget : this.widgets.values()) {
			advancementWidget.addToTree();
		}
		
	}
	
	@Nullable
	public AdvancementWidget getWidget(Advancement advancement) {
		return this.widgets.get(advancement);
	}
	
	public PaginatedAdvancementScreen getScreen() {
		return this.screen;
	}
	
}
