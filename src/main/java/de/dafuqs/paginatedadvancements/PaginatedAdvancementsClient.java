package de.dafuqs.paginatedadvancements;

import de.dafuqs.paginatedadvancements.config.*;
import de.dafuqs.paginatedadvancements.frames.*;
import me.shedaniel.autoconfig.*;
import me.shedaniel.autoconfig.serializer.*;
import net.minecraft.resources.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.*;
import org.jetbrains.annotations.*;
import org.slf4j.*;

import java.util.*;

public class PaginatedAdvancementsClient {
	
	public static final Logger LOGGER = LoggerFactory.getLogger("PaginatedAdvancements");
	public static final String MOD_ID = "paginatedadvancements";
	
	public static ConfigManager<PaginatedAdvancementsConfig> CONFIG_MANAGER;
	public static PaginatedAdvancementsConfig CONFIG;
	
	@Contract(value = "_ -> new", pure = true)
	public static @NotNull ResourceLocation locate(String name) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
	}
	
	public static void onInitializeClient() {
		ConfigHolder<PaginatedAdvancementsConfig> configHolder = AutoConfig.register(PaginatedAdvancementsConfig.class, JanksonConfigSerializer::new);
		CONFIG_MANAGER = ((ConfigManager<PaginatedAdvancementsConfig>) configHolder);
		CONFIG = AutoConfig.getConfigHolder(PaginatedAdvancementsConfig.class).getConfig();
		
		ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (modContainer, screen) ->
				AutoConfig.getConfigScreen(PaginatedAdvancementsConfig.class, screen).get());
	}
	
	@SubscribeEvent
	public static void registerResources(AddClientReloadListenersEvent event) {
		event.addListener(AdvancementFrameTypeDataLoader.ID, AdvancementFrameTypeDataLoader.INSTANCE);
		event.addListener(AdvancementFrameDataLoader.ID, AdvancementFrameDataLoader.INSTANCE);
	}
	
	public static void saveSelectedTab(ResourceLocation tabIdentifier) {
		if(CONFIG.SaveLastSelectedTab) {
			CONFIG.LastSelectedTab = tabIdentifier.toString();
			CONFIG_MANAGER.save();
		}
	}
	
	public static void pinTab(ResourceLocation tabIdentifier) {
		String identifierString = tabIdentifier.toString();
		if(!CONFIG.PinnedTabs.contains(identifierString)) {
			CONFIG.PinnedTabs.add(identifierString);
			CONFIG_MANAGER.save();
		}
	}
	
	public static void unpinTab(ResourceLocation tabIdentifier) {
		String identifierString = tabIdentifier.toString();
		if(CONFIG.PinnedTabs.contains(identifierString)) {
			CONFIG.PinnedTabs.remove(identifierString);
			CONFIG_MANAGER.save();
		}
	}
	
	public static boolean isPinned(ResourceLocation tabIdentifier) {
		return CONFIG.PinningEnabled && CONFIG.PinnedTabs.contains(tabIdentifier.toString());
	}
	
	public static boolean hasPins() {
		return CONFIG.PinningEnabled && !CONFIG.PinnedTabs.isEmpty();
	}
	
	public static List<String> getPinnedTabs() {
		return CONFIG.PinnedTabs;
	}
	
	public static int getPinIndex(ResourceLocation tabIdentifier) {
		return CONFIG.PinnedTabs.indexOf(tabIdentifier.toString());
	}
	
}
