package de.dafuqs.paginatedadvancements.frames;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.paginatedadvancements.*;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class AdvancementFrameDataLoader extends SimpleJsonResourceReloadListener<List<AdvancementFrameDataLoader.Entry>> {
	
	public static final String LOCATION = "advancement_frames";
	public static final ResourceLocation ID = PaginatedAdvancementsClient.locate(LOCATION);
	public static final AdvancementFrameDataLoader INSTANCE = new AdvancementFrameDataLoader();
	
	public record Entry(ResourceLocation advancementId, ResourceLocation frameId) {
		
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
				ResourceLocation.CODEC.fieldOf("advancement").forGetter(Entry::advancementId),
				ResourceLocation.CODEC.fieldOf("frame").forGetter(Entry::frameId)
		).apply(instance, Entry::new));
		
		public static final Codec<List<Entry>> LIST_CODEC = CODEC.listOf();
		
	}
	
	protected static final Map<ResourceLocation, FrameWrapper> CUSTOM_FRAMES = new HashMap<>();
	
	public AdvancementFrameDataLoader() {
		super(Entry.LIST_CODEC, FileToIdConverter.json(LOCATION));
	}
	
	public static @Nullable FrameWrapper get(ResourceLocation id) {
		return CUSTOM_FRAMES.getOrDefault(id, null);
	}
	
	@Override
	protected Map<ResourceLocation, List<AdvancementFrameDataLoader.Entry>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return super.prepare(resourceManager, profiler);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, List<Entry>> prepared, ResourceManager manager, ProfilerFiller profiler) {
		for (Map.Entry<ResourceLocation, List<Entry>> list : prepared.entrySet()) {
			for (Entry entry : list.getValue()) {
				ResourceLocation advancement = entry.advancementId();
				ResourceLocation frame = entry.frameId();
				
				@Nullable FrameWrapper frameWrapper = FrameWrapper.of(frame);
				if (frameWrapper == null) {
					PaginatedAdvancementsClient.LOGGER.error("Advancement Frame '{}' for advancement  '{}' is unknown.", frame, advancement);
				} else {
					CUSTOM_FRAMES.put(advancement, frameWrapper);
				}
			}
		}
	}
	
}