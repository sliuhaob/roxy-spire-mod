package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.SageTimePower;

public class RoxyChantlessWaterMagic extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyChantlessWaterMagic");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final String SAGE_TIME_KEYWORD = "贤者时间";
    private static final int COST = 1;
    private static final int SAGE_TIME = 2;

    public RoxyChantlessWaterMagic() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_chantless_water_magic.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.SKILL,
            RoxyEnums.ROXY_COLOR,
            CardRarity.RARE,
            CardTarget.SELF
        );

        this.baseMagicNumber = SAGE_TIME;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new ApplyPowerAction(player, player, new SageTimePower(player, this.magicNumber), this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(0);
        }
    }

    @Override
    public void initializeDescription() {
        super.initializeDescription();
        if (!this.keywords.contains(SAGE_TIME_KEYWORD)) {
            this.keywords.add(SAGE_TIME_KEYWORD);
        }
    }

    @Override
    public RoxyChantlessWaterMagic makeCopy() {
        return new RoxyChantlessWaterMagic();
    }
}
