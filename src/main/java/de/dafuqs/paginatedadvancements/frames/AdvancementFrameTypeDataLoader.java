package de.dafuqs.paginatedadvancements.frames;

import de.dafuqs.paginatedadvancements.PaginatedAdvancementsClient;
import de.dafuqs.paginatedadvancements.client.PaginatedAdvancementFrame;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class AdvancementFrameTypeDataLoader extends SimpleJsonResourceReloadListener<PaginatedAdvancementFrame> implements PreparableReloadListener {
	
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
	protected @NonNull Map<Identifier, PaginatedAdvancementFrame> prepare(@NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profiler) {
		return super.prepare(resourceManager, profiler);
	}

	@Override
	protected void apply(Map<Identifier, PaginatedAdvancementFrame> prepared, @NonNull ResourceManager manager, @NonNull ProfilerFiller profiler) {
		for (Map.Entry<Identifier, PaginatedAdvancementFrame> entry : prepared.entrySet()) {
			Identifier id = entry.getKey();
			PaginatedAdvancementFrame frame = entry.getValue();
			frame.setIdBasedData(id);
			ADVANCEMENT_TO_FRAME.put(id, frame);
		}
	}
	
}