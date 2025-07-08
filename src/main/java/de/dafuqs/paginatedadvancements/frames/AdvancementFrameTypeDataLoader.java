package de.dafuqs.paginatedadvancements.frames;

import de.dafuqs.paginatedadvancements.*;
import de.dafuqs.paginatedadvancements.client.*;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class AdvancementFrameTypeDataLoader extends SimpleJsonResourceReloadListener<PaginatedAdvancementFrame> {
	
	public static final String LOCATION = "advancement_frame_types";
	public static final ResourceLocation ID = PaginatedAdvancementsClient.locate(LOCATION);
	public static final AdvancementFrameTypeDataLoader INSTANCE = new AdvancementFrameTypeDataLoader();
	
	protected static final Map<ResourceLocation, PaginatedAdvancementFrame> ADVANCEMENT_TO_FRAME = new HashMap<>();
	
	public AdvancementFrameTypeDataLoader() {
		super(PaginatedAdvancementFrame.CODEC, FileToIdConverter.json(LOCATION));
	}
	
	public static @Nullable PaginatedAdvancementFrame getFrameForAdvancement(ResourceLocation id) {
		return ADVANCEMENT_TO_FRAME.getOrDefault(id, null);
	}
	
	@Override
	protected Map<ResourceLocation, PaginatedAdvancementFrame> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return super.prepare(resourceManager, profiler);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, PaginatedAdvancementFrame> prepared, ResourceManager manager, ProfilerFiller profiler) {
		for (Map.Entry<ResourceLocation, PaginatedAdvancementFrame> entry : prepared.entrySet()) {
			ResourceLocation id = entry.getKey();
			PaginatedAdvancementFrame frame = entry.getValue();
			frame.setIdBasedData(id);
			ADVANCEMENT_TO_FRAME.put(id, frame);
		}
	}
	
}