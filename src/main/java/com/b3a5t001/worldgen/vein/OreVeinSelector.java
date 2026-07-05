package com.b3a5t001.worldgen.vein;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum OreVeinSelector {
	POSITIVE,
	NEGATIVE,
	ANY;

	public static final Codec<OreVeinSelector> CODEC = Codec.STRING.comapFlatMap(OreVeinSelector::read, OreVeinSelector::serializedName);

	public boolean matches(final double veinToggle) {
		return switch (this) {
			case POSITIVE -> veinToggle > 0.0;
			case NEGATIVE -> veinToggle < 0.0;
			case ANY -> veinToggle != 0.0;
		};
	}

	private static DataResult<OreVeinSelector> read(final String name) {
		return switch (name.toLowerCase(Locale.ROOT)) {
			case "positive" -> DataResult.success(POSITIVE);
			case "negative" -> DataResult.success(NEGATIVE);
			case "any" -> DataResult.success(ANY);
			default -> DataResult.error(() -> "Unknown ore vein selector '" + name + "'. Expected positive, negative, or any.");
		};
	}

	private String serializedName() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
