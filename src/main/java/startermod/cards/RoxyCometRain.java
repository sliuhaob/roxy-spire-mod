package startermod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageRandomEnemyAction;
import startermod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import startermod.RoxyEnums;
import startermod.StarterMod;
import startermod.util.ChantHelper;

public class RoxyCometRain extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyCometRain");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DAMAGE = 5;
    private static final int HITS = 5;
    private static final int MAX_CHANT_CONSUMED = 3;
    private static final int UPGRADE_DAMAGE = 1;

    public RoxyCometRain() {
        super(ID, CARD_STRINGS.NAME, StarterMod.assetPath("images/cards/roxy_comet_rain.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK, RoxyEnums.ROXY_COLOR, CardRarity.RARE, CardTarget.ALL_ENEMY);
        this.baseDamage = DAMAGE;
        this.baseMagicNumber = HITS;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        int chantConsumed = Math.min(MAX_CHANT_CONSUMED, ChantHelper.amount(player));
        if (chantConsumed > 0) {
            this.addToBot(new ConsumeChantAction(player, chantConsumed));
        }

        int hits = this.magicNumber + chantConsumed;
        for (int i = 0; i < hits; i++) {
            this.addToBot(new DamageRandomEnemyAction(new DamageInfo(player, this.damage, this.damageTypeForTurn),
                AbstractGameAction.AttackEffect.SLASH_VERTICAL));
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
    public RoxyCometRain makeCopy() {
        return new RoxyCometRain();
    }
}
