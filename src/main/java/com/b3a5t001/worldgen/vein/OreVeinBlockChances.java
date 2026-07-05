package com.b3a5t001.worldgen.vein;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record OreVeinBlockChances(double filler, double ore, double raw) {
	public static final OreVeinBlockChances DEFAULT = new OreVeinBlockChances(0.7, 0.28, 0.02);
	public static final Codec<OreVeinBlockChances> CODEC = RecordCodecBuilder.<OreVeinBlockChances>create(
		instance -> instance.group(
			Codec.DOUBLE.optionalFieldOf("filler", DEFAULT.filler()).forGetter(OreVeinBlockChances::filler),
			Codec.DOUBLE.optionalFieldOf("ore", DEFAULT.ore()).forGetter(OreVeinBlockChances::ore),
			Codec.DOUBLE.optionalFieldOf("raw", DEFAULT.raw()).forGetter(OreVeinBlockChances::raw)
		).apply(instance, OreVeinBlockChances::new)
	).comapFlatMap(OreVeinBlockChances::validate, Function.identity());

	private static DataResult<OreVeinBlockChances> validate(final OreVeinBlockChances chances) {
		if (chances.filler < 0.0 || chances.ore < 0.0 || chances.raw < 0.0) {
			return DataResult.error(() -> "Ore vein block chances cannot be negative");
		}

		if (chances.filler + chances.ore + chances.raw <= 0.0) {
			return DataResult.error(() -> "At least one ore vein block chance must be greater than zero");
		}

		return DataResult.success(chances);
	}
}
