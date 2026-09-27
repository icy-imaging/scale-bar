/*
 * Copyright (c) 2010-2026. Institut Pasteur.
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

import org.jetbrains.annotations.Contract;

public enum ScaleBarLocation {
    IMAGE_BOTTOM_LEFT("Bottom left of image"), VIEWER_BOTTOM_LEFT("Bottom left of viewer"),
    IMAGE_BOTTOM_RIGHT("Bottom right of image"), VIEWER_BOTTOM_RIGHT("Bottom right of viewer"),
    IMAGE_TOP_LEFT("Top left of image"), VIEWER_TOP_LEFT("Top left of viewer"), IMAGE_TOP_RIGHT("Top right of image"),
    VIEWER_TOP_RIGHT("Top right of viewer");

    public final String name;

    @Contract(pure = true)
    ScaleBarLocation(final String name) {
        this.name = name;
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return name;
    }

    @Contract(pure = true)
    public boolean isRelativeToViewer() {
        return this == VIEWER_BOTTOM_LEFT || this == VIEWER_BOTTOM_RIGHT || this == VIEWER_TOP_LEFT || this == VIEWER_TOP_RIGHT;
    }

    @Contract(pure = true)
    public boolean isRelativeToImage() {
        return this == IMAGE_BOTTOM_LEFT || this == IMAGE_BOTTOM_RIGHT || this == IMAGE_TOP_LEFT || this == IMAGE_TOP_RIGHT;
    }
}
