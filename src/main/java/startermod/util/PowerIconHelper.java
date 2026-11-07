package startermod.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import startermod.StarterMod;
import java.util.HashMap;
import java.util.Map;

public final class PowerIconHelper {
    private static final int LARGE_ICON_SIZE = 84;
    private static final int SMALL_ICON_SIZE = 32;
    private static final Map<String, TextureAtlas.AtlasRegion> CACHE = new HashMap<>();

    private PowerIconHelper() {
    }

    public static TextureAtlas.AtlasRegion loadLarge(String iconName) {
        return load(iconName + "84.png", LARGE_ICON_SIZE);
    }

    public static TextureAtlas.AtlasRegion loadSmall(String iconName) {
        return load(iconName + "32.png", SMALL_ICON_SIZE);
    }

    private static TextureAtlas.AtlasRegion load(String fileName, int size) {
        TextureAtlas.AtlasRegion cached = CACHE.get(fileName);
        if (cached != null) {
            return cached;
        }
        Texture texture = ImageMaster.loadImage(StarterMod.assetPath("images/powers/" + fileName));
        if (texture == null) {
            throw new IllegalStateException("Missing Roxy power icon: " + fileName);
        }
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureAtlas.AtlasRegion region = new TextureAtlas.AtlasRegion(texture, 0, 0, size, size);
        CACHE.put(fileName, region);
        return region;
    }
}
