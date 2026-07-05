package com.b3a5t001.mixin;

import com.b3a5t001.worldgen.vein.OreVeinGenerationContext;
import com.b3a5t001.worldgen.vein.OreVeinManager;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin {
	@Inject(method = "<init>", at = @At("HEAD"))
	private static void veinlib$captureDefaultBlock(
		final int cellCountXZ,
		final RandomState randomState,
		final int chunkMinBlockX,
		final int chunkMinBlockZ,
		final NoiseSettings noiseSettings,
		final DensityFunctions.BeardifierOrMarker beardifier,
		final NoiseGeneratorSettings settings,
		final Aquifer.FluidPicker globalFluidPicker,
		final Blender blender,
		final CallbackInfo ci
	) {
		OreVeinGenerationContext.setDefaultBlock(settings.defaultBlock());
		OreVeinGenerationContext.setVanillaOreVeinsEnabled(settings.oreVeinsEnabled());
		OreVeinGenerationContext.setRandomState(randomState);
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;oreVeinsEnabled()Z"))
	private boolean veinlib$enableCustomOreVeins(final NoiseGeneratorSettings settings) {
		return settings.oreVeinsEnabled() || OreVeinManager.hasDefinitionsFor(OreVeinGenerationContext.dimension());
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void veinlib$clearDefaultBlock(
		final int cellCountXZ,
		final RandomState randomState,
		final int chunkMinBlockX,
		final int chunkMinBlockZ,
		final NoiseSettings noiseSettings,
		final DensityFunctions.BeardifierOrMarker beardifier,
		final NoiseGeneratorSettings settings,
		final Aquifer.FluidPicker globalFluidPicker,
		final Blender blender,
		final CallbackInfo ci
	) {
		OreVeinGenerationContext.clearDefaultBlock();
	}
}
