package startermod.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import startermod.StarterMod;
import startermod.powers.ChantPower;

public class RoxyLessonNotebook extends CustomRelic {
    public static final String ID = StarterMod.makeID("RoxyLessonNotebook");

    private static final String IMG = StarterMod.assetPath("images/relics/lesson_notebook.png");
    private static final String OUTLINE = StarterMod.assetPath("images/relics/lesson_notebook_outline.png");
    private static final String LARGE_IMG = StarterMod.assetPath("images/relics/lesson_notebook_large.png");
    private static final int DRAW = 1;
    private static final int CHANT = 1;

    private boolean triggeredThisCombat;

    public RoxyLessonNotebook() {
        super(
            ID,
            ImageMaster.loadImage(IMG),
            ImageMaster.loadImage(OUTLINE),
            RelicTier.UNCOMMON,
            LandingSound.FLAT
        );
        this.largeImg = ImageMaster.loadImage(LARGE_IMG);
    }

    @Override
    public void atBattleStart() {
        this.triggeredThisCombat = false;
        this.beginLongPulse();
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (this.triggeredThisCombat || card.type != AbstractCard.CardType.SKILL) {
            return;
        }

        this.triggeredThisCombat = true;
        this.stopPulse();
        this.flash();
        this.addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        this.addToBot(new ApplyPowerAction(
            AbstractDungeon.player,
            AbstractDungeon.player,
            new ChantPower(AbstractDungeon.player, CHANT),
            CHANT
        ));
        this.addToBot(new DrawCardAction(AbstractDungeon.player, DRAW));
    }

    @Override
    public void onVictory() {
        this.stopPulse();
    }

    @Override
    public String getUpdatedDescription() {
        return this.DESCRIPTIONS[0];
    }

    @Override
    public AbstractRelic makeCopy() {
        return new RoxyLessonNotebook();
    }
}
