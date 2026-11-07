package startermod.util;

import com.megacrit.cardcrawl.characters.AbstractPlayer;
import startermod.powers.ChantPower;

public final class ChantHelper {
    private ChantHelper() {
    }

    public static int amount(AbstractPlayer player) {
        if (player == null || !player.hasPower(ChantPower.POWER_ID)) {
            return 0;
        }
        return Math.max(0, player.getPower(ChantPower.POWER_ID).amount);
    }
}

