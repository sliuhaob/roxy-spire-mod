package roxymod.characters;

import basemod.abstracts.CustomPlayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.screens.CharSelectInfo;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import java.util.ArrayList;
import roxymod.RoxyMod;
import roxymod.RoxyEnums;
import roxymod.animations.NoAnimation;
import roxymod.cards.RoxyBeginnerChant;
import roxymod.cards.RoxyDefend;
import roxymod.cards.RoxyWaterCurtain;
import roxymod.cards.RoxyWaterBullet;
import roxymod.relics.RoxyManaCrystal;
import roxymod.ui.RoxyEnergyOrb;

public class Roxy extends CustomPlayer {
    public static final String NAME = "洛琪希";
    public static final String DESCRIPTION = "擅长缩略咏唱的水圣级魔术师，通过准备术式施展水、冰与雷魔法。";

    private static final int STARTING_HP = 70;
    private static final int STARTING_GOLD = 99;
    private static final int HAND_SIZE = 5;
    private static final int ENERGY_PER_TURN = 3;

    private static final Color CARD_COLOR = new Color(0.25f, 0.75f, 1.0f, 1.0f);

    public static final String CHARACTER_IMG = RoxyMod.assetPath("images/character/roxy.png");
    public static final String SHOULDER_1 = RoxyMod.assetPath("images/character/shoulder.png");
    public static final String SHOULDER_2 = RoxyMod.assetPath("images/character/shoulder2.png");
    public static final String CORPSE_IMG = RoxyMod.assetPath("images/character/corpse.png");
    private static final String CAMPFIRE_IMG = RoxyMod.assetPath("images/character/roxy_campfire.png");
    private static final String TEXT_ORB_IMG = RoxyMod.assetPath("images/512/card_small_orb.png");
    private static final float CAMPFIRE_IMG_HEIGHT = 650.0f;
    private static final float CAMPFIRE_TARGET_X = -20.0f;
    private static final float CAMPFIRE_TARGET_Y = -5.0f;
    private static final float CAMPFIRE_SLIDE_START_OFFSET = -430.0f;
    private static final float CAMPFIRE_SLIDE_DURATION = 1.20f;
    private static Texture campfireTexture;
    private static TextureAtlas.AtlasRegion textOrbRegion;

    private float campfireSlideTimer = 0.0f;
    private boolean wasInCampfireRoom = false;

    public Roxy(String name) {
        super(name, RoxyEnums.ROXY, new RoxyEnergyOrb(), new NoAnimation());

        this.initializeClass(
            CHARACTER_IMG,
            SHOULDER_2,
            SHOULDER_1,
            CORPSE_IMG,
            this.getLoadout(),
            -4.0f,
            -16.0f,
            220.0f,
            290.0f,
            new EnergyManager(ENERGY_PER_TURN)
        );
    }

    @Override
    public ArrayList<String> getStartingDeck() {
        ArrayList<String> deck = new ArrayList<>();
        deck.add(RoxyWaterBullet.ID);
        deck.add(RoxyWaterBullet.ID);
        deck.add(RoxyWaterBullet.ID);
        deck.add(RoxyWaterBullet.ID);
        deck.add(RoxyDefend.ID);
        deck.add(RoxyDefend.ID);
        deck.add(RoxyDefend.ID);
        deck.add(RoxyDefend.ID);
        deck.add(RoxyBeginnerChant.ID);
        deck.add(RoxyWaterCurtain.ID);
        return deck;
    }

    @Override
    public ArrayList<String> getStartingRelics() {
        ArrayList<String> relics = new ArrayList<>();
        relics.add(RoxyManaCrystal.ID);
        UnlockTracker.markRelicAsSeen(RoxyManaCrystal.ID);
        return relics;
    }

    @Override
    public CharSelectInfo getLoadout() {
        return new CharSelectInfo(
            NAME,
            DESCRIPTION,
            STARTING_HP,
            STARTING_HP,
            0,
            STARTING_GOLD,
            HAND_SIZE,
            this,
            this.getStartingRelics(),
            this.getStartingDeck(),
            false
        );
    }

    @Override
    public String getTitle(PlayerClass playerClass) {
        return NAME;
    }

