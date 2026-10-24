package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.*;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.gui.*;
import net.minecraft.world.item.ItemStack;

public class PinnedAdvancementTabType {
	
	public static final int TOP_SPACING = 24; // accounting for the "pin" ribbon
	public static final int WIDTH = 32;
	public static final int HEIGHT = 28;
	
	public static int getHeightWithSpacing() {
		return HEIGHT + Math.clamp(PaginatedAdvancementsClient.CONFIG.SpacingBetweenPinnedTabs, 0, Integer.MAX_VALUE - HEIGHT); // includes the empty space between tabs
	}
	
	public static void drawBackground(GuiGraphicsExtractor context, int x, int y, boolean selected, int index) {
		AdvancementTabType.RIGHT.extractRenderState(context, x + getTabX(), y + getTabY(index), selected, index);
	}
	
	public static void drawIcon(GuiGraphicsExtractor context, int x, int y, int index, ItemStack stack) {
		context.fakeItem(stack, x + getTabX() + 6, y + getTabY(index) + 5);
	}
	
	public static int getTabX() {
		return WIDTH - PaginatedAdvancementScreen.BORDER_PADDING - 4;
	}
	
	public static int getTabY(int index) {
        return TOP_SPACING + getHeightWithSpacing() * index;
    }

    public static boolean isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
		int tabX = screenX + getTabX();
		int tabY = screenY + getTabY(index);
		return mouseX > tabX && mouseX < tabX + WIDTH && mouseY > tabY && mouseY < tabY + HEIGHT;
    }
    
}
