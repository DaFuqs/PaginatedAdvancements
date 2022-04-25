package de.dafuqs.paginatedadvancements.client;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class PaginatedAdvancementTabType {
    
    private final int WIDTH = 28;
    private final int HEIGHT = 32;
    private final int TAB_COUNT = 8;

    public void drawBackground(MatrixStack matrices, DrawableHelper tab, int x, int y, boolean selected, int index) {
        int i = 0;
        if (index > 0) {
            i += this.WIDTH;
        }

        if (index == this.TAB_COUNT - 1) {
            i += this.WIDTH;
        }
    
        int v = 0;
        int j = selected ? v + this.HEIGHT : v;
        tab.drawTexture(matrices, x + this.getTabX(index), y + this.getTabY(), i, j, this.WIDTH, this.HEIGHT);
    }

    public void drawIcon(int x, int y, int index, ItemRenderer itemRenderer, ItemStack icon) {
        int i = x + this.getTabX(index) + 6;
        int j = y + this.getTabY() + 9;
        itemRenderer.renderInGui(icon, i, j);
    }

    public int getTabX(int index) {
        return (this.WIDTH + 4) * index;
    }

    public int getTabY() {
        return -this.HEIGHT + 4;
    }

    public boolean isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
        int i = screenX + this.getTabX(index);
        int j = screenY + this.getTabY();
        return mouseX > (double)i && mouseX < (double)(i + this.WIDTH) && mouseY > (double)j && mouseY < (double)(j + this.HEIGHT);
    }
    
}
