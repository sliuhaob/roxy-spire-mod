package startermod.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import java.util.ArrayList;
import basemod.BaseMod;

public class MoveRandomAttackFromDrawPileToHandAction extends AbstractGameAction {
    @Override
    public void update() {
        if (AbstractDungeon.player.hand.size() >= BaseMod.MAX_HAND_SIZE) {
            AbstractDungeon.player.createHandIsFullDialog();
            this.isDone = true;
            return;
        }
        ArrayList<AbstractCard> attacks = new ArrayList<>();
        for (AbstractCard card : AbstractDungeon.player.drawPile.group) {
            if (card.type == AbstractCard.CardType.ATTACK) {
                attacks.add(card);
            }
        }

        if (!attacks.isEmpty()) {
            AbstractCard card = attacks.get(AbstractDungeon.cardRandomRng.random(attacks.size() - 1));
            AbstractDungeon.player.drawPile.moveToHand(card, AbstractDungeon.player.drawPile);
            AbstractDungeon.player.hand.refreshHandLayout();
        }

        this.isDone = true;
    }
}
