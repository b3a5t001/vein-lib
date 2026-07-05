package com.b3a5t001.worldgen.vein;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomState;
import org.jspecify.annotations.Nullable;

public final class OreVeinGenerationContext {
	private static final ThreadLocal<BlockState> DEFAULT_BLOCK = new ThreadLocal<>();
	private static final ThreadLocal<ResourceKey<Level>> DIMENSION = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> VANILLA_ORE_VEINS_ENABLED = new ThreadLocal<>();
	private static final ThreadLocal<RandomState> RANDOM_STATE = new ThreadLocal<>();

	private OreVeinGenerationContext() {
	}

	public static void setDefaultBlock(final BlockState state) {
		DEFAULT_BLOCK.set(state);
	}

	public static @Nullable BlockState defaultBlock() {
		return DEFAULT_BLOCK.get();
	}

	public static void setDimension(final @Nullable ResourceKey<Level> dimension) {
		if (dimension == null) {
			DIMENSION.remove();
		} else {
			DIMENSION.set(dimension);
		}
	}

	public static @Nullable ResourceKey<Level> dimension() {
		return DIMENSION.get();
	}

	public static void setVanillaOreVeinsEnabled(final boolean vanillaOreVeinsEnabled) {
		VANILLA_ORE_VEINS_ENABLED.set(vanillaOreVeinsEnabled);
	}

	public static boolean vanillaOreVeinsEnabled() {
		return Boolean.TRUE.equals(VANILLA_ORE_VEINS_ENABLED.get());
	}

	public static void setRandomState(final RandomState randomState) {
		RANDOM_STATE.set(randomState);
	}

	public static @Nullable RandomState randomState() {
		return RANDOM_STATE.get();
	}

	public static void clearDefaultBlock() {
		DEFAULT_BLOCK.remove();
		VANILLA_ORE_VEINS_ENABLED.remove();
		RANDOM_STATE.remove();
	}

	public static void clearDimension() {
		DIMENSION.remove();
	}
}
