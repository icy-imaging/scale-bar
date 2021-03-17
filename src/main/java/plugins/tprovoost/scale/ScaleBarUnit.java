package plugins.tprovoost.scale;

import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;

public enum ScaleBarUnit
{
    KILO, NONE, MILLI, MICRO, NANO;

    @Override
    public String toString()
    {
        switch (this)
        {
            case KILO:
                return "km";
            case NONE:
                return "m";
            case MILLI:
                return "mm";
            default:
            case MICRO:
                return UnitUtil.MICRO_STRING + "m";
            case NANO:
                return "nm";
        }
    }

    public UnitPrefix asUnitPrefix()
    {
        switch (this)
        {
            case KILO:
                return UnitPrefix.KILO;
            case NONE:
                return UnitPrefix.NONE;
            case MILLI:
                return UnitPrefix.MILLI;
            default:
            case MICRO:
                return UnitPrefix.MICRO;
            case NANO:
                return UnitPrefix.NANO;
        }
    }

    public static ScaleBarUnit getUnitFrom(UnitPrefix up)
    {
        switch (up)
        {
            case GIGA:
            case MEGA:
            case KILO:
                return KILO;
            case NONE:
                return NONE;
            case MILLI:
                return MILLI;
            case MICRO:
            default:
                return MICRO;
            case NANO:
            case PICO:
                return NANO;
        }
    }
}
