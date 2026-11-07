package startermod.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.powers.ChantPower;

/** Explicit payment: losing chant at turn start is deliberately not a payment. */
public class ConsumeChantAction extends AbstractGameAction {
    public ConsumeChantAction(AbstractCreature owner, int amount) {
        this.target = owner;
        this.amount = Math.max(0, amount);
        this.actionType = ActionType.REDUCE_POWER;
    }

    @Override
    public void update() {
        AbstractPower power = this.target == null ? null : this.target.getPower(ChantPower.POWER_ID);
        if (power != null && power.amount > 0 && this.amount > 0) {
            // ReducePowerAction bypasses reducePower when removing the final stack.
            // Always call the hook once, including exact and excess payments.
            power.reducePower(Math.min(power.amount, this.amount));
            power.updateDescription();
            if (power.amount <= 0) {
                this.addToTop(new RemoveSpecificPowerAction(this.target, this.target, power));
            } else {
                AbstractDungeon.onModifyPower();
            }
        }
        this.isDone = true;
    }
}
