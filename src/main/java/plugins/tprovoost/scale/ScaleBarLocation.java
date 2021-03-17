package plugins.tprovoost.scale;

public enum ScaleBarLocation
{
    IMAGE_BOTTOM_LEFT("Bottom left of image"), VIEWER_BOTTOM_LEFT("Bottom left of viewer"),
    IMAGE_BOTTOM_RIGHT("Bottom right of image"), VIEWER_BOTTOM_RIGHT("Bottom right of viewer"),
    IMAGE_TOP_LEFT("Top left of image"), VIEWER_TOP_LEFT("Top left of viewer"), IMAGE_TOP_RIGHT("Top right of image"),
    VIEWER_TOP_RIGHT("Top right of viewer");

    public final String name;

    private ScaleBarLocation(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return name;
    }

    public boolean isRelativeToViewer()
    {
        return this == VIEWER_BOTTOM_LEFT || this == VIEWER_BOTTOM_RIGHT || this == VIEWER_TOP_LEFT
                || this == VIEWER_TOP_RIGHT;
    }

    public boolean isRelativeToImage()
    {
        return this == IMAGE_BOTTOM_LEFT || this == IMAGE_BOTTOM_RIGHT || this == IMAGE_TOP_LEFT
                || this == IMAGE_TOP_RIGHT;
    }

}
