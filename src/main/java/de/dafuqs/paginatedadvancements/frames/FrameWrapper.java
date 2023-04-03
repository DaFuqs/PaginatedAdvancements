package de.dafuqs.paginatedadvancements.frames;

import de.dafuqs.paginatedadvancements.client.*;
import net.minecraft.advancement.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

public abstract class FrameWrapper {
	
	public abstract Identifier getId();
	public abstract int getTextureU();
	public abstract int getTextureV();
	public abstract Formatting getTitleFormat();
	public abstract Identifier getTextureSheet();
	
	public static class VanillaFrameWrapper extends FrameWrapper {
		public final AdvancementFrame frame;
		
		private VanillaFrameWrapper(AdvancementFrame frame) {
			this.frame = frame;
		}
		
		@Override
		public Identifier getId() {
			return new Identifier(frame.getId());
		}
		
		@Override
		public int getTextureU() {
			return frame.getTextureV();
		}
		
		@Override
		public int getTextureV() {
			return 128;
		}
		
		@Override
		public Formatting getTitleFormat() {
			return frame.getTitleFormat();
		}
		
		@Override
		public Identifier getTextureSheet() {
			return new Identifier("textures/gui/advancements/widgets.png");
		}
	}
	
	public static class PaginatedFrameWrapper extends FrameWrapper {
		public final PaginatedAdvancementFrame frame;
		
		private PaginatedFrameWrapper(PaginatedAdvancementFrame frame) {
			this.frame = frame;
		}
		
		@Override
		public Identifier getId() {
			return frame.getId();
		}
		
		@Override
		public int getTextureU() {
			return frame.getTextureU();
		}
		
		@Override
		public int getTextureV() {
			return frame.getTextureV();
		}
		
		@Override
		public Formatting getTitleFormat() {
			return frame.getTitleFormat();
		}
		
		@Override
		public Identifier getTextureSheet() {
			return frame.getTextureSheet();
		}
	}
	
	public static @Nullable FrameWrapper of(Identifier frame) {
		String path = frame.getPath();
		if (frame.getNamespace().equals("minecraft")) {
			for (AdvancementFrame vanillaFrame : AdvancementFrame.values()) {
				if (vanillaFrame.getId().equals(path)) {
					return new VanillaFrameWrapper(vanillaFrame);
				}
			}
		}
		
		@Nullable PaginatedAdvancementFrame paginatedFrame = AdvancementFrameTypeDataLoader.get(frame);
		return paginatedFrame == null ? null : new PaginatedFrameWrapper(paginatedFrame);
	}
	
}
