package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.StarterMod;
import startermod.RoxyEnums;
import startermod.util.HighRankSpellHelper;

public class RoxyGreatFlood extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyGreatFlood");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 3;
    private static final int DAMAGE = 18;
    private static final int CHANT_REQUIRED = 4;
    private static final int UPGRADE_DAMAGE = 6;

    public RoxyGreatFlood() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_great_flood.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK,
            RoxyEnums.ROXY_COLOR,
            CardRarity.RARE,
            CardTarget.ALL_ENEMY
        );

        this.baseDamage = DAMAGE;
        this.baseMagicNumber = CHANT_REQUIRED;
        this.magicNumber = this.baseMagicNumber;
        this.isMultiDamage = true;
        this.exhaust = true;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        HighRankSpellHelper.resolve(player, this.magicNumber);
        this.addToBot(new DamageAllEnemiesAction(
            player,
            this.multiDamage,
            this.damageTypeForTurn,
            AbstractGameAction.AttackEffect.BLUNT_HEAVY
        ));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxyGreatFlood makeCopy() {
        return new RoxyGreatFlood();
    }
}
