package de.dafuqs.paginatedadvancements.client;

import net.minecraft.text.*;
import net.minecraft.util.*;

public class PaginatedAdvancementFrame {
	
	private final Identifier id;
	private final Identifier textureSheet;
	private final int textureV;
	private final int textureU;
	private final int itemOffsetX;
	private final int itemOffsetY;
	private final Formatting titleFormat;
	private final Text toastText;
	
	public PaginatedAdvancementFrame(Identifier id, Identifier textureSheet, int texU, int texV, int itemOffsetX, int itemOffsetY, Formatting titleFormat) {
		this.id = id;
		this.textureSheet = textureSheet;
		this.textureU = texU;
		this.textureV = texV;
		this.itemOffsetX = itemOffsetX;
		this.itemOffsetY = itemOffsetY;
		this.titleFormat = titleFormat;
		this.toastText = Text.translatable("advancements.toast." + id);
	}
	
	public Identifier getId() {
		return this.id;
	}
	
	public Identifier getTextureSheet() {
		return this.textureSheet;
	}
	
	public int getTextureU() {
		return this.textureU;
	}
	
	public int getTextureV() {
		return this.textureV;
	}
	
	public Formatting getTitleFormat() {
		return this.titleFormat;
	}
	
	public Text getToastText() {
		return this.toastText;
	}
	
	public int getItemOffsetX() {
		return this.itemOffsetX;
	}
	
	public int getItemOffsetY() {
		return this.itemOffsetY;
	}
	
}
