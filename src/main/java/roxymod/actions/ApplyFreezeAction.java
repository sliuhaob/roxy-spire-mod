package roxymod.actions;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import roxymod.powers.FreezePower;
import roxymod.powers.WetPower;

public class ApplyFreezeAction extends AbstractGameAction {
    private final AbstractMonster monster;
    private final AbstractCreature source;
    private final int freezeAmount;

    public ApplyFreezeAction(AbstractMonster monster, AbstractCreature source, int freezeAmount) {
        this.monster = monster;
        this.source = source;
        this.freezeAmount = Math.max(0, freezeAmount);
        this.actionType = ActionType.DEBUFF;
    }

    @Override
    public void update() {
        if (this.monster == null || this.monster.isDeadOrEscaped() || this.freezeAmount <= 0) {
            this.isDone = true;
            return;
        }

        int totalFreeze = this.freezeAmount;
        // Artifact blocks the freeze, so it must not consume the setup resource.
        if (!this.monster.hasPower(ArtifactPower.POWER_ID)
                && this.monster.hasPower(WetPower.POWER_ID)
                && this.monster.getPower(WetPower.POWER_ID).amount > 0) {
            this.monster.getPower(WetPower.POWER_ID).flash();
            totalFreeze++;
            this.addToTop(new ApplyPowerAction(
                this.monster,
                this.source,
                new FreezePower(this.monster, totalFreeze),
                totalFreeze
            ));
            this.addToTop(new ReducePowerAction(this.monster, this.source, WetPower.POWER_ID, 1));
        } else {
            this.addToTop(new ApplyPowerAction(
                this.monster,
                this.source,
                new FreezePower(this.monster, totalFreeze),
                totalFreeze
            ));
        }

        this.isDone = true;
    }
}
