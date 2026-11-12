package roxymod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import roxymod.RoxyMod;
import roxymod.powers.ChantPower;

public class RoxyChantPrimer extends CustomRelic {
    public static final String ID = RoxyMod.makeID("RoxyChantPrimer");

    private static final String IMG = RoxyMod.assetPath("images/relics/chant_primer.png");
    private static final String OUTLINE = RoxyMod.assetPath("images/relics/chant_primer_outline.png");
    private static final String LARGE_IMG = RoxyMod.assetPath("images/relics/chant_primer_large.png");
    private static final int CHANT = 2;

    public RoxyChantPrimer() {
        super(
            ID,
            ImageMaster.loadImage(IMG),
            ImageMaster.loadImage(OUTLINE),
            RelicTier.COMMON,
            LandingSound.FLAT
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
        return new RoxyChantPrimer();
    }
}
