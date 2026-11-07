package startermod.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import java.util.ArrayList;

public class SetRandomHandCardCostForTurnAction extends AbstractGameAction {
    @Override
    public void update() {
        ArrayList<AbstractCard> cards = new ArrayList<>();
        for (AbstractCard card : AbstractDungeon.player.hand.group) {
            if (card.costForTurn > 0 && card.cost >= 0) {
                cards.add(card);
            }
        }

        if (!cards.isEmpty()) {
            AbstractCard card = cards.get(AbstractDungeon.cardRandomRng.random(cards.size() - 1));
            card.setCostForTurn(0);
            card.applyPowers();
        }

        this.isDone = true;
    }
}
