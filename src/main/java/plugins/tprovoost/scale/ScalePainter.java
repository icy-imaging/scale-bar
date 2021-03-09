package plugins.tprovoost.scale;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.math.MathUtil;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.painter.Overlay;
import icy.sequence.Sequence;
import icy.util.GraphicsUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

public class ScalePainter extends Overlay
{
    /**
     * Possible rounded scale factors : 1 --> 900
     */
    final static double[] scaleRoundedFactors = new double[] {1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 20d, 30d, 40d,
            50d, 60d, 70d, 80d, 90d, 100d, 200d, 300d, 400d, 500d, 600d, 700d, 800d, 900d};

    public ScalePainter()
    {
        super("Scale Bar");
    }

    @Override
    public void paint(Graphics2D g2, Sequence sequence, IcyCanvas canvas)
    {
        if (g2 == null)
            return;
        if (!(canvas instanceof IcyCanvas2D))
            return;

        final IcyCanvas2D c2d = (IcyCanvas2D) canvas;
        final Graphics2D g = (Graphics2D) g2.create();

        double scaleFactor = c2d.getScaleX();
        g.transform(c2d.getInverseTransform());

        BasicStroke[] stroke = new BasicStroke[4];
        stroke[0] = new BasicStroke(2f);
        stroke[1] = new BasicStroke(3f);
        stroke[2] = new BasicStroke(4f);
        stroke[3] = new BasicStroke(5f);
        int w = canvas.getCanvasSizeX();
        int h = canvas.getCanvasSizeY();
        double pxSize = sequence.getPixelSizeX();
        int space = 20; // spacing from borders

        int sizeW = w / 8;
        // get the value with the best display possible
        double valueReal = sizeW * pxSize / scaleFactor / 10;
        UnitPrefix bestUnit = UnitUtil.getBestUnit(valueReal, UnitPrefix.MICRO, 1);
        double valueRealBestUnit = UnitUtil.getValueInUnit(valueReal * 10, UnitPrefix.MICRO, bestUnit);

        double closestScale = MathUtil.closest(valueRealBestUnit, scaleRoundedFactors);

        // closest scale in px
        int closestScPx = (int) (UnitUtil.getValueInUnit(closestScale, bestUnit, UnitPrefix.MICRO) / pxSize * scaleFactor);
        // int closestScPx = closestScale;

        g.setColor(Color.BLACK);
        g.setStroke(stroke[3]);
        // g.drawLine(w - space - closestScPx, h - space, w - space, h - space);
        g.drawLine(space, h - space, space + closestScPx, h - space);

        g.setColor(Color.WHITE);
        g.setStroke(stroke[2]);
        g.drawLine(space, h - space, space + closestScPx, h - space);

        int fontSize = 14;
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        g.setFont(font);
        String pixelString = "" + closestScale + " " + bestUnit + "m";
        int txW = g.getFontMetrics().charsWidth(pixelString.toCharArray(), 0, pixelString.length());
        int txH = g.getFontMetrics().getHeight();

        g.setColor(Color.black);
        // GraphicsUtil.drawString(g, pixelString, w - space - closestScPx / 2 -
        // txW / 2, h - space - txH, true);
        GraphicsUtil.drawString(g, pixelString, space + closestScPx / 2 - txW / 2, h - space - txH, true);

        g.dispose();
    }

    double convertScale(IcyCanvas canvas, double value)
    {
        return canvas.canvasToImageLogDeltaX((int) value);
    }

    public class CompositeStroke implements Stroke
    {
        private Stroke stroke1, stroke2;

        public CompositeStroke(Stroke stroke1, Stroke stroke2)
        {
            this.stroke1 = stroke1;
            this.stroke2 = stroke2;
        }

        @Override
        public Shape createStrokedShape(Shape shape)
        {
            return stroke2.createStrokedShape(stroke1.createStrokedShape(shape));
        }
    }

//    private int findBestScale(IcyCanvas2D canvas, Sequence s)
//    {
//        int w = s.getWidth();
//        int minSize = w / 8;
//        int maxSize = w / 4;
//        double pxSize = s.getPixelSizeX();
//        double tab[] = new double[maxSize - minSize];
//        double scaleFactor = canvas.getScaleX();
//
//        // generate all values
//        for (int i = minSize; i < maxSize; ++i)
//        {
//
//            // get the value with the best display possible
//            double valueReal = i * pxSize / scaleFactor;
//            UnitPrefix bestUnit = UnitUtil.getBestUnit(valueReal, UnitPrefix.MICRO, 1);
//            double bestValue = UnitUtil.getValueInUnit(valueReal, UnitPrefix.MICRO, bestUnit);
//            bestValue = ((int) (bestValue * 100)) / 100d;
//
//            tab[i - minSize] = bestValue;
//        }
//
//        // return the first value with a round value
//        for (int i = 0; i < maxSize - minSize; ++i)
//        {
//            double bestValue = tab[i];
//            if (bestValue == (int) bestValue)
//            {
//                return i + minSize;
//            }
//        }
//
//        // otherwise, return first value with only one decimal
//        for (int i = 0; i < maxSize - minSize; ++i)
//        {
//            double bestValue = tab[i];
//            if (bestValue == ((int) (bestValue * 10)) / 10d)
//            {
//                return i + minSize;
//            }
//        }
//
//        // otherwise, return w / 4
//        return w / 4;
//    }
}
