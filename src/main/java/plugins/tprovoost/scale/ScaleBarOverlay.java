package plugins.tprovoost.scale;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas2D;
import icy.math.MathUtil;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.painter.Overlay;
import icy.preferences.XMLPreferences;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.util.ColorUtil;
import icy.util.GraphicsUtil;
import plugins.adufour.vars.gui.model.IntegerRangeModel;
import plugins.adufour.vars.gui.swing.SwingVarEditor;
import plugins.adufour.vars.lang.Var;
import plugins.adufour.vars.lang.VarBoolean;
import plugins.adufour.vars.lang.VarColor;
import plugins.adufour.vars.lang.VarEnum;
import plugins.adufour.vars.lang.VarInteger;
import plugins.kernel.canvas.VtkCanvas;

/**
 * Scale Bar Overay class
 * 
 * @author Stephane
 */
public class ScaleBarOverlay extends Overlay
{
    private static final double[] scaleRoundedFactors = {1.0D, 2.0D, 3.0D, 4.0D, 5.0D, 6.0D, 7.0D, 8.0D, 9.0D, 10.0D,
            20.0D, 30.0D, 40.0D, 50.0D, 60.0D, 70.0D, 80.0D, 90.0D, 100.0D, 200.0D, 300.0D, 400.0D, 500.0D, 600.0D,
            700.0D, 800.0D, 900.0D};

    private final XMLPreferences preferences;
    private final Line2D.Double line;
    private JPanel optionPanel;
    private boolean init = false;

    // settings
    private final VarEnum<ScaleBarLocation> location;
    private final VarColor color;
    private final VarBoolean showText;
    private final VarInteger size;
    private final VarInteger thickness;
    private final VarEnum<ScaleBarUnit> unit;
    private final VarBoolean autoSize;

