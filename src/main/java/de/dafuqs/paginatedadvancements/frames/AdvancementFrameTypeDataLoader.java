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
	public static final Identifier ID = PaginatedAdvancementsClient.locate(LOCATION);
	public static final AdvancementFrameTypeDataLoader INSTANCE = new AdvancementFrameTypeDataLoader();
	
	protected static final Map<Identifier, PaginatedAdvancementFrame> ADVANCEMENT_TO_FRAME = new HashMap<>();
	
	public AdvancementFrameTypeDataLoader() {
		super(PaginatedAdvancementFrame.CODEC, FileToIdConverter.json(LOCATION));
	}
	
	public static @Nullable PaginatedAdvancementFrame getFrameForAdvancement(Identifier id) {
		return ADVANCEMENT_TO_FRAME.getOrDefault(id, null);
	}
	
	@Override
	protected Map<Identifier, PaginatedAdvancementFrame> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return super.prepare(resourceManager, profiler);
	}
	
	@Override
	protected void apply(Map<Identifier, PaginatedAdvancementFrame> prepared, ResourceManager manager, ProfilerFiller profiler) {
		for (Map.Entry<Identifier, PaginatedAdvancementFrame> entry : prepared.entrySet()) {
			Identifier id = entry.getKey();
			PaginatedAdvancementFrame frame = entry.getValue();
			frame.setIdBasedData(id);
			ADVANCEMENT_TO_FRAME.put(id, frame);
		}
	}
	
}