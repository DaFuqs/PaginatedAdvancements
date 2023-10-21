package de.dafuqs.paginatedadvancements.client;

import net.minecraft.text.*;
import net.minecraft.util.*;

public class PaginatedAdvancementFrame {
	
	private final Identifier id;
	private final Identifier textureObtained;
	private final Identifier textureUnobtained;
	private final int itemOffsetX;
	private final int itemOffsetY;
	private final Formatting titleFormat;
	private final Text toastText;
	
	public PaginatedAdvancementFrame(Identifier id, Identifier textureObtained, Identifier textureUnobtained, int itemOffsetX, int itemOffsetY, Formatting titleFormat) {
		this.id = id;
		this.textureObtained = textureObtained;
		this.textureUnobtained = textureUnobtained;
		this.itemOffsetX = itemOffsetX;
		this.itemOffsetY = itemOffsetY;
		this.titleFormat = titleFormat;
		this.toastText = Text.translatable("advancements.toast." + id);
	}
	
	public Identifier getId() {
		return this.id;
	}
	
	public Identifier getTextureObtained() {
		return this.textureObtained;
	}
	
	public Identifier getTextureUnobtained() {
		return this.textureUnobtained;
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
