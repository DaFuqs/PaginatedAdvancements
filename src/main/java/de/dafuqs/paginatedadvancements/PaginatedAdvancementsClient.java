package de.dafuqs.paginatedadvancements;

import de.dafuqs.paginatedadvancements.config.*;
import net.minecraft.resources.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.config.*;
import net.neoforged.neoforge.client.gui.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

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
		
		modBus.addListener(PaginatedAdvancementsConfig::registerResources);
	}
	
}
