package plugins.tprovoost.scale;

import icy.gui.main.GlobalSequenceListener;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.painter.Overlay;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginDaemon;
import icy.preferences.XMLPreferences;
import icy.sequence.Sequence;
import icy.system.IcyHandledException;

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
 * @author Thomas Provoost, Stephane Dallongeville, Alexandre Dufour
 */
public class Scale extends Plugin implements PluginDaemon, GlobalSequenceListener
{
    private static XMLPreferences preferences = null;

    @Override
    public void init()
    {
        if (preferences == null)
            preferences = getPreferencesRoot();

        Icy.getMainInterface().addGlobalSequenceListener(this);
    }

    @Override
    public void run()
    {
        for (Sequence s : getSequences())
            addScaleBarTo(s);
    }

    @Override
    public void stop()
    {
        for (Sequence s : getSequences())
            removeScaleBarFrom(s);

        Icy.getMainInterface().removeGlobalSequenceListener(this);
    }

    @Override
    public void sequenceOpened(Sequence sequence)
    {
        addScaleBarTo(sequence);
    }

    @Override
    public void sequenceClosed(Sequence sequence)
    {
        removeScaleBarFrom(sequence);
    }

    public static ScaleBarOverlay addScaleBarTo(Sequence sequence)
    {
        if (sequence == null)
            throw new IcyHandledException("Cannot add the scale bar: no sequence specified");

        if (sequence.hasOverlay(ScaleBarOverlay.class))
            return sequence.getOverlays(ScaleBarOverlay.class).get(0);

        ScaleBarOverlay overlay = new ScaleBarOverlay(preferences);

        sequence.addOverlay(overlay);

        // Try to detect if the sequence is a screenshot (via its name)
        // => hide the overlay by default (but don't remove it)
        String name = sequence.getName().toLowerCase();
        if (name.startsWith("rendering") || name.startsWith("screen shot"))
        {
            for (Viewer viewer : sequence.getViewers())
                viewer.getCanvas().getLayer(overlay).setVisible(false);
        }

        return overlay;
    }

    public static void removeScaleBarFrom(Sequence sequence)
    {
        if (sequence == null)
            throw new IcyHandledException("Cannot remove the scale bar: no sequence specified");

        for (Overlay o : sequence.getOverlays(ScaleBarOverlay.class))
            sequence.removeOverlay(o);
    }
}
