package com.b3a5t001.worldgen.vein;

import com.b3a5t001.VeinLib;
import java.util.Map;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class OreVeinReloadListener extends SimpleJsonResourceReloadListener<OreVeinDefinition> implements IdentifiableResourceReloadListener {
	private static final FileToIdConverter FILES = FileToIdConverter.json("vein_lib/ore_vein");

	public OreVeinReloadListener() {
		super(OreVeinDefinition.CODEC, FILES);
	}

	@Override
	public Identifier getFabricId() {
		return VeinLib.id("ore_veins");
	}

	@Override
	protected void apply(final Map<Identifier, OreVeinDefinition> preparations, final ResourceManager manager, final ProfilerFiller profiler) {
		OreVeinManager.replaceDefinitions(preparations);
		VeinLib.LOGGER.info("Loaded {} ore vein definition(s)", preparations.size());
	}
}
