package roxymod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import roxymod.RoxyMod;
import roxymod.actions.ApplyFreezeAction;

public class RoxyWaterSigilRelic extends CustomRelic {
    public static final String ID = RoxyMod.makeID("RoxyWaterSigilRelic");

    private static final String IMG = RoxyMod.assetPath("images/relics/water_sigil_relic.png");
    private static final String OUTLINE = RoxyMod.assetPath("images/relics/water_sigil_relic_outline.png");
    private static final String LARGE_IMG = RoxyMod.assetPath("images/relics/water_sigil_relic_large.png");
    private static final int FREEZE = 1;

    public RoxyWaterSigilRelic() {
        super(
            ID,
            ImageMaster.loadImage(IMG),
            ImageMaster.loadImage(OUTLINE),
            RelicTier.COMMON,
            LandingSound.CLINK
        );
        this.largeImg = ImageMaster.loadImage(LARGE_IMG);
    }

    @Override
    public void atBattleStart() {
        this.flash();
        this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
            if (!monster.isDeadOrEscaped()) {
                this.addToBot(new ApplyFreezeAction(monster, AbstractDungeon.player, FREEZE));
            }
        }
    }

    @Override
    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new RoxyWaterSigilRelic();
    }
}
