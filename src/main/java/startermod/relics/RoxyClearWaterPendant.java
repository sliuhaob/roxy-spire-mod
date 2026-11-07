package startermod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import startermod.StarterMod;
import startermod.powers.ChantPower;

public class RoxyClearWaterPendant extends CustomRelic {
    public static final String ID = StarterMod.makeID("RoxyClearWaterPendant");

    private static final String IMG = StarterMod.assetPath("images/relics/clear_water_pendant.png");
    private static final String OUTLINE = StarterMod.assetPath("images/relics/clear_water_pendant_outline.png");
    private static final String LARGE_IMG = StarterMod.assetPath("images/relics/clear_water_pendant_large.png");
    private static final int ARTIFACT = 1;
    private static final int CHANT = 1;

    public RoxyClearWaterPendant() {
        super(
            ID,
            ImageMaster.loadImage(IMG),
            ImageMaster.loadImage(OUTLINE),
            RelicTier.RARE,
            LandingSound.MAGICAL
        );
        this.largeImg = ImageMaster.loadImage(LARGE_IMG);
    }

    @Override
    public void atBattleStart() {
        this.flash();
        this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        this.addToBot(new ApplyPowerAction(
            AbstractDungeon.player,
            AbstractDungeon.player,
            new ArtifactPower(AbstractDungeon.player, ARTIFACT),
            ARTIFACT
        ));
        this.addToBot(new ApplyPowerAction(
            AbstractDungeon.player,
            AbstractDungeon.player,
            new ChantPower(AbstractDungeon.player, CHANT),
            CHANT
        ));
    }

    @Override
    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new RoxyClearWaterPendant();
    }
}
