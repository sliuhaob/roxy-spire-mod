package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.ChantPower;
import startermod.powers.ZeroChantDelayPower;

public class RoxyZeroChant extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyZeroChant");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 0;
    private static final int CHANT = 5;
    private static final int UPGRADE_CHANT = 2;

    public RoxyZeroChant() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_zero_chant.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.RARE, CardTarget.SELF);
        this.baseMagicNumber = CHANT;
        this.magicNumber = this.baseMagicNumber;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new ChantPower(player, this.magicNumber), this.magicNumber));
        this.addToBot(new ApplyPowerAction(player, player, new ZeroChantDelayPower(player)));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_CHANT);
        }
    }

    @Override
    public RoxyZeroChant makeCopy() {
        return new RoxyZeroChant();
    }
}
