package com.b3a5t001.worldgen.vein;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class OreVeinManager {
	private static volatile List<OreVeinDefinition> definitions = List.of();

	private OreVeinManager() {
	}

	public static List<OreVeinDefinition> definitions() {
		return definitions;
	}

	public static List<OreVeinDefinition> definitionsFor(final @Nullable ResourceKey<Level> dimension) {
		if (dimension == null) {
			return List.of();
		}

		return definitions.stream().filter(definition -> definition.matchesDimension(dimension)).toList();
	}

	public static boolean hasDefinitionsFor(final @Nullable ResourceKey<Level> dimension) {
		if (dimension == null) {
			return false;
		}

		return definitions.stream().anyMatch(definition -> definition.matchesDimension(dimension));
	}

	public static void replaceDefinitions(final Map<Identifier, OreVeinDefinition> loadedDefinitions) {
		definitions = loadedDefinitions.entrySet()
			.stream()
			.sorted(Comparator.comparing(entry -> entry.getKey().toString()))
			.map(Map.Entry::getValue)
			.toList();
	}
}
