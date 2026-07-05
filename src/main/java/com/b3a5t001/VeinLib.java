package com.b3a5t001;

import com.b3a5t001.worldgen.vein.OreVeinReloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VeinLib implements ModInitializer {
	public static final String MOD_ID = "vein-lib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Identifier TEST_VEINS_PACK_ID = id("test_veins");

	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new OreVeinReloadListener());
		registerTestVeinsPack();
		LOGGER.info("Vein Lib initialized");
	}

	private static void registerTestVeinsPack() {
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresentOrElse(VeinLib::registerTestVeinsPack, () -> {
			LOGGER.warn("Could not find Vein Lib mod container; test veins built-in datapack was not registered");
		});
	}

	private static void registerTestVeinsPack(final ModContainer container) {
		boolean registered = ResourceLoader.registerBuiltinPack(
			TEST_VEINS_PACK_ID,
			container,
			Component.literal("Vein Lib Test Veins"),
			PackActivationType.NORMAL
		);
		if (!registered) {
			LOGGER.warn("Failed to register built-in datapack {}", TEST_VEINS_PACK_ID);
		}
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