    @Override
    public AbstractCard.CardColor getCardColor() {
        return RoxyEnums.ROXY_COLOR;
    }

    @Override
    public Color getCardRenderColor() {
        return CARD_COLOR;
    }

    @Override
    public AbstractCard getStartCardForEvent() {
        return new RoxyWaterBullet();
    }

    @Override
    public Color getCardTrailColor() {
        return CARD_COLOR.cpy();
    }

    @Override
    public int getAscensionMaxHPLoss() {
        return 5;
    }

    @Override
    public BitmapFont getEnergyNumFont() {
        return FontHelper.energyNumFontBlue;
    }

    @Override
    public TextureAtlas.AtlasRegion getOrb() {
        if (textOrbRegion == null) {
            Texture texture = ImageMaster.loadImage(TEXT_ORB_IMG);
            if (texture == null) {
                return AbstractCard.orb_blue;
            }
            textOrbRegion = new TextureAtlas.AtlasRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
        }
        return textOrbRegion;
    }

    @Override
    public void doCharSelectScreenSelectEffect() {
        CardCrawlGame.sound.playA("ATTACK_HEAVY", 0.0f);
        CardCrawlGame.screenShake.shake(
            ScreenShake.ShakeIntensity.LOW,
            ScreenShake.ShakeDur.SHORT,
            false
        );
    }

    @Override
    public String getCustomModeCharacterButtonSoundKey() {
        return "ATTACK_HEAVY";
    }

    @Override
    public String getLocalizedCharacterName() {
        return NAME;
    }

    @Override
    public void render(SpriteBatch sb) {
        boolean campfireRoom = this.isCampfireRoom();
        if (campfireRoom) {
            this.renderCampfireImage(sb);
            return;
        }
        this.wasInCampfireRoom = false;
        this.campfireSlideTimer = 0.0f;
        super.render(sb);
    }

    @Override
    public void renderPlayerImage(SpriteBatch sb) {
        if (this.isCampfireRoom()) {
            this.renderCampfireImage(sb);
            return;
        }
        super.renderPlayerImage(sb);
    }

    private boolean isCampfireRoom() {
        try {
            return AbstractDungeon.getCurrRoom() instanceof RestRoom;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void renderCampfireImage(SpriteBatch sb) {
        if (campfireTexture == null) {
            campfireTexture = ImageMaster.loadImage(CAMPFIRE_IMG);
        }

        if (!this.wasInCampfireRoom) {
            this.campfireSlideTimer = 0.0f;
            this.wasInCampfireRoom = true;
        } else {
            this.campfireSlideTimer = Math.min(
                CAMPFIRE_SLIDE_DURATION,
                this.campfireSlideTimer + Gdx.graphics.getDeltaTime()
            );
        }

        float progress = this.campfireSlideTimer / CAMPFIRE_SLIDE_DURATION;
        progress = 1.0f - (1.0f - progress) * (1.0f - progress) * (1.0f - progress);

        float height = CAMPFIRE_IMG_HEIGHT * Settings.scale;
        float width = height * campfireTexture.getWidth() / campfireTexture.getHeight();
        float x = (CAMPFIRE_TARGET_X + CAMPFIRE_SLIDE_START_OFFSET * (1.0f - progress)) * Settings.scale;
        float y = CAMPFIRE_TARGET_Y * Settings.scale;

        sb.setColor(Color.WHITE);
        sb.draw(campfireTexture, x, y, width, height);
    }

    @Override
    public AbstractPlayer newInstance() {
        return new Roxy(this.name);
    }

    @Override
    public String getSpireHeartText() {
        return "NL 洛琪希以水魔法回应了心脏的悸动。";
    }

    @Override
    public Color getSlashAttackColor() {
        return CARD_COLOR.cpy();
    }

    @Override
    public AbstractGameAction.AttackEffect[] getSpireHeartSlashEffect() {
        return new AbstractGameAction.AttackEffect[] {
            AbstractGameAction.AttackEffect.SLASH_DIAGONAL,
            AbstractGameAction.AttackEffect.LIGHTNING,
            AbstractGameAction.AttackEffect.SLASH_VERTICAL
        };
    }

    @Override
    public String getVampireText() {
        return "Navigating an unlit street, you come across several hooded figures in the midst of some dark ritual.";
    }
}


