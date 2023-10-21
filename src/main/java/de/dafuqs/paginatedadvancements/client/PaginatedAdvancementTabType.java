package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.*;
import net.minecraft.client.gui.*;
import net.minecraft.item.*;
import net.minecraft.util.*;

public class PaginatedAdvancementTabType {
	
	public static final int WIDTH = 28;
	public static final int HEIGHT = 32;
	
	protected static final Identifier TOP_LEFT_TEXTURE_SELECTED = new Identifier("advancements/tab_above_left_selected");
	protected static final Identifier TOP_MIDDLE_TEXTURE_SELECTED = new Identifier("advancements/tab_above_middle_selected");
	protected static final Identifier TOP_LEFT_TEXTURE = new Identifier("advancements/tab_above_left");
	protected static final Identifier TOP_MIDDLE_TEXTURE = new Identifier("advancements/tab_above_middle");
	
	public static int getWidthWithSpacing() {
		return WIDTH + PaginatedAdvancementsClient.CONFIG.SpacingBetweenHorizontalTabs; // includes the empty space between tabs
	}
	
	public static void drawBackground(DrawContext context, int x, int y, boolean selected, int index) {
		Identifier identifier;
		if (index == 0) {
			identifier = selected ? TOP_LEFT_TEXTURE_SELECTED : TOP_LEFT_TEXTURE;
		} else {
			identifier = selected ? TOP_MIDDLE_TEXTURE_SELECTED : TOP_MIDDLE_TEXTURE;
		}
		context.drawGuiTexture(identifier, x + getTabX(index), y + getTabY(), WIDTH, HEIGHT);
	}
	
	public static void drawIcon(DrawContext context, int x, int y, int index, ItemStack stack) {
		int i = x + getTabX(index) + 6;
		int j = y + getTabY() + 9;
		context.drawItemWithoutEntity(stack, i, j);
	}
	
	public static int getTabX(int index) {
		return getWidthWithSpacing() * index;
	}
	
	public static int getTabY() {
        return -HEIGHT + 4;
    }

    public static boolean isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
        int i = screenX + getTabX(index);
        int j = screenY + getTabY();
        return mouseX > (double)i && mouseX < (double)(i + WIDTH) && mouseY > (double)j && mouseY < (double)(j + HEIGHT);
    }
    
}
