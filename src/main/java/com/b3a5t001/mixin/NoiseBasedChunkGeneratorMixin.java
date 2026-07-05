package com.b3a5t001.mixin;

import com.b3a5t001.worldgen.vein.OreVeinGenerationContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
	@Inject(method = "createNoiseChunk", at = @At("HEAD"))
	private void veinlib$captureDimension(
		final ChunkAccess chunk,
		final StructureManager structureManager,
		final Blender blender,
		final RandomState randomState,
		final CallbackInfoReturnable<NoiseChunk> cir
	) {
		OreVeinGenerationContext.setDimension(veinlib$dimension(structureManager));
	}

	@Inject(method = "createNoiseChunk", at = @At("RETURN"))
	private void veinlib$clearDimension(
		final ChunkAccess chunk,
		final StructureManager structureManager,
		final Blender blender,
		final RandomState randomState,
		final CallbackInfoReturnable<NoiseChunk> cir
	) {
		OreVeinGenerationContext.clearDimension();
	}

	private static @Nullable ResourceKey<Level> veinlib$dimension(final StructureManager structureManager) {
		LevelAccessor level = ((StructureManagerAccessor)structureManager).veinlib$getLevel();
		if (level instanceof Level actualLevel) {
			return actualLevel.dimension();
		}

		if (level instanceof WorldGenRegion region) {
			return region.getLevel().dimension();
		}

		return null;
	}
}
