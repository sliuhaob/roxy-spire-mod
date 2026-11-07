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
import startermod.StarterMod;
import startermod.RoxyEnums;
import startermod.util.ChantHelper;

public class RoxyTorrentImpact extends CustomCard {
    public static final String ID = StarterMod.makeID("RoxyTorrentImpact");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 2;
    private static final int DAMAGE = 14;
    private static final int EXTRA_DAMAGE = 6;
    private static final int UPGRADE_DAMAGE = 5;

    public RoxyTorrentImpact() {
        super(
            ID,
            CARD_STRINGS.NAME,
            StarterMod.assetPath("images/cards/roxy_torrent_impact.png"),
            COST,
            CARD_STRINGS.DESCRIPTION,
            CardType.ATTACK,
            RoxyEnums.ROXY_COLOR,
            CardRarity.UNCOMMON,
            CardTarget.ENEMY
        );

        this.baseDamage = DAMAGE;
        this.baseMagicNumber = EXTRA_DAMAGE;
        this.magicNumber = this.baseMagicNumber;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new DamageAction(
            monster,
            new DamageInfo(player, this.damage, this.damageTypeForTurn),
            AbstractGameAction.AttackEffect.BLUNT_HEAVY
        ));

        if (ChantHelper.amount(player) > 0) {
            this.addToBot(new ConsumeChantAction(player, 1));
            this.addToBot(new DamageAction(
                monster,
                new DamageInfo(player, this.magicNumber, this.damageTypeForTurn),
                AbstractGameAction.AttackEffect.LIGHTNING
            ));
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
    public RoxyTorrentImpact makeCopy() {
        return new RoxyTorrentImpact();
    }
}

