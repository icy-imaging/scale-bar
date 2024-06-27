/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package plugins.tprovoost.scale;

import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.common.math.UnitUtil.UnitPrefix;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum ScaleBarUnit {
    KILO, NONE, MILLI, MICRO, NANO;

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return switch (this) {
            case KILO -> "km";
            case NONE -> "m";
            case MILLI -> "mm";
            default -> UnitUtil.MICRO_STRING + "m";
            case NANO -> "nm";
        };
    }

    @Contract(pure = true)
    public UnitUtil.UnitPrefix asUnitPrefix() {
        return switch (this) {
            case KILO -> UnitUtil.UnitPrefix.KILO;
            case NONE -> UnitUtil.UnitPrefix.NONE;
            case MILLI -> UnitUtil.UnitPrefix.MILLI;
            default -> UnitUtil.UnitPrefix.MICRO;
            case NANO -> UnitUtil.UnitPrefix.NANO;
        };
    }

    @Contract(pure = true)
    public static ScaleBarUnit getUnitFrom(final @NotNull UnitPrefix up) {
        return switch (up) {
            case GIGA, MEGA, KILO -> KILO;
            case NONE -> NONE;
            case MILLI -> MILLI;
            default -> MICRO;
            case NANO, PICO -> NANO;
        };
    }
}
