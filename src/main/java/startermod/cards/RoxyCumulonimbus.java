package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.powers.CumulonimbusPower;
import startermod.util.ContinuationHelper;

public class RoxyCumulonimbus extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyCumulonimbus");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DAMAGE = 6;
    private static final int UPGRADE_DAMAGE = 3;

    public RoxyCumulonimbus() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_cumulonimbus.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.POWER,
            RoxyEnums.ROXY_COLOR,
            CardRarity.RARE,
            CardTarget.SELF
        );

        this.baseMagicNumber = DAMAGE;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        ContinuationHelper.grant(player);
        this.addToBot(new ApplyPowerAction(
            player,
            player,
            new CumulonimbusPower(player, this.magicNumber),
            this.magicNumber
        ));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeMagicNumber(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxyCumulonimbus makeCopy() {
        return new RoxyCumulonimbus();
    }
}
