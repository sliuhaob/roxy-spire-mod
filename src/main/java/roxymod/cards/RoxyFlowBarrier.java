package roxymod.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import roxymod.RoxyEnums;
import roxymod.RoxyMod;
import roxymod.util.ChantHelper;

public class RoxyFlowBarrier extends CustomCard {
    public static final String ID = RoxyMod.makeID("RoxyFlowBarrier");

    private static final CardStrings CARD_STRINGS = CardCrawlGame.languagePack.getCardStrings(ID);
    private static final int COST = 1;
    private static final int BLOCK = 7;
    private static final int UPGRADE_BLOCK = 3;

    public RoxyFlowBarrier() {
        super(ID, CARD_STRINGS.NAME, RoxyMod.assetPath("images/cards/roxy_flow_barrier.png"), COST, CARD_STRINGS.DESCRIPTION,
            CardType.SKILL, RoxyEnums.ROXY_COLOR, CardRarity.COMMON, CardTarget.SELF);
        this.baseBlock = BLOCK;
    }

    @Override
    public void use(AbstractPlayer player, AbstractMonster monster) {
        this.addToBot(new GainBlockAction(player, player, this.block));
        if (ChantHelper.amount(player) > 0) {
            this.addToBot(new DrawCardAction(player, 1));
        }
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            this.upgradeName();
            this.upgradeBlock(UPGRADE_BLOCK);
        }
    }

    @Override
    public RoxyFlowBarrier makeCopy() {
        return new RoxyFlowBarrier();
    }
}
