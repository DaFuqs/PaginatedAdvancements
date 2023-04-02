package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.PaginatedAdvancementsClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PinnedAdvancementTabType {
    
    public static final int TOP_SPACING = 24; // accounting for the "pin" ribbon
    public static final int WIDTH = 32;
    public static final int HEIGHT = 28;
    
    public static int getHeightWithSpacing() {
        return HEIGHT + PaginatedAdvancementsClient.CONFIG.SpacingBetweenPinnedTabs; // includes the empty space between tabs
    }
    
    public static void drawBackground(MatrixStack matrices, DrawableHelper tab, int x, int y, boolean selected, int index) {
        int i = index > 0 ? WIDTH + 96 : 96;
        int j = selected ? 64 + HEIGHT : 64;
        tab.drawTexture(matrices, x + getTabX(), y + getTabY(index), i, j, WIDTH, HEIGHT);
    }

    public static void drawIcon(MatrixStack matrices, int x, int y, int index, @NotNull ItemRenderer itemRenderer, ItemStack icon) {
        int i = x + getTabX() + 6;
        int j = y + getTabY(index) + 5;
        itemRenderer.renderInGui(matrices, icon, i, j);
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
