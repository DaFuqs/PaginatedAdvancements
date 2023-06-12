package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.item.*;

public class PinnedAdvancementTabType {
	
	public static final int TOP_SPACING = 24; // accounting for the "pin" ribbon
	public static final int WIDTH = 32;
	public static final int HEIGHT = 28;
	
	public static int getHeightWithSpacing() {
		return HEIGHT + PaginatedAdvancementsClient.CONFIG.SpacingBetweenPinnedTabs; // includes the empty space between tabs
	}
	
	public static void drawBackground(DrawContext context, int x, int y, boolean selected, int index) {
		int i = index > 0 ? WIDTH + 96 : 96;
		int j = selected ? 64 + HEIGHT : 64;
		context.drawTexture(AdvancementsScreen.TABS_TEXTURE, x + getTabX(), y + getTabY(index), i, j, WIDTH, HEIGHT);
	}
	
	public static void drawIcon(DrawContext context, int x, int y, int index, ItemStack stack) {
		int i = x + getTabX() + 6;
		int j = y + getTabY(index) + 5;
		context.drawItemWithoutEntity(stack, i, j);
	}
	
	public static int getTabX() {
		return WIDTH - PaginatedAdvancementScreen.BORDER_PADDING - 4;
	}
	
	public static int getTabY(int index) {
        return TOP_SPACING + getHeightWithSpacing() * index;
    }

    public static boolean isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
        int i = screenX + getTabX();
        int j = screenY + getTabY(index);
        return mouseX > (double)i && mouseX < (double)(i + WIDTH) && mouseY > (double)j && mouseY < (double)(j + HEIGHT);
    }
    
}
