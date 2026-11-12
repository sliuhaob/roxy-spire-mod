package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.actions.LightningStrikeAction;
import roxymod.cards.interfaces.FinisherCard;
import roxymod.util.ContinuationHelper;
import roxymod.util.HighRankSpellHelper;
import roxymod.powers.WetPower;

public class RoxyLightning extends CustomCard implements FinisherCard {
    public static final String ID = RoxyMod.makeID("RoxyLightning");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DAMAGE = 24;
    private static final int CHANT_REQUIRED = 3;
    private static final int DAMAGE_PER_WET = 5;
    private static final int UPGRADE_DAMAGE = 8;

    public RoxyLightning() {
        super(
            ID,
            CARD_STRINGS.NAME,
            RoxyMod.assetPath("images/cards/roxy_lightning.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK,
            RoxyEnums.ROXY_COLOR,
            CardRarity.RARE,
            CardTarget.ENEMY
        );

        this.baseDamage = DAMAGE;
        this.baseMagicNumber = CHANT_REQUIRED;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        boolean continued = ContinuationHelper.consume(player);
        int chantRequired = Math.max(0, this.magicNumber - (continued ? 1 : 0));
        HighRankSpellHelper.resolve(player, chantRequired);
        this.addToBot(new LightningStrikeAction(
            monster,
            player,
            this.makeStatEquivalentCopy()
        ));
    }

    @Override
    public void calculateCardDamage(AbstractMonster monster) {
        int originalBaseDamage = this.baseDamage;
        int wet = monster != null && monster.hasPower(WetPower.POWER_ID)
            ? Math.max(0, monster.getPower(WetPower.POWER_ID).amount) : 0;
        try {
            this.baseDamage += wet * DAMAGE_PER_WET;
            super.calculateCardDamage(monster);
        } finally {
            this.baseDamage = originalBaseDamage;
        }
        this.isDamageModified = this.damage != this.baseDamage;
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxyLightning makeCopy() {
        return new RoxyLightning();
    }
}
