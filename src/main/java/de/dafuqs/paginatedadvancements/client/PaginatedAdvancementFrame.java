package de.dafuqs.paginatedadvancements.client;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.*;

public class PaginatedAdvancementFrame {
	
	public static final Codec<PaginatedAdvancementFrame> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			ExtraCodecs.intRange(-16, 16).optionalFieldOf("item_offset_x", 0).forGetter(PaginatedAdvancementFrame::getItemOffsetX),
			ExtraCodecs.intRange(-16, 16).optionalFieldOf("item_offset_y", 0).forGetter(PaginatedAdvancementFrame::getItemOffsetY),
			Style.Serializer.CODEC.optionalFieldOf("style", Style.EMPTY.applyFormat(ChatFormatting.GREEN)).forGetter(PaginatedAdvancementFrame::getTitleStyle)
	).apply(instance, PaginatedAdvancementFrame::new));
	
	protected final int itemOffsetX;
	protected final int itemOffsetY;
	protected final Style titleStyle;
	protected Component toastText;
	protected Identifier textureObtained;
	protected Identifier textureUnobtained;
	
	public PaginatedAdvancementFrame(int itemOffsetX, int itemOffsetY, Style titleStyle) {
		this.textureObtained = Identifier.parse("todo"); // Identifier.of(id.getNamespace(), "advancements/" + id.getPath() + "_obtained");
		this.textureUnobtained = Identifier.parse("todo"); // Identifier.of(id.getNamespace(), "advancements/" + id.getPath() + "_unobtained");
		this.itemOffsetX = itemOffsetX;
		this.itemOffsetY = itemOffsetY;
		this.titleStyle = titleStyle;
		this.toastText = Component.nullToEmpty("todo"); // Text.translatable("advancements.toast." + id);
	}
	
	public void setIdBasedData(Identifier id) {
		this.toastText = Component.translatable("advancements.toast." + id);
		this.textureObtained = Identifier.fromNamespaceAndPath(id.getNamespace(), "advancements/" + id.getPath() + "_obtained");
		this.textureUnobtained = Identifier.fromNamespaceAndPath(id.getNamespace(), "advancements/" + id.getPath() + "_unobtained");
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
	
	public Component getToastText() {
		return this.toastText;
	}
	
	public int getItemOffsetX() {
		return this.itemOffsetX;
	}
	
	public int getItemOffsetY() {
		return this.itemOffsetY;
	}
	
}
