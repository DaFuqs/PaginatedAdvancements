package de.dafuqs.paginatedadvancements.frames;

import com.google.gson.*;
import de.dafuqs.paginatedadvancements.*;
import de.dafuqs.paginatedadvancements.client.*;
import net.fabricmc.fabric.api.resource.*;
import net.minecraft.resource.*;
import net.minecraft.util.*;
import net.minecraft.util.profiler.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class AdvancementFrameTypeDataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
	
	public static final String ID = "advancement_frame_types";
	public static final AdvancementFrameTypeDataLoader INSTANCE = new AdvancementFrameTypeDataLoader();
	
	protected static final Map<Identifier, PaginatedAdvancementFrame> FRAMES = new HashMap<>();
	
	private AdvancementFrameTypeDataLoader() {
		super(new Gson(), ID);
	}
	
	
	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
		prepared.forEach((identifier, jsonElement) -> {
			JsonObject object = jsonElement.getAsJsonObject();
			Identifier textureSheet = new Identifier(identifier.getNamespace(), object.get("texture_sheet").getAsString());
			
			for (JsonElement frameEntry : object.get("frames").getAsJsonArray()) {
				JsonObject jsonObject = frameEntry.getAsJsonObject();
				Identifier name = new Identifier(identifier.getNamespace(), jsonObject.get("name").getAsString());
				int u = jsonObject.get("u").getAsInt();
				int v = jsonObject.get("v").getAsInt();
				Formatting formatting = Formatting.byName(jsonObject.get("formatting").getAsString());
				
				PaginatedAdvancementFrame frame = new PaginatedAdvancementFrame(name, textureSheet, u, v, formatting);
				FRAMES.put(name, frame);
			}
			
		});
	}
	
	@Override
	public Identifier getFabricId() {
		return PaginatedAdvancementsClient.locate(ID);
	}
	
	public static @Nullable PaginatedAdvancementFrame get(Identifier id) {
		return FRAMES.getOrDefault(id, null);
	}
	
}