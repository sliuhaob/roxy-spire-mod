package roxymod.util;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import roxymod.powers.ContinuationPower;
import roxymod.relics.RoxyContinuationConsumedRelic;

public final class ContinuationHelper {
    private ContinuationHelper() {
    }

    public static boolean has(AbstractPlayer player) {
        return player != null
            && player.hasPower(ContinuationPower.POWER_ID)
            && player.getPower(ContinuationPower.POWER_ID).amount > 0;
    }

    public static void grant(AbstractPlayer player) {
        if (player == null) {
            return;
        }
        DevCombatLog.continuation("grant");
        AbstractDungeon.actionManager.addToBottom(
            new ApplyPowerAction(player, player, new ContinuationPower(player), 1)
        );
    }

    public static boolean consume(AbstractPlayer player) {
        if (!has(player)) {
            return false;
        }

        AbstractPower continuation = player.getPower(ContinuationPower.POWER_ID);
        continuation.flash();
        continuation.amount = 0;
        DevCombatLog.continuation("consume");
        AbstractDungeon.actionManager.addToTop(
            new RemoveSpecificPowerAction(player, player, ContinuationPower.POWER_ID)
        );
        if (player == AbstractDungeon.player) {
            for (AbstractRelic relic : player.relics) {
                if (relic instanceof RoxyContinuationConsumedRelic) {
                    ((RoxyContinuationConsumedRelic) relic).onContinuationConsumed();
                }
            }
        }
        return true;
    }
}
