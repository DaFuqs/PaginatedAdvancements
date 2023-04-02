package de.dafuqs.paginatedadvancements.client;

import net.minecraft.text.*;
import net.minecraft.util.*;

public enum PaginatedAdvancementFrame {
	
	NOTE("note", 0, 0, Formatting.UNDERLINE),
	PROGRESS("progress", 0, 78, Formatting.BLUE);
	
	private final String id;
	private final int textureV;
	private final int textureU;
	private final Formatting titleFormat;
	private final Text toastText;
	
	PaginatedAdvancementFrame(String id, int texU, int texV, Formatting titleFormat) {
		this.id = id;
		this.textureU = texU;
		this.textureV = texV;
		this.titleFormat = titleFormat;
		this.toastText = Text.translatable("advancements.toast." + id);
	}
	
	public String getId() {
		return this.id;
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
	
	
}
