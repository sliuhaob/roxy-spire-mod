package roxymod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import roxymod.RoxyMod;

public class RoxyHydraulicSeal extends CustomRelic implements RoxyChantConsumedRelic {
    public static final String ID = RoxyMod.makeID("RoxyHydraulicSeal");

    private static final String IMG = RoxyMod.assetPath("images/relics/hydraulic_seal.png");
    private static final String OUTLINE = RoxyMod.assetPath("images/relics/hydraulic_seal_outline.png");
    private static final String LARGE_IMG = RoxyMod.assetPath("images/relics/hydraulic_seal_large.png");

    private boolean triggeredThisTurn;

    public RoxyHydraulicSeal() {
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
    public void onChantConsumed(int amount) {
        if (amount <= 0 || this.triggeredThisTurn) {
            return;
        }

        this.triggeredThisTurn = true;
        this.stopPulse();
        this.flash();
        this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        this.addToBot(new DrawCardAction(AbstractDungeon.player, 1));
    }

    @Override
    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new RoxyHydraulicSeal();
    }
}
