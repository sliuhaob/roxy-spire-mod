package startermod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import startermod.StarterMod;
import startermod.actions.ApplyFreezeAction;

public class RoxyWaterSigilRelic extends CustomRelic {
    public static final String ID = StarterMod.makeID("RoxyWaterSigilRelic");

    private static final String IMG = StarterMod.assetPath("images/relics/water_sigil_relic.png");
    private static final String OUTLINE = StarterMod.assetPath("images/relics/water_sigil_relic_outline.png");
    private static final String LARGE_IMG = StarterMod.assetPath("images/relics/water_sigil_relic_large.png");
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
