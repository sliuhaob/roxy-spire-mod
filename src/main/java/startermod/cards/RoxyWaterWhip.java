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
import startermod.util.ChantHelper;

public class RoxyWaterWhip extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyWaterWhip");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int DAMAGE = 5;
    private static final int HITS = 2;
    private static final int UPGRADE_DAMAGE = 2;

    public RoxyWaterWhip() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_water_whip.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.ENEMY);
        this.baseDamage = DAMAGE;
        this.baseMagicNumber = HITS;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        int hits = this.magicNumber;
        if (ChantHelper.amount(player) > 0) {
            hits++;
        }

        for (int i = 0; i < hits; i++) {
            this.addToBot(new DamageAction(monster, new DamageInfo(player, this.damage, this.damageTypeForTurn),
                AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeDamage(UPGRADE_DAMAGE);
        }
    }

    @Override
    public RoxyWaterWhip makeCopy() {
        return new RoxyWaterWhip();
    }
}
