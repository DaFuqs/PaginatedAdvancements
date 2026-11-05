package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class PaginatedAdvancementTabType {
	
	public static final int WIDTH = 28;
	public static final int HEIGHT = 32;
	
	protected static final Identifier TOP_LEFT_TEXTURE_SELECTED = Identifier.withDefaultNamespace("advancements/tab_above_left_selected");
	protected static final Identifier TOP_MIDDLE_TEXTURE_SELECTED = Identifier.withDefaultNamespace("advancements/tab_above_middle_selected");
	protected static final Identifier TOP_LEFT_TEXTURE = Identifier.withDefaultNamespace("advancements/tab_above_left");
	protected static final Identifier TOP_MIDDLE_TEXTURE = Identifier.withDefaultNamespace("advancements/tab_above_middle");
	
	public static int getWidthWithSpacing() {
		return WIDTH + Math.clamp(PaginatedAdvancementsClient.CONFIG.SpacingBetweenHorizontalTabs, 0, Integer.MAX_VALUE - WIDTH); // includes the empty space between tabs
	}
	
	public static void drawBackground(GuiGraphicsExtractor context, int x, int y, boolean selected, int index) {
		Identifier texture = index == 0
				? (selected ? TOP_LEFT_TEXTURE_SELECTED : TOP_LEFT_TEXTURE)
				: (selected ? TOP_MIDDLE_TEXTURE_SELECTED : TOP_MIDDLE_TEXTURE);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x + getTabX(index), y + getTabY(), WIDTH, HEIGHT);
	}
	
	public static void drawIcon(GuiGraphicsExtractor context, int x, int y, int index, ItemStack stack) {
		context.fakeItem(stack, x + getTabX(index) + 6, y + getTabY() + 9);
	}
	
	public static int getTabX(int index) {
		return getWidthWithSpacing() * index;
	}
	
	public static int getTabY() {
        return -HEIGHT + 4;
    }

    public static boolean isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
		int tabX = screenX + getTabX(index);
		int tabY = screenY + getTabY();
		return mouseX > tabX && mouseX < tabX + WIDTH && mouseY > tabY && mouseY < tabY + HEIGHT;
    }
    
}
