package plugins.tprovoost.scale;

import icy.gui.main.GlobalSequenceListener;
import icy.main.Icy;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginDaemon;
import icy.sequence.Sequence;

public class Scale extends Plugin implements PluginDaemon
{
    ScalePainter sp;
    GlobalSequenceListener globalSequenceListener;

    @Override
    public void run()
    {
        // nothing to do here
    }

    @Override
    public void stop()
    {
        for (Sequence sequence : Icy.getMainInterface().getSequences())
            sequence.removeOverlay(sp);

        Icy.getMainInterface().removeGlobalSequenceListener(globalSequenceListener);
    }

    @Override
    public void init()
    {
        // create painter
        sp = new ScalePainter();
        globalSequenceListener = new GlobalSequenceListener()
        {
            @Override
            public void sequenceOpened(Sequence sequence)
            {
                sequence.addOverlay(sp);
            }

            @Override
            public void sequenceClosed(Sequence sequence)
            {
                sequence.removeOverlay(sp);
            }
        };

        // add the Scale Bar overlay on opened sequences
        for (Sequence sequence : Icy.getMainInterface().getSequences())
            sequence.addOverlay(sp);

        // listen new opened sequence
        Icy.getMainInterface().addGlobalSequenceListener(globalSequenceListener);
    }
}
