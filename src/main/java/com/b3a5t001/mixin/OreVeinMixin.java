package com.b3a5t001.mixin;

import com.b3a5t001.worldgen.vein.OreVeinDefinition;
import com.b3a5t001.worldgen.vein.OreVeinGenerationContext;
import com.b3a5t001.worldgen.vein.OreVeinManager;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.OreVeinifier;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreVeinifier.class)
public class OreVeinMixin {
	@Unique
	private static final float VEINLIB_VEININESS_THRESHOLD = 0.4F;
	@Unique
	private static final int VEINLIB_EDGE_ROUNDOFF_BEGIN = 20;
	@Unique
	private static final double VEINLIB_MAX_EDGE_ROUNDOFF = 0.2;
	@Unique
	private static final float VEINLIB_VEIN_SOLIDNESS = 0.7F;
	@Unique
	private static final float VEINLIB_SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3F;

	@Inject(method = "create", at = @At("RETURN"), cancellable = true)
	private static void veinlib$appendDataDrivenVeins(
		final DensityFunction veinToggle,
		final DensityFunction veinRidged,
		final DensityFunction veinGap,
		final PositionalRandomFactory oreVeinsPositionalRandomFactory,
		final CallbackInfoReturnable<NoiseChunk.BlockStateFiller> cir
	) {
		List<OreVeinDefinition> definitions = OreVeinManager.definitionsFor(OreVeinGenerationContext.dimension());
		BlockState replacedState = OreVeinGenerationContext.defaultBlock();
		if (definitions.isEmpty() || replacedState == null) {
			return;
		}

		boolean vanillaOreVeinsEnabled = OreVeinGenerationContext.vanillaOreVeinsEnabled();
		NoiseChunk.BlockStateFiller vanillaFiller = cir.getReturnValue();
		VeinNoiseSampler noiseSampler = veinlib$createNoiseSampler(vanillaOreVeinsEnabled, veinToggle, veinRidged, veinGap);
		NoiseChunk.BlockStateFiller dataDrivenFiller = veinlib$createDataDrivenFiller(definitions, replacedState, noiseSampler, oreVeinsPositionalRandomFactory);
		cir.setReturnValue(context -> {
			if (vanillaOreVeinsEnabled) {
				BlockState vanillaState = vanillaFiller.calculate(context);
				if (vanillaState != null) {
					return vanillaState;
				}
			}

			return dataDrivenFiller.calculate(context);
		});
	}

	@Unique
	private static VeinNoiseSampler veinlib$createNoiseSampler(
		final boolean vanillaOreVeinsEnabled,
		final DensityFunction veinToggle,
		final DensityFunction veinRidged,
		final DensityFunction veinGap
	) {
		RandomState randomState = OreVeinGenerationContext.randomState();
		if (!vanillaOreVeinsEnabled && randomState != null) {
			return new DirectVeinNoiseSampler(
				randomState.getOrCreateNoise(Noises.ORE_VEININESS),
				randomState.getOrCreateNoise(Noises.ORE_VEIN_A),
				randomState.getOrCreateNoise(Noises.ORE_VEIN_B),
				randomState.getOrCreateNoise(Noises.ORE_GAP)
			);
		}

		return new RouterVeinNoiseSampler(veinToggle, veinRidged, veinGap);
	}

	@Unique
	private static NoiseChunk.BlockStateFiller veinlib$createDataDrivenFiller(
		final List<OreVeinDefinition> definitions,
		final BlockState replacedState,
		final VeinNoiseSampler noiseSampler,
		final PositionalRandomFactory oreVeinsPositionalRandomFactory
	) {
		BlockState defaultState = SharedConstants.DEBUG_ORE_VEINS ? Blocks.AIR.defaultBlockState() : null;
		return context -> {
			double oreVeininessNoiseValue = noiseSampler.veininess(context);
			int posY = context.blockY();

			for (OreVeinDefinition definition : definitions) {
				if (!definition.matches(oreVeininessNoiseValue, posY, replacedState)) {
					continue;
				}

				double veininessRidged = Math.abs(oreVeininessNoiseValue);
				int distanceFromTop = definition.maxY() - posY;
				int distanceFromBottom = posY - definition.minY();
				int distanceFromEdge = Math.min(distanceFromTop, distanceFromBottom);
				double edgeRoundoff = Mth.clampedMap(distanceFromEdge, 0.0, VEINLIB_EDGE_ROUNDOFF_BEGIN, -VEINLIB_MAX_EDGE_ROUNDOFF, 0.0);
				if (veininessRidged + edgeRoundoff < VEINLIB_VEININESS_THRESHOLD) {
					return defaultState;
				}

				RandomSource positionalRandom = oreVeinsPositionalRandomFactory.at(context.blockX(), posY, context.blockZ());
				if (positionalRandom.nextFloat() > VEINLIB_VEIN_SOLIDNESS || noiseSampler.ridged(context) >= 0.0) {
					return defaultState;
				}

				BlockPos pos = new BlockPos(context.blockX(), posY, context.blockZ());
				boolean canPlaceOre = veininessRidged >= VEINLIB_VEININESS_THRESHOLD
					&& noiseSampler.gap(context) > VEINLIB_SKIP_ORE_IF_GAP_NOISE_IS_BELOW;
				return definition.chooseState(positionalRandom, pos, canPlaceOre);
			}

			return defaultState;
		};
	}

	@Unique
	private interface VeinNoiseSampler {
		double veininess(DensityFunction.FunctionContext context);

		double ridged(DensityFunction.FunctionContext context);

		double gap(DensityFunction.FunctionContext context);
	}

	@Unique
	private record RouterVeinNoiseSampler(DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) implements VeinNoiseSampler {
		@Override
		public double veininess(final DensityFunction.FunctionContext context) {
			return this.veinToggle.compute(context);
		}

		@Override
		public double ridged(final DensityFunction.FunctionContext context) {
			return this.veinRidged.compute(context);
		}

		@Override
		public double gap(final DensityFunction.FunctionContext context) {
			return this.veinGap.compute(context);
		}
	}

	@Unique
	private record DirectVeinNoiseSampler(NormalNoise veininess, NormalNoise veinA, NormalNoise veinB, NormalNoise gap) implements VeinNoiseSampler {
		@Override
		public double veininess(final DensityFunction.FunctionContext context) {
			return this.veininess.getValue(context.blockX() * 1.5, context.blockY() * 1.5, context.blockZ() * 1.5);
		}

		@Override
		public double ridged(final DensityFunction.FunctionContext context) {
			double a = Math.abs(this.veinA.getValue(context.blockX() * 4.0, context.blockY() * 4.0, context.blockZ() * 4.0));
			double b = Math.abs(this.veinB.getValue(context.blockX() * 4.0, context.blockY() * 4.0, context.blockZ() * 4.0));
			return -0.08F + Math.max(a, b);
		}

		@Override
		public double gap(final DensityFunction.FunctionContext context) {
			return this.gap.getValue(context.blockX(), context.blockY(), context.blockZ());
		}
	}
}
