package de.dafuqs.paginatedadvancements.frames;

import de.dafuqs.paginatedadvancements.*;
import de.dafuqs.paginatedadvancements.client.*;
import net.fabricmc.fabric.api.resource.*;
import net.minecraft.resource.*;
import net.minecraft.util.*;
import net.minecraft.util.profiler.*;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.*;

import java.util.*;

public class AdvancementFrameTypeDataLoader extends JsonDataLoader<PaginatedAdvancementFrame> implements IdentifiableResourceReloadListener {
	
	public static final String LOCATION = "advancement_frame_types";
	public static final Identifier ID = PaginatedAdvancementsClient.locate(LOCATION);
	public static final AdvancementFrameTypeDataLoader INSTANCE = new AdvancementFrameTypeDataLoader();
	
	protected static final Map<Identifier, PaginatedAdvancementFrame> ADVANCEMENT_TO_FRAME = new HashMap<>();
	
	public AdvancementFrameTypeDataLoader() {
		super(PaginatedAdvancementFrame.CODEC, ResourceFinder.json(LOCATION));
	}
	
	public static @Nullable PaginatedAdvancementFrame getFrameForAdvancement(Identifier id) {
		return ADVANCEMENT_TO_FRAME.getOrDefault(id, null);
	}
	
	@Override
	protected Map<Identifier, PaginatedAdvancementFrame> prepare(ResourceManager resourceManager, Profiler profiler) {
		return super.prepare(resourceManager, profiler);
	}
	
	@Override
	protected void apply(Map<Identifier, PaginatedAdvancementFrame> prepared, ResourceManager manager, Profiler profiler) {
		for (Map.Entry<Identifier, PaginatedAdvancementFrame> entry : prepared.entrySet()) {
			Identifier id = entry.getKey();
			PaginatedAdvancementFrame frame = entry.getValue();
			frame.setIdBasedData(id);
			ADVANCEMENT_TO_FRAME.put(id, frame);
		}
	}
	
	@Override
	public @NonNull Identifier getFabricId() {
		return ID;
	}
	
}