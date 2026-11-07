package startermod.powers;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.util.PowerIconHelper;

public class ManaWeavePower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("ManaWeave");

    private static final String NAME = "魔力编织";

    private boolean triggeredThisTurn;

    public ManaWeavePower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("mana_weave");
        this.region48 = PowerIconHelper.loadSmall("mana_weave");
        this.updateDescription();
    }

    @Override
    public void atStartOfTurn() {
        this.triggeredThisTurn = false;
    }

    public void onChantConsumed(int chantConsumed) {
        if (!this.triggeredThisTurn) {
            this.triggeredThisTurn = true;
            this.flash();
            this.addToBot(new DrawCardAction(this.owner, this.amount));
        }
    }

    @Override
    public void updateDescription() {
        this.description = "每回合第一次消耗咏唱时，抽 #b" + this.amount + " 张牌。";
    }
}
