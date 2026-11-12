package roxymod.ui;

import basemod.abstracts.CustomEnergyOrb;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import roxymod.RoxyMod;

/**
 * Roxy's combat energy orb: a water vortex spinning inside a static frame, greyed and slowed
 * when energy is 0. Layers come from art_sources/tools/export_energy_orb.py.
 *
 * Extends CustomEnergyOrb only because CustomPlayer.getEnergyImage() requires one. The
 * superclass is built with its defaults (the already-loaded red textures) and its layer
 * logic is not used; the energy-gain flash is the game's blue one.
 */
public class RoxyEnergyOrb extends CustomEnergyOrb {
    private static final float ORB_SCALE = 1.15f;
    private static final float VORTEX_SPEED = 24.0f;
    private static final float GLINT_SPEED = -14.0f;
    private static final float NO_ENERGY_PACE = 0.25f;

    private final Texture frame = load("frame");
    private final Texture vortex = load("vortex");
    private final Texture vortexNoEnergy = load("vortex_d");
    private final Texture glints = load("glints");
    private final Texture glintsNoEnergy = load("glints_d");
    private float vortexAngle;
    private float glintAngle;

    public RoxyEnergyOrb() {
        super(null, null, null);
    }

    private static Texture load(String name) {
        Texture texture = ImageMaster.loadImage(RoxyMod.assetPath("images/ui/energyorb/" + name + ".png"));
        if (texture == null) {
            throw new IllegalStateException("Missing Roxy energy orb layer: " + name);
        }
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    @Override
    public Texture getEnergyImage() {
        return ImageMaster.BLUE_ORB_FLASH_VFX;
    }

    @Override
    public void updateOrb(int energyCount) {
        float step = Gdx.graphics.getDeltaTime() * (energyCount == 0 ? NO_ENERGY_PACE : 1.0f);
        this.vortexAngle = (this.vortexAngle + step * VORTEX_SPEED) % 360.0f;
        this.glintAngle = (this.glintAngle + step * GLINT_SPEED) % 360.0f;
    }

    @Override
    public void renderOrb(SpriteBatch sb, boolean enabled, float x, float y) {
        sb.setColor(Color.WHITE);
        draw(sb, enabled ? this.vortex : this.vortexNoEnergy, x, y, this.vortexAngle);
        draw(sb, enabled ? this.glints : this.glintsNoEnergy, x, y, this.glintAngle);
        draw(sb, this.frame, x, y, 0.0f);
    }

    private static void draw(SpriteBatch sb, Texture texture, float x, float y, float angle) {
        float scale = ORB_SCALE * Settings.scale;
        sb.draw(texture, x - 64.0f, y - 64.0f, 64.0f, 64.0f, 128.0f, 128.0f, scale, scale, angle,
            0, 0, 128, 128, false, false);
    }
}
