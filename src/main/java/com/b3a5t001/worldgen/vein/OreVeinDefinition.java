package com.b3a5t001.worldgen.vein;

import com.b3a5t001.VeinLib;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public record OreVeinDefinition(
	int minY,
	int maxY,
	ResourceKey<Level> dimension,
	OreVeinSelector selector,
	BlockStateProvider filler,
	BlockStateProvider ore,
	BlockStateProvider raw,
	OreVeinBlockChances chances,
	TagKey<Block> cannotReplace
) {
	public static final TagKey<Block> DEFAULT_CANNOT_REPLACE = TagKey.create(Registries.BLOCK, VeinLib.id("ore_vein_cannot_replace"));
	private static final Codec<BlockStateProvider> SUPPORTED_PROVIDER_CODEC = BlockStateProvider.CODEC.comapFlatMap(provider -> {
		if (provider instanceof SimpleStateProvider || provider instanceof WeightedStateProvider) {
			return DataResult.success(provider);
		}

		return DataResult.error(() -> "Vein Lib currently supports only simple_state_provider and weighted_state_provider");
	}, Function.identity());
	public static final Codec<OreVeinDefinition> CODEC = RecordCodecBuilder.<OreVeinDefinition>create(
		instance -> instance.group(
			Codec.INT.fieldOf("min_y").forGetter(OreVeinDefinition::minY),
			Codec.INT.fieldOf("max_y").forGetter(OreVeinDefinition::maxY),
			Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(OreVeinDefinition::dimension),
			OreVeinSelector.CODEC.optionalFieldOf("selector", OreVeinSelector.NEGATIVE).forGetter(OreVeinDefinition::selector),
			SUPPORTED_PROVIDER_CODEC.fieldOf("filler").forGetter(OreVeinDefinition::filler),
			SUPPORTED_PROVIDER_CODEC.fieldOf("ore").forGetter(OreVeinDefinition::ore),
			SUPPORTED_PROVIDER_CODEC.fieldOf("raw").forGetter(OreVeinDefinition::raw),
			OreVeinBlockChances.CODEC.optionalFieldOf("chances", OreVeinBlockChances.DEFAULT).forGetter(OreVeinDefinition::chances),
			TagKey.hashedCodec(Registries.BLOCK).optionalFieldOf("cannot_replace", DEFAULT_CANNOT_REPLACE).forGetter(OreVeinDefinition::cannotReplace)
		).apply(instance, OreVeinDefinition::new)
	).comapFlatMap(OreVeinDefinition::validate, Function.identity());

	public boolean matchesDimension(final ResourceKey<Level> dimension) {
		return this.dimension.equals(dimension);
	}

	public boolean matches(final double veinToggle, final int posY, final BlockState replacedState) {
		return this.selector.matches(veinToggle) && posY >= this.minY && posY <= this.maxY && !replacedState.is(this.cannotReplace);
	}

	public BlockState chooseState(final RandomSource random, final BlockPos pos, final boolean canPlaceOre) {
		double rawChance = canPlaceOre ? this.chances.raw() : 0.0;
		double oreChance = canPlaceOre ? this.chances.ore() : 0.0;
		double fillerChance = this.chances.filler();
		double totalChance = rawChance + oreChance + fillerChance;
		if (totalChance <= 0.0) {
			return this.filler.getState(null, random, pos);
		}

		double roll = random.nextDouble() * totalChance;
		if (roll < rawChance) {
			return this.raw.getState(null, random, pos);
		}

		roll -= rawChance;
		if (roll < oreChance) {
			return this.ore.getState(null, random, pos);
		}

		return this.filler.getState(null, random, pos);
	}

	private static DataResult<OreVeinDefinition> validate(final OreVeinDefinition definition) {
		if (definition.minY > definition.maxY) {
			return DataResult.error(() -> "min_y must be less than or equal to max_y");
		}

		return DataResult.success(definition);
	}
}
