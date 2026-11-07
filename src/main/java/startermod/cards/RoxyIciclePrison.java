package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import startermod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.actions.ApplyFreezeAction;
import startermod.util.ChantHelper;

public class RoxyIciclePrison extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyIciclePrison");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DAMAGE = 7;
    private static final int FREEZE = 1;
    private static final int UPGRADE_DAMAGE = 3;

    public RoxyIciclePrison() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_icicle_prison.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = DAMAGE;
        this.baseMagicNumber = FREEZE;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DamageAction(monster, new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
        int freeze = this.magicNumber;
        if (ChantHelper.amount(player) > 0) {
            this.addToBot(new ConsumeChantAction(player, 1));
            freeze += this.magicNumber;
        }
        // One spell application consumes at most one Wet stack.
        this.addToBot(new ApplyFreezeAction(monster, player, freeze));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxyIciclePrison makeCopy() {
        return new RoxyIciclePrison();
    }
}
