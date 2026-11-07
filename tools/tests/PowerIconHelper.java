package startermod.util;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/** Test classpath adapter: only GPU texture loading is replaced. Never packaged. */
public final class PowerIconHelper {
    public static TextureAtlas.AtlasRegion loadLarge(String name) { return null; }
    public static TextureAtlas.AtlasRegion loadSmall(String name) { return null; }
}
