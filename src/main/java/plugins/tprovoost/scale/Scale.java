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

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginDescription;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginIcon;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginName;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginDaemon;
import org.bioimageanalysis.icy.gui.listener.GlobalSequenceListener;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.system.IcyHandledException;
import org.bioimageanalysis.icy.system.preferences.XMLPreferences;
import org.jetbrains.annotations.Contract;

/**
 * Scale bar plug-in for Icy. Displays a scale bar on 2D viewers, with various display adjustment
 * options, such as:
 * <ul>
 * <li>Color</li>
 * <li>Size and unit (manually or automatically)</li>
 * <li>Position (each image corner)</li>
 * <li>Hide/Show text</li>
 * </ul>
 * These options can be managed from the graphical interface (in the "Layers" section of the
 * inspector), or within scripts (see {@link #addScaleBarTo(Sequence)} and
 * {@link #removeScaleBarFrom(Sequence)}
 *
 * @author Thomas Provoost
 * @author Stephane Dallongeville
 * @author Alexandre Dufour
 * @author Thomas Musset
 */
@IcyPluginName("Scale Bar")
@IcyPluginDescription(shortDesc = "Displays a scale bar on 2D viewers, with various display adjustment options.")
@IcyPluginIcon(path = "/plugins/tprovoost/scale/icon/Scale_icon.png")
public class Scale extends Plugin implements PluginDaemon, GlobalSequenceListener {
    private static XMLPreferences preferences = null;

    @Override
    public void init() {
        if (preferences == null)
            preferences = getPreferencesRoot();

        Icy.getMainInterface().addGlobalSequenceListener(this);
    }

    @Override
    public void run() {
        for (final Sequence s : getSequences())
            addScaleBarTo(s);
    }

    @Override
    public void stop() {
        for (final Sequence s : getSequences())
            removeScaleBarFrom(s);

        Icy.getMainInterface().removeGlobalSequenceListener(this);
    }

    @Override
    public void sequenceOpened(final Sequence sequence) {
        addScaleBarTo(sequence);
    }

    @Override
    public void sequenceClosed(final Sequence sequence) {
        removeScaleBarFrom(sequence);
    }

    @Contract("null -> fail")
    public static ScaleBarOverlay addScaleBarTo(final Sequence sequence) {
        if (sequence == null)
            throw new IcyHandledException("Cannot add the scale bar: no sequence specified");

        if (sequence.hasOverlay(ScaleBarOverlay.class))
            return sequence.getOverlays(ScaleBarOverlay.class).getFirst();

        final ScaleBarOverlay overlay = new ScaleBarOverlay(preferences);

        sequence.addOverlay(overlay);

        // Try to detect if the sequence is a screenshot (via its name)
        // => hide the overlay by default (but don't remove it)
        final String name = sequence.getName().toLowerCase();
        if (name.startsWith("rendering") || name.startsWith("screen shot")) {
            for (final Viewer viewer : sequence.getViewers())
                viewer.getCanvas().getLayer(overlay).setVisible(false);
        }

        return overlay;
    }

    @Contract("null -> fail")
    public static void removeScaleBarFrom(final Sequence sequence) {
        if (sequence == null)
            throw new IcyHandledException("Cannot remove the scale bar: no sequence specified");

        for (final Overlay o : sequence.getOverlays(ScaleBarOverlay.class))
            sequence.removeOverlay(o);
    }
}
