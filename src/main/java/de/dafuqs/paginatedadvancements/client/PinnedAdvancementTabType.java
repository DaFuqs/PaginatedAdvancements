package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.*;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;
import net.minecraft.world.item.ItemStack;

public class PinnedAdvancementTabType {
	
	public static final int TOP_SPACING = 24; // accounting for the "pin" ribbon
	public static final int WIDTH = 32;
	public static final int HEIGHT = 28;
	
	protected static final Identifier RIGHT_TOP_TEXTURE_SELECTED = Identifier.withDefaultNamespace("advancements/tab_right_top_selected");
	protected static final Identifier RIGHT_MIDDLE_TEXTURE_SELECTED = Identifier.withDefaultNamespace("advancements/tab_right_middle_selected");
	protected static final Identifier RIGHT_TOP_TEXTURE = Identifier.withDefaultNamespace("advancements/tab_right_top");
	protected static final Identifier RIGHT_MIDDLE_TEXTURE = Identifier.withDefaultNamespace("advancements/tab_right_middle");
	
	public static int getHeightWithSpacing() {
		return HEIGHT + PaginatedAdvancementsClient.CONFIG.SpacingBetweenPinnedTabs; // includes the empty space between tabs
	}
	
	public static void drawBackground(GuiGraphics context, int x, int y, boolean selected, int index) {
		Identifier identifier;
		if (index == 0) {
			identifier = selected ? RIGHT_TOP_TEXTURE_SELECTED : RIGHT_TOP_TEXTURE;
		} else {
			identifier = selected ? RIGHT_MIDDLE_TEXTURE_SELECTED : RIGHT_MIDDLE_TEXTURE;
		}
		context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x + getTabX(), y + getTabY(index), WIDTH, HEIGHT);
		
		AdvancementTabType.RIGHT.draw(context, x + getTabX(), y + getTabY(index), selected, index);
	}
	
	public static void drawIcon(GuiGraphics context, int x, int y, int index, ItemStack stack) {
		int i = x + getTabX() + 6;
		int j = y + getTabY(index) + 5;
		context.renderFakeItem(stack, i, j);
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
