package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.actions.MoveRandomAttackFromDrawPileToHandAction;

public class RoxyLabyrinthMap extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyLabyrinthMap");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;

    public RoxyLabyrinthMap() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_labyrinth_map.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.UNCOMMON, CardTarget.SELF);
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new MoveRandomAttackFromDrawPileToHandAction());
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBaseCost(0);
        }
    }

    @Override
    public RoxyLabyrinthMap makeCopy() {
        return new RoxyLabyrinthMap();
    }
}
