package de.dafuqs.paginatedadvancements;

import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.event.lifecycle.*;

@Mod(value = PaginatedAdvancementsClient.MOD_ID)
public class PaginatedAdvancements {
	
	public PaginatedAdvancements(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(PaginatedAdvancementsClient::registerResources);
	}
	
	private void clientSetup(final FMLClientSetupEvent event) {
		PaginatedAdvancementsClient.onInitializeClient();
	}
	
}