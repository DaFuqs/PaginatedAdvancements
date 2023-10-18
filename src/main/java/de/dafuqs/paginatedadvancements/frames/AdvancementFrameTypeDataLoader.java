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
			
			for (JsonElement frameEntry : object.get("frames").getAsJsonArray()) {
				JsonObject jsonObject = frameEntry.getAsJsonObject();
				
				
				String name = jsonObject.get("name").getAsString();
				Identifier id = new Identifier(identifier.getNamespace(), name);
				int itemOffsetX = JsonHelper.getInt(jsonObject, "item_offset_x", 0);
				int itemOffsetY = JsonHelper.getInt(jsonObject, "item_offset_y", 0);
				String formattingString = JsonHelper.getString(jsonObject, "formatting", "green");
				Formatting formatting = Formatting.byName(formattingString);
				
				if (formatting == null) {
					// green is the vanilla default for most AdvancementFrames
					PaginatedAdvancementsClient.LOGGER.error("Formatting for frame '" + id + "' is invalid: '" + formattingString + "'. Will use default 'green'");
					formatting = Formatting.GREEN;
				}
				
				Identifier textureObtained = new Identifier(identifier.getNamespace(), "advancements/" + name + "_obtained");
				Identifier textureUnobtained = new Identifier(identifier.getNamespace(), "advancements/" + name + "_unobtained");
				
				PaginatedAdvancementFrame frame = new PaginatedAdvancementFrame(id, textureObtained, textureUnobtained, itemOffsetX, itemOffsetY, formatting);
				FRAMES.put(id, frame);
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