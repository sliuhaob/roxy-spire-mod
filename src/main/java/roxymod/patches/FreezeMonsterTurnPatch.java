package roxymod.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.ShowMoveNameAction;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import roxymod.powers.FreezePower;

@SpirePatch(clz = GameActionManager.class, method = "getNextAction")
public class FreezeMonsterTurnPatch {
    private static final String MOVE_NAME_SIGNATURE = "(Lcom/megacrit/cardcrawl/monsters/AbstractMonster;)V";

    @SpireInstrumentPatch
    public static ExprEditor instrument() {
        return new ExprEditor() {
            @Override
            public void edit(MethodCall call) throws CannotCompileException {
                if (call.getClassName().equals(AbstractMonster.class.getName())
                    && call.getMethodName().equals("takeTurn")) {
                    call.replace("{ if (!roxymod.patches.FreezeMonsterTurnPatch.shouldSkipTurn($0)) { $proceed($$); } }");
                }
            }

            @Override
            public void edit(NewExpr expr) throws CannotCompileException {
                if (expr.getClassName().equals(ShowMoveNameAction.class.getName())
                    && expr.getSignature().equals(MOVE_NAME_SIGNATURE)) {
                    expr.replace("{ $_ = roxymod.patches.FreezeMonsterTurnPatch.moveNameAction($1); }");
                }
            }
        };
    }

    /**
     * Runs just before the turn is skipped. Vanilla's one-argument constructor shows and
     * clears moveName, which would announce a move that does not happen and leave the
     * delayed move without its name next turn. The two-argument one leaves it alone.
     */
    public static ShowMoveNameAction moveNameAction(AbstractMonster monster) {
        if (FreezePower.isFrozen(monster)) {
            return new ShowMoveNameAction(monster, FreezePower.NAME);
        }
        return new ShowMoveNameAction(monster);
    }

    public static boolean shouldSkipTurn(AbstractMonster monster) {
        if (!FreezePower.isFrozen(monster)) {
            return false;
        }
        ((FreezePower) monster.getPower(FreezePower.POWER_ID)).onSkipTurn();
        return true;
    }

    @SpirePatch(clz = AbstractMonster.class, method = SpirePatch.CLASS)
    public static class FreezeCounterField {
        public static SpireField<Integer> counter = new SpireField<>(() -> FreezePower.STARTING_COUNTER);
    }
}
