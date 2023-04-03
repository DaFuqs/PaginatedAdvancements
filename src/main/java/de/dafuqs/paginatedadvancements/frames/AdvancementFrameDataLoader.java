package de.dafuqs.paginatedadvancements.frames;

import com.google.gson.*;
import de.dafuqs.paginatedadvancements.*;
import net.fabricmc.fabric.api.resource.*;
import net.minecraft.resource.*;
import net.minecraft.util.*;
import net.minecraft.util.profiler.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class AdvancementFrameDataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
	
	public static final String ID = "advancement_frames";
	public static final AdvancementFrameDataLoader INSTANCE = new AdvancementFrameDataLoader();
	
	protected static final Map<Identifier, FrameWrapper> ADVANCEMENT_FRAMES = new HashMap<>();
	
	private AdvancementFrameDataLoader() {
		super(new Gson(), ID);
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
		prepared.forEach((identifier, jsonElement) -> {
			for (JsonElement entry : jsonElement.getAsJsonArray()) {
				JsonObject jsonObject = entry.getAsJsonObject();
				Identifier advancement = Identifier.tryParse(jsonObject.get("advancement").getAsString());
				Identifier frame = Identifier.tryParse(jsonObject.get("frame").getAsString());
				
				@Nullable FrameWrapper frameWrapper = FrameWrapper.of(frame);
				if (frameWrapper == null) {
					PaginatedAdvancementsClient.LOGGER.error("Advancement Frame '" + frame + "' for advancement  '" + advancement + "' is unknown");
				} else {
					ADVANCEMENT_FRAMES.put(advancement, frameWrapper);
				}
			}
		});
	}
	
	@Override
	public Identifier getFabricId() {
		return PaginatedAdvancementsClient.locate(ID);
	}
	
	public static @Nullable FrameWrapper get(Identifier id) {
		return ADVANCEMENT_FRAMES.getOrDefault(id, null);
	}
	
}