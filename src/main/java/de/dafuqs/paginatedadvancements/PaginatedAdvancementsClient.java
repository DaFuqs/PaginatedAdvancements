package de.dafuqs.paginatedadvancements;

import de.dafuqs.paginatedadvancements.config.PaginatedAdvancementsConfig;
import de.dafuqs.paginatedadvancements.frames.AdvancementFrameDataLoader;
import de.dafuqs.paginatedadvancements.frames.AdvancementFrameTypeDataLoader;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = PaginatedAdvancementsClient.MOD_ID, dist = Dist.CLIENT)
public class PaginatedAdvancementsClient {
	
	public static final Logger LOGGER = LoggerFactory.getLogger("PaginatedAdvancements");
	public static final String MOD_ID = "paginatedadvancements";

	@Contract(value = "_ -> new", pure = true)
	public static @NotNull Identifier locate(String name) {
		return Identifier.fromNamespaceAndPath(MOD_ID, name);
	}
	
	public PaginatedAdvancementsClient(IEventBus modBus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, PaginatedAdvancementsConfig.CONFIG_SPEC);
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

		modBus.addListener(PaginatedAdvancementsClient::registerResources);
	}
	
	@SubscribeEvent
	public static void registerResources(AddClientReloadListenersEvent event) {
		event.addListener(AdvancementFrameTypeDataLoader.ID, AdvancementFrameTypeDataLoader.INSTANCE);
		event.addListener(AdvancementFrameDataLoader.ID, AdvancementFrameDataLoader.INSTANCE);
	}
	
}
