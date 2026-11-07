package startermod.util;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public final class ReviewHelper {
    private ReviewHelper() {
    }

    public static boolean isReview(AbstractCard card) {
        if (card == null || AbstractDungeon.actionManager == null) {
            return false;
        }

        int matchingCards = 0;
        for (AbstractCard playedCard : AbstractDungeon.actionManager.cardsPlayedThisCombat) {
            if (card.cardID.equals(playedCard.cardID)) {
                matchingCards++;
                // The game records the current card before calling card.use().
                if (matchingCards >= 2) {
                    return true;
                }
            }
        }
        return false;
    }
}
