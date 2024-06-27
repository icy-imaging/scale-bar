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

import org.bioimageanalysis.icy.common.color.ColorUtil;
import org.bioimageanalysis.icy.common.geom.shape.ShapeUtil;
import org.bioimageanalysis.icy.common.math.MathUtil;
import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.gui.GraphicsUtil;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas2D;
import org.bioimageanalysis.icy.gui.canvas.VtkCanvas;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.system.preferences.XMLPreferences;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.NotNull;
import plugins.adufour.vars.gui.model.IntegerRangeModel;
import plugins.adufour.vars.gui.swing.SwingVarEditor;
import plugins.adufour.vars.lang.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * Scale Bar Overlay class
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ScaleBarOverlay extends Overlay {
    private static final double[] scaleRoundedFactors = {1.0D, 2.0D, 3.0D, 4.0D, 5.0D, 6.0D, 7.0D, 8.0D, 9.0D, 10.0D, 20.0D, 30.0D, 40.0D, 50.0D, 60.0D, 70.0D,
            80.0D, 90.0D, 100.0D, 200.0D, 300.0D, 400.0D, 500.0D, 600.0D, 700.0D, 800.0D, 900.0D};

    //private final XMLPreferences preferences;
    private final Line2D.Double line;
    private JPanel optionPanel;
    private boolean init = false;

    // settings
    private final VarEnum<ScaleBarLocation> location;
    private final VarColor color;
    private final VarBoolean opaque;
    private final VarBoolean showText;
    private final VarInteger size;
    private final VarInteger thickness;
    private final VarEnum<ScaleBarUnit> unit;
    private final VarBoolean autoSize;

    public ScaleBarOverlay(final @NotNull XMLPreferences preferences) {
        super("Scale bar", OverlayPriority.TOPMOST);

        //this.preferences = preferences;
        line = new Line2D.Double();

        // scale bar location
        location = new VarEnum<>("Location", ScaleBarLocation.valueOf(preferences.get("location", ScaleBarLocation.VIEWER_BOTTOM_LEFT.name()))) {
            @Override
            public void setValue(final ScaleBarLocation newValue) {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.put("location", newValue.name());

                painterChanged();
            }
        };

        // scale bar color
        color = new VarColor("Color", new Color(preferences.getInt("color", new Color(255, 255, 255, 255).getRGB()))) {
            @Override
            public void setValue(final Color newValue) {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putInt("color", newValue.getRGB());

                painterChanged();
            }
        };

        // Opaque background
        opaque = new VarBoolean("Opaque", preferences.getBoolean("opaque", false)) {
            @Override
            public void setValue(final Boolean newValue) {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putBoolean("opaque", newValue);

                painterChanged();
            }
        };

        // Display text over the scale bar
        showText = new VarBoolean("Display size", preferences.getBoolean("showText", true)) {
            @Override
            public void setValue(final Boolean newValue) {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putBoolean("showText", newValue);

                painterChanged();
            }
        };

        // Scale bar size (default: 10 units)
        size = new VarInteger("Size", preferences.getInt("size", 10)) {
            @Override
            public void setValue(final Integer newValue) {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putInt("size", newValue);

                painterChanged();
            }
        };

        // Scale bar thickness (default: 5)
        thickness = new VarInteger("Thickness", preferences.getInt("thickness", 5)) {
            @Override
            public void setValue(final Integer newValue) {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putInt("thickness", newValue);

                painterChanged();
            }
        };

        // Scale bar unit (default: microns)
        unit = new VarEnum<>("Unit", ScaleBarUnit.valueOf(preferences.get("unit", ScaleBarUnit.MICRO.name()))) {
            @Override
            public void setValue(final ScaleBarUnit newValue) {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.put("unit", newValue.name());

                painterChanged();
            }
        };

        // auto-adjust the size and unit of the scale bar
        autoSize = new VarBoolean("Auto-adjust size", preferences.getBoolean("autoSize", false)) {
            @Override
            public void setValue(final Boolean newValue) {
                if (getValue().equals(newValue))
                    return;

                super.setValue(newValue);

                preferences.putBoolean("autoSize", newValue);

                painterChanged();

            }
        };

        final Integer currentSize = size.getValue();
        size.setDefaultEditorModel(new IntegerRangeModel(10, 1, 999, 1));
        size.setValue(currentSize);

        final Integer currentThickness = thickness.getValue();
        thickness.setDefaultEditorModel(new IntegerRangeModel(5, 1, 20, 1));
        thickness.setValue(currentThickness);

        // do graphical stuff to the graphical thread
        ThreadUtil.invokeLater(this::initOptionPanel);
    }

    /**
     * Scale bar color (default: white)
     */

    private void initOptionPanel() {
        optionPanel = new JPanel(new GridBagLayout());

        for (final Var<?> variable : new Var<?>[]{location, color, opaque, autoSize, size, thickness, showText}) {
            final GridBagConstraints gbc = new GridBagConstraints();

            gbc.insets = new Insets(2, 10, 2, 5);
            gbc.fill = GridBagConstraints.BOTH;
            optionPanel.add(new JLabel(variable.getName()), gbc);

            // special case: show unit next to size
            if (variable == size) {
                gbc.weightx = 0.5;

                final SwingVarEditor<?> editor = (SwingVarEditor<?>) variable.createVarEditor(true);
                optionPanel.add(editor.getEditorComponent(), gbc);

                gbc.gridwidth = GridBagConstraints.REMAINDER;

                final SwingVarEditor<?> unitEditor = (SwingVarEditor<?>) unit.createVarEditor(true);
                optionPanel.add(unitEditor.getEditorComponent(), gbc);
            }
            else {
                gbc.weightx = 1;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                final SwingVarEditor<?> editor = (SwingVarEditor<?>) variable.createVarEditor(true);
                optionPanel.add(editor.getEditorComponent(), gbc);
            }
        }
    }

    @Override
    public void paint(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas) {
        if (canvas instanceof VtkCanvas && !init) {
            init = true;
            return;
        }
        else if (g == null || !(canvas instanceof IcyCanvas2D))
            return;

        final Graphics2D g2 = (Graphics2D) g.create();
        final IcyCanvas2D canvas2d = (IcyCanvas2D) canvas;

        if (autoSize.getValue()) {
            int sizeW = Math.min(canvas.getCanvasSizeX() / 4, Math.max(canvas.getCanvasSizeX() / 7, 120));
            // sequence.getSizeX() / 7;

            // don't draw a scale bar larger than the image itself
            if (location.getValue().isRelativeToImage())
                // sizeW = Math.min(c2.getCanvasSizeX() / 4, Math.max(c2.getCanvasSizeX() / 7, 120));
                sizeW = (int) Math.min(sizeW, sequence.getSizeX() * canvas.getScaleX() * 0.5);

            final double valueReal = (sizeW * sequence.getPixelSizeX() / canvas.getScaleX());
            final UnitUtil.UnitPrefix bestUnit = UnitUtil.getBestUnit(valueReal * 0.1, UnitUtil.UnitPrefix.MICRO, 1);
            final double valueRealBestUnit = UnitUtil.getValueInUnit(valueReal, UnitUtil.UnitPrefix.MICRO, bestUnit);

            final double closestScale = MathUtil.closest(valueRealBestUnit, scaleRoundedFactors);
            size.setValue((int) closestScale);
            unit.setValue(ScaleBarUnit.getUnitFrom(bestUnit));
        }

        final String text = size.getValue() + " " + unit.getValue();
        double length = UnitUtil.getValueInUnit(size.getValue(), unit.getValue().asUnitPrefix(), UnitUtil.UnitPrefix.MICRO) / sequence.getPixelSizeX();
        final double unit;
        final double sizeX, sizeY;

        // image relative
        if (location.getValue().isRelativeToImage()) {
            unit = canvas.canvasToImageDeltaX(1);
            sizeX = canvas.getImageSizeX();
            sizeY = canvas.getImageSizeY();
        }
        // viewer relative
        else {
            unit = 1d;
            sizeX = canvas.getCanvasSizeX();
            sizeY = canvas.getCanvasSizeY();
            // adjust length
            length = canvas.imageToCanvasDeltaX(length);
            // need to reserve transform
            g2.transform(canvas2d.getInverseTransform());
        }

        final float finalThickness = (float) (thickness.getValue().doubleValue() * unit);
        final float fontSize = (float) ((GeneralPreferences.getGuiFontSize() + 2) * unit);
        final double borderDist = 20d * unit;

        // prepare rendering
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setStroke(new BasicStroke(finalThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        Rectangle2D textBnds = new Rectangle2D.Double();

        // draw text ? prepare font and compute text bounds
        if (showText.getValue()) {
            // set graphics font
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, fontSize));
            // get text bounds
            textBnds = GraphicsUtil.getStringBounds(g2, text);
        }

        switch (location.getValue()) {
            case VIEWER_BOTTOM_LEFT:
            case IMAGE_BOTTOM_LEFT:
                line.x1 = borderDist;
                line.x2 = line.x1 + length;
                line.y1 = sizeY - (textBnds.getHeight() + (borderDist / 2d));
                break;

            case VIEWER_BOTTOM_RIGHT:
            case IMAGE_BOTTOM_RIGHT:
                line.x1 = sizeX - borderDist;
                line.x2 = line.x1 - length;
                line.y1 = sizeY - (textBnds.getHeight() + (borderDist / 2d));
                break;

            case VIEWER_TOP_LEFT:
            case IMAGE_TOP_LEFT:
                line.x1 = borderDist;
                line.x2 = line.x1 + length;
                line.y1 = borderDist / 2d;
                break;

            case VIEWER_TOP_RIGHT:
            case IMAGE_TOP_RIGHT:
                line.x1 = sizeX - borderDist;
                line.x2 = line.x1 - length;
                line.y1 = borderDist / 2d;
                break;
        }

        line.y2 = line.y1;

        // opaque background ?
        if (opaque.getValue()) {
            if (ColorUtil.getLuminance(color.getValue()) > 128)
                g2.setColor(Color.black);
            else
                g2.setColor(Color.white);

            // base bounds
            final Rectangle2D rect = line.getBounds2D();
            // give margins
            ShapeUtil.enlarge(rect, 8d * unit, 8d * unit, true);
            // draw text ? enlarge using text bounds
            if (showText.getValue())
                rect.setRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() + textBnds.getHeight());

            g2.fill(rect);
        }
        else {
            // transparent background --> just draw a light shadow
            if (ColorUtil.getLuminance(color.getValue()) > 128)
                g2.setColor(Color.darkGray);
            else
                g2.setColor(Color.lightGray);

            line.x1 += unit;
            line.y1 += unit;
            line.x2 += unit;
            line.y2 += unit;
            g2.draw(line);
            line.x1 -= unit;
            line.y1 -= unit;
            line.x2 -= unit;
            line.y2 -= unit;
        }

        // draw scale bar line
        g2.setColor(color.getValue());
        g2.draw(line);

        // draw text
        if (showText.getValue()) {
            final float baseX = (float) line.getBounds().getCenterX();
            final float baseY = (float) (line.getBounds().getCenterY() + (unit * 4d));
            final float x = (float) (baseX - (textBnds.getWidth() / 2d));
            final float y = (float) (baseY - textBnds.getY());

            // need to draw text shadow first
            if (!opaque.getValue()) {
                if (ColorUtil.getLuminance(color.getValue()) > 128)
                    g2.setColor(Color.darkGray);
                else
                    g2.setColor(Color.lightGray);

                // draw text in shadow
                g2.drawString(text, (float) (x + unit), (float) (y + unit));
                // restore color
                g2.setColor(color.getValue());
            }

            // draw text
            g2.drawString(text, x, y);
        }

        g2.dispose();
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionPanel;
    }

    /**
     * @return <code>true</code> if the scale bar automatically adjusts its size and unit
     */
    public boolean getAutoSize() {
        return autoSize.getValue();
    }

    /**
     * @return the color of the scale bar
     */
    public Color getColor() {
        return color.getValue();
    }

    /**
     * @return the opaque property of the scale bar
     */
    public boolean getOpaque() {
        return opaque.getValue();
    }

    /**
     * @return the location of the scale bar on the image
     */
    public ScaleBarLocation getLocation() {
        return location.getValue();
    }

    /**
     * @return the scale bar size (in metric units as defined by the pixel size)
     */
    public double getSize() {
        return size.getValue();
    }

    /**
     * @return whether the scale bar's size is currently displayed next to it
     */
    public boolean getTextDisplay() {
        return showText.getValue();
    }

    /**
     * Sets whether the scale should automatically guess its optimal size and unit
     */
    public void setAutoSize(final boolean autoSize) {
        this.autoSize.setValue(autoSize);
    }

    /**
     * Sets the color of the scale bar
     *
     * @param color the new color of the scale bar
     */
    public void setColor(final Color color) {
        this.color.setValue(color);
    }

    /**
     * Sets whether the scale bar should have an opaque background or not
     */
    public void setOpaque(final boolean opaque) {
        this.opaque.setValue(opaque);
    }

    /**
     * Sets the location of the scale bar
     *
     * @param location the new location of the scale bar
     */
    public void setLocation(final ScaleBarLocation location) {
        this.location.setValue(location);
    }

    /**
     * Sets whether the scale bar's size should appear next to it
     */
    public void setTextDisplay(final boolean displayText) {
        this.showText.setValue(displayText);
    }

    /**
     * Sets the size of the scale bar in metric units, as defined by the pixel size
     *
     * @param size the new size of the scale bar
     */
    public void setSize(final int size) {
        this.size.setValue(size);
    }

    /**
     * Sets the scale bar unit
     *
     * @param unit the new unit
     */
    public void setUnit(final UnitUtil.UnitPrefix unit) {
        this.unit.setValue(ScaleBarUnit.getUnitFrom(unit));
    }
}