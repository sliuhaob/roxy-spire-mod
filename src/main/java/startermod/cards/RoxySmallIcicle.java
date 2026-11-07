package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.actions.ApplyFreezeAction;

public class RoxySmallIcicle extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxySmallIcicle");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DAMAGE = 6;
    private static final int FREEZE = 1;
    private static final int UPGRADE_DAMAGE = 3;

    public RoxySmallIcicle() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_small_icicle.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK,
            RoxyEnums.ROXY_COLOR,
            CardRarity.COMMON,
            CardTarget.ENEMY
        );

        this.baseDamage = DAMAGE;
        this.baseMagicNumber = FREEZE;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DamageAction(
            monster,
            new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.SLASH_DIAGONAL
        ));
        this.addToBot(new ApplyFreezeAction(monster, player, this.magicNumber));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxySmallIcicle makeCopy() {
        return new RoxySmallIcicle();
    }
}
