package roxymod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import roxymod.RoxyMod;
import roxymod.powers.ChantPower;

public class RoxyTeachingPointer extends CustomRelic {
    public static final String ID = RoxyMod.makeID("RoxyTeachingPointer");

    private static final String IMG = RoxyMod.assetPath("images/relics/teaching_pointer.png");
    private static final String OUTLINE = RoxyMod.assetPath("images/relics/teaching_pointer_outline.png");
    private static final String LARGE_IMG = RoxyMod.assetPath("images/relics/teaching_pointer_large.png");
    private static final int REQUIRED_SKILLS = 3;
    private static final int CHANT = 1;

    public RoxyTeachingPointer() {
        super(
            ID,
            ImageMaster.loadImage(IMG),
            ImageMaster.loadImage(OUTLINE),
            RelicTier.UNCOMMON,
            LandingSound.CLINK
        );
        this.largeImg = ImageMaster.loadImage(LARGE_IMG);
    }

    @Override
    public void atBattleStart() {
        this.setCounter(0);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type != AbstractCard.CardType.SKILL) {
            return;
        }

        this.setCounter(this.counter + 1);
        if (this.counter >= REQUIRED_SKILLS) {
            this.flash();
            this.setCounter(0);
            this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
            this.addToBot(new ApplyPowerAction(
                AbstractDungeon.player,
                AbstractDungeon.player,
                new ChantPower(AbstractDungeon.player, CHANT),
                CHANT
            ));
        }
    }

    @Override
    public void onVictory() {
        this.setCounter(-1);
    }

    @Override
    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0] + REQUIRED_SKILLS + this.DESCRIPTIONS[1];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new RoxyTeachingPointer();
    }
}
