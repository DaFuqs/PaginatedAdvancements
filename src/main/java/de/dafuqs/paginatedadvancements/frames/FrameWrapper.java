package de.dafuqs.paginatedadvancements.frames;

import de.dafuqs.paginatedadvancements.client.*;
import net.minecraft.advancement.*;
import net.minecraft.client.gui.screen.advancement.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

public abstract class FrameWrapper {
	
	public abstract int getItemOffsetX();
	public abstract int getItemOffsetY();
	
	public abstract Style getTitleStyle();
	
	public abstract Identifier getTexture(AdvancementObtainedStatus status, AdvancementFrame vanillaFrame);
	
	public static class VanillaFrameWrapper extends FrameWrapper {
		public final AdvancementFrame frame;
		
		private VanillaFrameWrapper(AdvancementFrame frame) {
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
			return Style.EMPTY.withFormatting(frame.getTitleFormat());
		}
		
		public Identifier getTexture(AdvancementObtainedStatus status, AdvancementFrame vanillaFrame) {
			return status.getFrameTexture(frame);
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
		
		public Identifier getTexture(AdvancementObtainedStatus status, AdvancementFrame vanillaFrame) {
			if (status == AdvancementObtainedStatus.OBTAINED) {
				return frame.getTextureObtained();
			}
			return frame.getTextureUnobtained();
		}
		
	}
	
	public static @Nullable FrameWrapper of(Identifier frame) {
		String path = frame.getPath();
		if (frame.getNamespace().equals("minecraft")) {
			for (AdvancementFrame vanillaFrame : AdvancementFrame.values()) {
				if (vanillaFrame.asString().equals(path)) {
					return new VanillaFrameWrapper(vanillaFrame);
				}
			}
		}
		
		@Nullable PaginatedAdvancementFrame paginatedFrame = AdvancementFrameTypeDataLoader.getFrameForAdvancement(frame);
		return paginatedFrame == null ? null : new PaginatedFrameWrapper(paginatedFrame);
	}
	
}
