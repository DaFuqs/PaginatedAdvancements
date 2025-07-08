package de.dafuqs.paginatedadvancements.frames;

import de.dafuqs.paginatedadvancements.client.*;
import net.minecraft.advancements.*;
import net.minecraft.client.gui.screens.advancements.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import org.jetbrains.annotations.*;

public abstract class FrameWrapper {
	
	public abstract int getItemOffsetX();
	public abstract int getItemOffsetY();
	
	public abstract Style getTitleStyle();
	
	public abstract ResourceLocation getTexture(AdvancementWidgetType status, AdvancementType vanillaFrame);
	
	public static class VanillaFrameWrapper extends FrameWrapper {
		public final AdvancementType frame;
		
		private VanillaFrameWrapper(AdvancementType frame) {
			this.frame = frame;
		}
		
		@Override
		public int getItemOffsetX() {
			return 0;
		}
		
		@Override
		public int getItemOffsetY() {
			return 0;
		}
		
		@Override
		public Style getTitleStyle() {
			return Style.EMPTY.applyFormat(frame.getChatColor());
		}
		
		public ResourceLocation getTexture(AdvancementWidgetType status, AdvancementType vanillaFrame) {
			return status.frameSprite(frame);
		}
		
	}
	
	public static class PaginatedFrameWrapper extends FrameWrapper {
		public final PaginatedAdvancementFrame frame;
		
		private PaginatedFrameWrapper(PaginatedAdvancementFrame frame) {
			this.frame = frame;
		}
		
		@Override
		public int getItemOffsetX() {
			return frame.getItemOffsetX();
		}
		
		@Override
		public int getItemOffsetY() {
			return frame.getItemOffsetY();
		}
		
		@Override
		public Style getTitleStyle() {
			return frame.getTitleStyle();
		}
		
		public ResourceLocation getTexture(AdvancementWidgetType status, AdvancementType vanillaFrame) {
			if (status == AdvancementWidgetType.OBTAINED) {
				return frame.getTextureObtained();
			}
			return frame.getTextureUnobtained();
		}
		
	}
	
	public static @Nullable FrameWrapper of(ResourceLocation frame) {
		String path = frame.getPath();
		if (frame.getNamespace().equals("minecraft")) {
			for (AdvancementType vanillaFrame : AdvancementType.values()) {
				if (vanillaFrame.getSerializedName().equals(path)) {
					return new VanillaFrameWrapper(vanillaFrame);
				}
			}
		}
		
		@Nullable PaginatedAdvancementFrame paginatedFrame = AdvancementFrameTypeDataLoader.getFrameForAdvancement(frame);
		return paginatedFrame == null ? null : new PaginatedFrameWrapper(paginatedFrame);
	}
	
}
