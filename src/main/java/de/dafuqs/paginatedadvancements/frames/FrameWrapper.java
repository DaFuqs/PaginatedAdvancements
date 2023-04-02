package de.dafuqs.paginatedadvancements.frames;

import de.dafuqs.paginatedadvancements.*;
import de.dafuqs.paginatedadvancements.client.*;
import net.minecraft.advancement.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

public abstract class FrameWrapper {
	
	public abstract String getId();
	
	public abstract int getTextureU();
	
	public abstract int getTextureV();
	
	public abstract Formatting getTitleFormat();
	
	public abstract Text getToastText();
	
	public abstract Identifier getTextureSheet();
	
	public static class VanillaFrameWrapper extends FrameWrapper {
		public final AdvancementFrame frame;
		
		private VanillaFrameWrapper(AdvancementFrame frame) {
			this.frame = frame;
		}
		
		@Override
		public String getId() {
			return frame.getId();
		}
		
		@Override
		public int getTextureU() {
			return 128;
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
		public Text getToastText() {
			return frame.getToastText();
		}
		
		@Override
		public Identifier getTextureSheet() {
			return new Identifier("textures/gui/advancements/widgets.png");
		}
	}
	
	public static class ExtendedFrameWrapper extends FrameWrapper {
		public final PaginatedAdvancementFrame frame;
		
		private ExtendedFrameWrapper(PaginatedAdvancementFrame frame) {
			this.frame = frame;
		}
		
		@Override
		public String getId() {
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
		public Text getToastText() {
			return frame.getToastText();
		}
		
		@Override
		public Identifier getTextureSheet() {
			return PaginatedAdvancementsClient.locate("textures/gui/frames.png");
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
		} else if (frame.getNamespace().equals(PaginatedAdvancementsClient.MOD_ID)) {
			for (PaginatedAdvancementFrame customFrame : PaginatedAdvancementFrame.values()) {
				if (customFrame.getId().equals(path)) {
					return new ExtendedFrameWrapper(customFrame);
				}
			}
		}
		return null;
	}
	
}
