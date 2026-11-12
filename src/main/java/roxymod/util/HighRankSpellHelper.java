package roxymod.util;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import roxymod.actions.ConsumeChantAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import roxymod.powers.ExhaustionPower;

public final class HighRankSpellHelper {
    private HighRankSpellHelper() {
    }

    public static void resolve(AbstractPlayer player, int chantRequired) {
        if (player == null || chantRequired <= 0) {
            return;
        }

        int chantAvailable = ChantHelper.amount(player);
        int chantConsumed = Math.min(chantAvailable, chantRequired);
        if (chantConsumed > 0) {
            AbstractDungeon.actionManager.addToBottom(
                new ConsumeChantAction(player, chantConsumed)
            );
        }

        int chantMissing = chantRequired - chantConsumed;
        int exhaustion = Math.min(ExhaustionPower.MAX_AMOUNT, chantMissing);
        if (exhaustion > 0) {
            AbstractDungeon.actionManager.addToBottom(
                new ApplyPowerAction(player, player, new ExhaustionPower(player, exhaustion), exhaustion)
            );
        }
    }
}
