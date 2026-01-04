package de.dafuqs.paginatedadvancements.frames;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.paginatedadvancements.*;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.*;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.*;

import java.util.*;

public class AdvancementFrameDataLoader extends SimpleJsonResourceReloadListener<List<AdvancementFrameDataLoader.Entry>> {
	
	public static final String LOCATION = "advancement_frames";
	public static final Identifier ID = PaginatedAdvancementsClient.locate(LOCATION);
	public static final AdvancementFrameDataLoader INSTANCE = new AdvancementFrameDataLoader();
	
	public record Entry(Identifier advancementId, Identifier frameId) {
		
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
				Identifier.CODEC.fieldOf("advancement").forGetter(Entry::advancementId),
				Identifier.CODEC.fieldOf("frame").forGetter(Entry::frameId)
		).apply(instance, Entry::new));
		
		public static final Codec<List<Entry>> LIST_CODEC = CODEC.listOf();
	}
	
	protected static final Map<Identifier, FrameWrapper> CUSTOM_FRAMES = new HashMap<>();
	
	public AdvancementFrameDataLoader() {
		super(Entry.LIST_CODEC, FileToIdConverter.json(LOCATION));
	}
	
	public static @Nullable FrameWrapper get(Identifier id) {
		return CUSTOM_FRAMES.getOrDefault(id, null);
	}
	
	@Override
	protected @NonNull Map<Identifier, List<AdvancementFrameDataLoader.Entry>> prepare(@NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profiler) {
		return super.prepare(resourceManager, profiler);
	}
	
	@Override
	protected void apply(Map<Identifier, List<Entry>> prepared, @NonNull ResourceManager manager, @NonNull ProfilerFiller profiler) {
		for (Map.Entry<Identifier, List<Entry>> list : prepared.entrySet()) {
			for (Entry entry : list.getValue()) {
				Identifier advancement = entry.advancementId();
				Identifier frame = entry.frameId();
				
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