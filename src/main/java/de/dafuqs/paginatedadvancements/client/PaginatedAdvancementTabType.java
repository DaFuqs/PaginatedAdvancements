package de.dafuqs.paginatedadvancements.client;

import de.dafuqs.paginatedadvancements.PaginatedAdvancementsClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PaginatedAdvancementTabType {
    
    public static final int WIDTH = 28;
    public static final int HEIGHT = 32;
    
    public static int getWidthWithSpacing() {
        return WIDTH + PaginatedAdvancementsClient.CONFIG.SpacingBetweenHorizontalTabs; // includes the empty space between tabs
    }

    public static void drawBackground(MatrixStack matrices, DrawableHelper tab, int x, int y, boolean selected, int index) {
        int i = index > 0 ? WIDTH : 0;
        int j = selected ? HEIGHT : 0;
        tab.drawTexture(matrices, x + getTabX(index), y + getTabY(), i, j, WIDTH, HEIGHT);
    }

    public static void drawIcon(MatrixStack matrices, int x, int y, int index, @NotNull ItemRenderer itemRenderer, ItemStack icon) {
        int i = x + getTabX(index) + 6;
        int j = y + getTabY() + 9;
        itemRenderer.renderInGui(matrices, icon, i, j);
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
