package de.dafuqs.paginatedadvancements.frames;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.paginatedadvancements.*;
import net.fabricmc.fabric.api.resource.*;
import net.minecraft.resource.*;
import net.minecraft.util.*;
import net.minecraft.util.profiler.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class AdvancementFrameDataLoader extends JsonDataLoader<List<AdvancementFrameDataLoader.Entry>> implements IdentifiableResourceReloadListener {
	
	public static final Identifier ID = PaginatedAdvancementsClient.locate("advancement_frames");
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
		super(Entry.LIST_CODEC, "advancement_frames");
	}
	
	public static @Nullable FrameWrapper get(Identifier id) {
		return CUSTOM_FRAMES.getOrDefault(id, null);
	}
	
	@Override
	protected Map<Identifier, List<AdvancementFrameDataLoader.Entry>> prepare(ResourceManager resourceManager, Profiler profiler) {
		return super.prepare(resourceManager, profiler);
	}
	
	@Override
	protected void apply(Map<Identifier, List<Entry>> prepared, ResourceManager manager, Profiler profiler) {
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
	
	@Override
	public Identifier getFabricId() {
		return ID;
	}
	
}