package startermod.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import startermod.StarterMod;
import startermod.util.PowerIconHelper;

public class CrystalStaffPower extends AbstractPower {
    public static final String POWER_ID = StarterMod.makeID("CrystalStaff");

    private static final String NAME = "结晶法杖";

    public CrystalStaffPower(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;

        this.region128 = PowerIconHelper.loadLarge("crystal_staff");
        this.region48 = PowerIconHelper.loadSmall("crystal_staff");
        this.updateDescription();
    }

    @Override
    public void updateDescription() {
        this.description = "每获得 1 层咏唱，获得 #b" + this.amount + " 点格挡。";
    }
}
