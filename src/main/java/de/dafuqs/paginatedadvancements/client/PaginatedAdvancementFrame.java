package de.dafuqs.paginatedadvancements.client;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.dynamic.*;

public class PaginatedAdvancementFrame {
	
	public static final Codec<PaginatedAdvancementFrame> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			Codecs.rangedInt(-16, 16).optionalFieldOf("item_offset_x", 0).forGetter(PaginatedAdvancementFrame::getItemOffsetX),
			Codecs.rangedInt(-16, 16).optionalFieldOf("item_offset_y", 0).forGetter(PaginatedAdvancementFrame::getItemOffsetY),
			Style.Codecs.CODEC.optionalFieldOf("style", Style.EMPTY.withFormatting(Formatting.GREEN)).forGetter(PaginatedAdvancementFrame::getTitleStyle)
	).apply(instance, PaginatedAdvancementFrame::new));
	
	protected final int itemOffsetX;
	protected final int itemOffsetY;
	protected final Style titleStyle;
	protected Text toastText;
	protected Identifier textureObtained;
	protected Identifier textureUnobtained;
	
	public PaginatedAdvancementFrame(int itemOffsetX, int itemOffsetY, Style titleStyle) {
		this.textureObtained = Identifier.of("todo"); // Identifier.of(id.getNamespace(), "advancements/" + id.getPath() + "_obtained");
		this.textureUnobtained = Identifier.of("todo"); // Identifier.of(id.getNamespace(), "advancements/" + id.getPath() + "_unobtained");
		this.itemOffsetX = itemOffsetX;
		this.itemOffsetY = itemOffsetY;
		this.titleStyle = titleStyle;
		this.toastText = Text.of("todo"); // Text.translatable("advancements.toast." + id);
	}
	
	public void setIdBasedData(Identifier id) {
		this.toastText = Text.translatable("advancements.toast." + id);
		this.textureObtained = Identifier.of(id.getNamespace(), "advancements/" + id.getPath() + "_obtained");
		this.textureUnobtained = Identifier.of(id.getNamespace(), "advancements/" + id.getPath() + "_unobtained");
	}
	
	public Identifier getTextureObtained() {
		return this.textureObtained;
	}
	
	public Identifier getTextureUnobtained() {
		return this.textureUnobtained;
	}
	
	public Style getTitleStyle() {
		return this.titleStyle;
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
