package roxymod.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import java.util.ArrayList;

public class UpgradeRandomHandCardAction extends AbstractGameAction {
    private final int amount;

    public UpgradeRandomHandCardAction() {
        this(1);
    }

    public UpgradeRandomHandCardAction(int amount) {
        this.amount = Math.max(0, amount);
    }

    @Override
    public void update() {
        ArrayList<AbstractCard> cards = new ArrayList<>();
        for (AbstractCard card : AbstractDungeon.player.hand.group) {
            if (card.canUpgrade()) {
                cards.add(card);
            }
        }

        int upgradedCards = 0;
        while (!cards.isEmpty() && upgradedCards < this.amount) {
            AbstractCard card = cards.get(AbstractDungeon.cardRandomRng.random(cards.size() - 1));
            cards.remove(card);
            card.upgrade();
            card.applyPowers();
            upgradedCards++;
        }

        if (upgradedCards > 0) {
            AbstractDungeon.player.hand.refreshHandLayout();
        }

        this.isDone = true;
    }
}
