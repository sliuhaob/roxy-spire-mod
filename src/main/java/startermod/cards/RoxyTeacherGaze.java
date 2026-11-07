package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.WeakPower;
import startermod.RoxyEnums;
import startermod.StarterMod;

public class RoxyTeacherGaze extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyTeacherGaze");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int WEAK = 2;

    public RoxyTeacherGaze() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_teacher_gaze.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.COMMON, CardTarget.ENEMY);
        this.baseMagicNumber = WEAK;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(monster, player, new WeakPower(monster, this.magicNumber, false), this.magicNumber));
        this.addToBot(new DrawCardAction(player, 1));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(0);
        }
    }

    @Override
    public RoxyTeacherGaze makeCopy() {
        return new RoxyTeacherGaze();
    }
}