    public ScaleBarOverlay(XMLPreferences preferences)
    {
        super("Scale bar", OverlayPriority.TOPMOST);

        this.preferences = preferences;
        line = new Line2D.Double();

        // scale bar location
        location = new VarEnum<ScaleBarLocation>("Location",
                ScaleBarLocation.valueOf(preferences.get("location", ScaleBarLocation.VIEWER_BOTTOM_LEFT.name())))
        {
            @Override
            public void setValue(ScaleBarLocation newValue)
            {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.put("location", newValue.name());

                painterChanged();
            }
        };

        // scale bar color
        color = new VarColor("Color", new Color(preferences.getInt("color", new Color(255, 255, 255, 255).getRGB())))
        {
            @Override
            public void setValue(Color newValue)
            {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putInt("color", newValue.getRGB());

                painterChanged();
            }
        };

        // Display text over the scale bar
        showText = new VarBoolean("Display size", preferences.getBoolean("showText", true))
        {
            @Override
            public void setValue(Boolean newValue)
            {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putBoolean("showText", newValue);

                painterChanged();
            }
        };

        // Scale bar size (default: 10 units)
        size = new VarInteger("Size", preferences.getInt("size", 10))
        {
            @Override
            public void setValue(Integer newValue)
            {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putInt("size", newValue);

                painterChanged();
            }
        };

        // Scale bar thickness (default: 5)
        thickness = new VarInteger("Thickness", preferences.getInt("thickness", 5))
        {
            @Override
            public void setValue(Integer newValue)
            {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putInt("thickness", newValue);

                painterChanged();
            }
        };

        // Scale bar unit (default: microns)
        unit = new VarEnum<ScaleBarUnit>("Unit",
                ScaleBarUnit.valueOf(preferences.get("unit", ScaleBarUnit.MICRO.name())))
        {
            @Override
            public void setValue(ScaleBarUnit newValue)
            {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.put("unit", newValue.name());

                painterChanged();
            }
        };

        // auto-adjust the size and unit of the scale bar
        autoSize = new VarBoolean("Auto-adjust size", preferences.getBoolean("autoSize", false))
        {
            @Override
            public void setValue(Boolean newValue)
            {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putBoolean("autoSize", newValue);

                painterChanged();

            }
        };

        Integer currentSize = size.getValue();
        size.setDefaultEditorModel(new IntegerRangeModel(10, 1, 999, 1));
        size.setValue(currentSize);

        Integer currentThickness = thickness.getValue();
        thickness.setDefaultEditorModel(new IntegerRangeModel(5, 1, 20, 1));
        thickness.setValue(currentThickness);

        // do graphical stuff to the graphical thread
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                initOptionPanel();
            }
        });
    }

    /**
     * Scale bar color (default: white)
     */

    private void initOptionPanel()
    {
        optionPanel = new JPanel(new GridBagLayout());

        for (Var<?> variable : new Var<?>[] {location, color, autoSize, size, thickness, showText})
        {
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(2, 10, 2, 5);
            gbc.fill = GridBagConstraints.BOTH;
            optionPanel.add(new JLabel(variable.getName()), gbc);

            // special case: show unit next to size
            if (variable == size)
            {
                gbc.weightx = 0.5;

                SwingVarEditor<?> editor = (SwingVarEditor<?>) variable.createVarEditor(true);
                optionPanel.add(editor.getEditorComponent(), gbc);

                gbc.gridwidth = GridBagConstraints.REMAINDER;

                SwingVarEditor<?> unitEditor = (SwingVarEditor<?>) unit.createVarEditor(true);
                optionPanel.add(unitEditor.getEditorComponent(), gbc);
            }
            else
            {
                gbc.weightx = 1;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                SwingVarEditor<?> editor = (SwingVarEditor<?>) variable.createVarEditor(true);
                optionPanel.add(editor.getEditorComponent(), gbc);
            }
        }
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
    {
        if (canvas instanceof VtkCanvas && !init)
        {
            init = true;

            // VtkCanvas vtk = (VtkCanvas) canvas;

            // vtkScalarBarActor sb = new vtkScalarBarActor();

            // vtk.getRenderer().AddActor(sb);

            return;
        }
        else if (g == null || !(canvas instanceof IcyCanvas2D))
            return;

        final IcyCanvas2D c2 = (IcyCanvas2D) canvas;
        final Graphics2D g2 = (Graphics2D) g.create();

        if (autoSize.getValue())
        {
            int sizeW = Math.min(c2.getCanvasSizeX() / 4, Math.max(c2.getCanvasSizeX() / 7, 120));
            // sequence.getSizeX() / 7;

            // don't draw a scale bar larger than the image itself
            if (location.getValue().isRelativeToImage())
                // sizeW = Math.min(c2.getCanvasSizeX() / 4, Math.max(c2.getCanvasSizeX() / 7, 120));
                sizeW = (int) Math.min(sizeW, sequence.getSizeX() * canvas.getScaleX() * 0.5);

            double valueReal = (sizeW * sequence.getPixelSizeX() / canvas.getScaleX());
            UnitPrefix bestUnit = UnitUtil.getBestUnit(valueReal * 0.1, UnitUtil.UnitPrefix.MICRO, 1);
            double valueRealBestUnit = UnitUtil.getValueInUnit(valueReal, UnitPrefix.MICRO, bestUnit);

            double closestScale = MathUtil.closest(valueRealBestUnit, scaleRoundedFactors);
            size.setValue((int) closestScale);
            unit.setValue(ScaleBarUnit.getUnitFrom(bestUnit));
        }

        String text = size.getValue() + " " + unit.getValue();

        double length = UnitUtil.getValueInUnit(size.getValue(), unit.getValue().asUnitPrefix(), UnitPrefix.MICRO)
                / sequence.getPixelSizeX();

        int textYfactor = 0;
        float finalThickness = this.thickness.getValue().floatValue();

        switch (location.getValue())
        {
            case VIEWER_BOTTOM_LEFT:
                g2.transform(c2.getInverseTransform());
                line.x1 = canvas.getCanvasSizeX() * 0.05;
                line.x2 = line.x1 + length * c2.getScaleX();
                line.y1 = line.y2 = canvas.getCanvasSizeY() * 0.95;
                textYfactor = -2;
                break;

            case IMAGE_BOTTOM_LEFT:
                line.x1 = sequence.getSizeX() * 0.05;
                line.x2 = line.x1 + length;
                line.y1 = line.y2 = sequence.getSizeY() * 0.95;
                textYfactor = -2;
                finalThickness = (float) c2.canvasToImageLogDeltaX((int) finalThickness);
                break;

            case VIEWER_BOTTOM_RIGHT:
                g2.transform(c2.getInverseTransform());
                line.x1 = canvas.getCanvasSizeX() * 0.95;
                line.x2 = line.x1 - length * c2.getScaleX();
                line.y1 = line.y2 = canvas.getCanvasSizeY() * 0.95;
                textYfactor = -2;
                break;

            case IMAGE_BOTTOM_RIGHT:
                line.x1 = sequence.getSizeX() * 0.95;
                line.x2 = line.x1 - length;
                line.y1 = line.y2 = sequence.getSizeY() * 0.95;
                textYfactor = -2;
                finalThickness = (float) c2.canvasToImageLogDeltaX((int) finalThickness);
                break;

            case VIEWER_TOP_LEFT:
                g2.transform(c2.getInverseTransform());
                line.x1 = canvas.getCanvasSizeX() * 0.05;
                line.x2 = line.x1 + length * c2.getScaleX();
                line.y1 = line.y2 = canvas.getCanvasSizeY() * 0.05;
                textYfactor = 4;
                break;

            case IMAGE_TOP_LEFT:
                line.x1 = sequence.getSizeX() * 0.05;
                line.x2 = line.x1 + length;
                line.y1 = line.y2 = sequence.getSizeY() * 0.05;
                textYfactor = 4;
                finalThickness = (float) c2.canvasToImageLogDeltaX((int) finalThickness);
                break;

            case VIEWER_TOP_RIGHT:
                g2.transform(c2.getInverseTransform());
                line.x1 = canvas.getCanvasSizeX() * 0.95;
                line.x2 = line.x1 - length * c2.getScaleX();
                line.y1 = line.y2 = canvas.getCanvasSizeY() * 0.05;
                textYfactor = 4;
                break;

            case IMAGE_TOP_RIGHT:
                line.x1 = sequence.getSizeX() * 0.95;
                line.x2 = line.x1 - length;
                line.y1 = line.y2 = sequence.getSizeY() * 0.05;
                textYfactor = 4;
                finalThickness = (float) c2.canvasToImageLogDeltaX((int) finalThickness);
                break;
        }

        // draw scale bar line
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setStroke(new BasicStroke(finalThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        if (ColorUtil.getLuminance(color.getValue()) > 128)
            g2.setColor(Color.darkGray);
        else
            g2.setColor(Color.lightGray);
        line.x1 += 1;
        line.y1 += 1;
        line.x2 += 1;
        line.y2 += 1;
        g2.draw(line);
        line.x1 -= 1;
        line.y1 -= 1;
        line.x2 -= 1;
        line.y2 -= 1;
        g2.setColor(color.getValue());
        g2.draw(line);

        // draw text
        if (showText.getValue())
        {
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, finalThickness * 3));
            GraphicsUtil.drawHCenteredString(g2, text, (int) line.getBounds().getCenterX(),
                    (int) (line.getBounds().getCenterY() + (finalThickness * textYfactor * 3)), true);
        }

        g2.dispose();
    }

    @Override
    public JPanel getOptionsPanel()
    {
        return optionPanel;
    }

    /**
     * @return <code>true</code> if the scale bar automatically adjusts its size and unit
     */
    public boolean getAutoSize()
    {
        return autoSize.getValue();
    }

    /**
     * @return the color of the scale bar
     */
    public Color getColor()
    {
        return color.getValue();
    }

    /**
     * @return the location of the scale bar on the image
     */
    public ScaleBarLocation getLocation()
    {
        return location.getValue();
    }

    /**
     * @return the scale bar size (in metric units as defined by the pixel size)
     */
    public double getSize()
    {
        return size.getValue();
    }

    /**
     * @return whether the scale bar's size is currently displayed next to it
     */
    public boolean getTextDisplay()
    {
        return showText.getValue();
    }

    /**
     * Sets whether the scale should automatically guess its optimal size and unit
     * 
     * @param autoSize
     */
    public void setAutoSize(boolean autoSize)
    {
        this.autoSize.setValue(autoSize);
    }

    /**
     * Sets the color of the scale bar
     * 
     * @param color
     *        the new color of the scale bar
     */
    public void setColor(Color color)
    {
        this.color.setValue(color);
    }

    /**
     * Sets the location of the scale bar
     * 
     * @param location
     *        the new location of the scale bar
     */
    public void setLocation(ScaleBarLocation location)
    {
        this.location.setValue(location);
    }

    /**
     * Sets whether the scale bar's size should appear next to it
     * 
     * @param displayText
     */
    public void setTextDisplay(boolean displayText)
    {
        this.showText.setValue(displayText);
    }

    /**
     * Sets the size of the scale bar in metric units, as defined by the pixel size
     * 
     * @param size
     *        the new size of the scale bar
     */
    public void setSize(int size)
    {
        this.size.setValue(size);
    }

    /**
     * Sets the scale bar unit
     * 
     * @param unit
     *        the new unit
     */
    public void setUnit(UnitPrefix unit)
    {
        this.unit.setValue(ScaleBarUnit.getUnitFrom(unit));
    }
}