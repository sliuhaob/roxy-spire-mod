package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import roxymod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.util.ChantHelper;

public class RoxyEndlessBlue extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyEndlessBlue");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 3;
    private static final int DAMAGE = 10;
    private static final int HITS = 3;
    private static final int CHANT_PER_EXTRA_HIT = 3;
    private static final int UPGRADE_DAMAGE = 3;

    public RoxyEndlessBlue() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_endless_blue.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK, RoxyEnums.ROXY_COLOR, CardRarity.RARE, CardTarget.ENEMY);
        this.baseDamage = DAMAGE;
        this.baseMagicNumber = HITS;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        int chant = ChantHelper.amount(player);
        if (chant > 0) {
            this.addToBot(new ConsumeChantAction(player, chant));
        }

        int hits = this.magicNumber + chant / CHANT_PER_EXTRA_HIT;
        for (int i = 0; i < hits; i++) {
            this.addToBot(new DamageAction(monster, new DamageInfo(player, this.damage, this.damageTypeForTurn),
                AbstractGameAction.AttackEffect.SLASH_HEAVY));
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
    public RoxyEndlessBlue makeCopy() {
        return new RoxyEndlessBlue();
    }
}
