package startermod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import startermod.StarterMod;
import startermod.powers.ChantPower;

public class RoxyManaCrystalCore extends CustomRelic implements RoxyContinuationConsumedRelic {
    public static final String ID = StarterMod.makeID("RoxyManaCrystalCore");

    private static final String IMG = StarterMod.assetPath("images/relics/mana_crystal_core.png");
    private static final String OUTLINE = StarterMod.assetPath("images/relics/mana_crystal_core_outline.png");
    private static final String LARGE_IMG = StarterMod.assetPath("images/relics/mana_crystal_core_large.png");
    private static final int ENERGY = 1;
    private static final int CHANT = 1;

    private boolean triggeredThisTurn;

    public RoxyManaCrystalCore() {
        super(
            ID,
            ImageMaster.loadImage(IMG),
            ImageMaster.loadImage(OUTLINE),
            RelicTier.BOSS,
            LandingSound.MAGICAL
        );
        this.largeImg = ImageMaster.loadImage(LARGE_IMG);
    }

    @Override
    public boolean canSpawn() {
        return AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(RoxyManaCrystal.ID);
    }

    @Override
    public void obtain() {
        if (AbstractDungeon.player != null && AbstractDungeon.player.hasRelic(RoxyManaCrystal.ID)) {
            int index = AbstractDungeon.player.relics.indexOf(AbstractDungeon.player.getRelic(RoxyManaCrystal.ID));
            AbstractDungeon.player.getRelic(RoxyManaCrystal.ID).onUnequip();
            AbstractDungeon.player.relics.remove(index);
            this.instantObtain(AbstractDungeon.player, index, true);
        } else {
            super.obtain();
        }
    }

    @Override
    public void atBattleStart() {
        this.triggeredThisTurn = false;
    }

    @Override
    public void atTurnStart() {
        this.triggeredThisTurn = false;
        this.beginLongPulse();
    }

    @Override
    public void onPlayerEndTurn() {
        this.stopPulse();
    }

    @Override
    public void onContinuationConsumed() {
        if (this.triggeredThisTurn) {
            return;
        }

        this.triggeredThisTurn = true;
        this.stopPulse();
        this.flash();
        this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        this.addToBot(new GainEnergyAction(ENERGY));
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
        return new RoxyManaCrystalCore();
    }
}
