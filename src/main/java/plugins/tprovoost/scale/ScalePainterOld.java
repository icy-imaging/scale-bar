package plugins.tprovoost.scale;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.painter.Overlay;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.util.GraphicsUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

public class ScalePainterOld extends Overlay
{
	public ScalePainterOld()
	{
		super("Scale Bar");
	}

	@Override
	public void paint(Graphics2D g2, Sequence sequence, IcyCanvas canvas)
	{
		if (g2 == null)
			return;
		Graphics2D g = (Graphics2D) g2.create();
		// g.transform(((IcyCanvas2D) canvas).getInverseTransform());
		int w = sequence.getWidth();
		int h = sequence.getHeight();
		int sizeW = w / 10;
		double pxSize = sequence.getPixelSizeX();
		int spaceFBords = 20; // spacing from borders

		BasicStroke[] stroke = new BasicStroke[4];
		stroke[0] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(2));
		stroke[1] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(3));
		stroke[2] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(4));
		stroke[3] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(5));

		// get the value with the best display possible
		double valueReal = sizeW * pxSize / 10;
		UnitPrefix bestUnit = UnitUtil.getBestUnit(valueReal, UnitPrefix.MICRO, 1);
		double bestValue = UnitUtil.getValueInUnit(valueReal * 10, UnitPrefix.MICRO, bestUnit);

		// get the best scale possible
		int closestScale = 0;
		if (bestValue < 10)
		{
			closestScale = (int) bestValue;
		} else
			closestScale = (int) (bestValue / 10) * 10;

		// closest scale in px
		int closestScPx = (int) (UnitUtil.getValueInUnit(closestScale, bestUnit, UnitPrefix.MICRO) / pxSize);

		g.setColor(Color.BLACK);
		g.setStroke(stroke[3]);
		g.drawLine(w - spaceFBords - closestScPx, h - spaceFBords, w - spaceFBords, h - spaceFBords);

		g.setColor(Color.WHITE);
		g.setStroke(stroke[2]);
		g.drawLine(w - spaceFBords - closestScPx, h - spaceFBords, w - spaceFBords, h - spaceFBords);

		int fontSize = (int) canvas.canvasToImageLogDeltaX(14);
		Font font = new Font("Arial", Font.PLAIN, fontSize);
		g.setFont(font);
		String pixelString = "" + closestScale + " " + bestUnit + "m";
		int txW = g.getFontMetrics().charsWidth(pixelString.toCharArray(), 0, pixelString.length());
		int txH = g.getFontMetrics().getHeight();

		// g.fillRect(w - spaceFBords - closestScPx / 2 - txW / 2, h -
		// spaceFBords - txH, txW, txH);

		g.setColor(Color.black);
		GraphicsUtil.drawString(g, pixelString, w - spaceFBords - closestScPx / 2 - txW / 2, h - spaceFBords - txH, true);

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

		public Shape createStrokedShape(Shape shape)
		{
			return stroke2.createStrokedShape(stroke1.createStrokedShape(shape));
		}
	}
}
