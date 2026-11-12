import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.*;
import com.megacrit.cardcrawl.characters.Ironclad;
import com.megacrit.cardcrawl.core.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.monsters.*;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.EmptyRoom;
import com.megacrit.cardcrawl.stances.NeutralStance;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import sun.misc.Unsafe;
import roxymod.actions.*;
import roxymod.cards.RoxyLightning;
import roxymod.powers.*;
import roxymod.relics.RoxyChantConsumedRelic;
import roxymod.util.ReviewHelper;

/** Uses the installed game engine. Skips graphical constructors with Unsafe. */
public class MechanicsTest {
    private static Unsafe unsafe;
    private static Ironclad player;
    private static int checks;

    static class HeadlessTexture extends com.badlogic.gdx.graphics.Texture {
        HeadlessTexture() { super(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888); }
        public int getWidth() { return 1; }
        public int getHeight() { return 1; }
    }

    public static class Monster extends AbstractMonster {
        public Integer freezeCounter;
        Monster() { super("test", "test", 100, 0, 0, 1, 1, null); }
        public void takeTurn() { }
        protected void getMove(int roll) { }
    }
    static class CounterRelic extends AbstractRelic implements RoxyChantConsumedRelic {
        int calls, consumed;
        CounterRelic() { super("test", "test.png", RelicTier.COMMON, LandingSound.FLAT); }
        public void onChantConsumed(int n) { calls++; consumed += n; }
        public String getUpdatedDescription() { return "test"; }
        public AbstractRelic makeCopy() { return this; }
    }
    static class QuietWet extends WetPower {
        QuietWet(AbstractCreature c, int n) { super(c, n); }
        public void flash() { }
    }
    static class QuietDomain extends CrystalDomainPower {
        QuietDomain(AbstractCreature c, int n) { super(c, n); }
        public void flash() { }
    }
    static class QuietZero extends ZeroChantDelayPower {
        QuietZero(AbstractCreature c) { super(c); }
        public void flash() { }
    }
    static class QuietCounter extends WaveCounterPower {
        QuietCounter(AbstractCreature c, int n) { super(c, n); }
        public void flash() { }
    }
    static class QuietFreeze extends FreezePower {
        QuietFreeze(AbstractCreature c, int n) { super(c, n); }
        public void flash() { }
    }
    static <T> T bare(Class<T> type) throws Exception { return type.cast(unsafe.allocateInstance(type)); }
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
    static void reset() {
        AbstractDungeon.actionManager = new GameActionManager();
        player.powers.clear();
        player.relics.clear();
    }
    static long queued(Class<?> type) {
        return AbstractDungeon.actionManager.actions.stream().filter(type::isInstance).count();
    }
    static Object field(Object obj, String name) throws Exception {
        Class<?> type = obj.getClass();
        while (type != null) {
            try { Field f = type.getDeclaredField(name); f.setAccessible(true); return f.get(obj); }
            catch (NoSuchFieldException ignored) { type = type.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
    }
    static Monster monster() throws Exception {
        Monster m = bare(Monster.class);
        m.powers = new ArrayList<>(); m.currentHealth = m.maxHealth = 100;
        m.freezeCounter = 3;
        m.hb = new com.megacrit.cardcrawl.helpers.Hitbox(1, 1);
        return m;
    }
    static void initialize() throws Exception {
        com.badlogic.gdx.Graphics.DisplayMode display = new com.badlogic.gdx.Graphics.DisplayMode(1920, 1080, 60, 32) { };
        com.badlogic.gdx.Gdx.graphics = (com.badlogic.gdx.Graphics)Proxy.newProxyInstance(
            MechanicsTest.class.getClassLoader(), new Class<?>[]{com.badlogic.gdx.Graphics.class},
            (proxy, method, args) -> {
                if (method.getReturnType() == com.badlogic.gdx.Graphics.DisplayMode.class) return display;
                if (method.getReturnType() == com.badlogic.gdx.Graphics.DisplayMode[].class) return new com.badlogic.gdx.Graphics.DisplayMode[]{display};
                if (method.getReturnType() == boolean.class) return false;
                if (method.getReturnType() == int.class) return 1080;
                if (method.getReturnType() == float.class) return 1f / 60;
                if (method.getReturnType() == long.class) return 0L;
                return null;
            });
        com.badlogic.gdx.Gdx.app = (com.badlogic.gdx.Application)Proxy.newProxyInstance(
            MechanicsTest.class.getClassLoader(), new Class<?>[]{com.badlogic.gdx.Application.class},
            (proxy, method, args) -> {
                if (method.getReturnType() == com.badlogic.gdx.Application.ApplicationType.class) return com.badlogic.gdx.Application.ApplicationType.Desktop;
                if (method.getReturnType() == int.class) return 0;
                if (method.getReturnType() == long.class) return 0L;
                return null;
            });
        // Route file probes away from real game preferences and saves.
        com.badlogic.gdx.Gdx.files = (com.badlogic.gdx.Files)Proxy.newProxyInstance(
            MechanicsTest.class.getClassLoader(), new Class<?>[]{com.badlogic.gdx.Files.class},
            (proxy, method, args) -> {
                if (method.getReturnType() == com.badlogic.gdx.files.FileHandle.class)
                    return new com.badlogic.gdx.files.FileHandle("build/headless-state/" + args[0]);
                if (method.getReturnType() == boolean.class) return false;
                return "build/headless-state/";
            });
        Field f = Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        unsafe = (Unsafe)f.get(null);
        Gson gson = new Gson();
        for (Field map : LocalizedStrings.class.getDeclaredFields()) {
            if (Map.class.isAssignableFrom(map.getType())) {
                String name = map.getName().replace("scoreBonuses", "score_bonuses").replace("mod", "run_mods");
                InputStream stream = MechanicsTest.class.getResourceAsStream("/localization/eng/" + name + ".json");
                map.setAccessible(true);
                map.set(null, stream == null ? new HashMap<>() : gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), map.getGenericType()));
            }
        }
        Field cards = LocalizedStrings.class.getDeclaredField("cards"); cards.setAccessible(true);
        Map<String, CardStrings> custom = gson.fromJson(new InputStreamReader(new FileInputStream(
            new File(System.getProperty("roxy.projectDir", "."), "src/main/resources/roxymod/localization/zhs/CardStrings.json")), StandardCharsets.UTF_8), cards.getGenericType());
        ((Map<String, CardStrings>)cards.get(null)).putAll(custom);
        CardCrawlGame.languagePack = bare(LocalizedStrings.class);
        HeadlessTexture texture = bare(HeadlessTexture.class);
        for (Field imageField : com.megacrit.cardcrawl.helpers.ImageMaster.class.getDeclaredFields()) {
            if (Modifier.isStatic(imageField.getModifiers()) && imageField.getType() == com.badlogic.gdx.graphics.Texture.class) {
                imageField.setAccessible(true); imageField.set(null, texture);
            }
        }
        AbstractPower.atlas = new TextureAtlas();
        com.badlogic.gdx.graphics.g2d.BitmapFont font = new com.badlogic.gdx.graphics.g2d.BitmapFont(
            new com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData(),
            new com.badlogic.gdx.graphics.g2d.TextureRegion(), false);
        for (Field fontField : com.megacrit.cardcrawl.helpers.FontHelper.class.getDeclaredFields()) {
            if (Modifier.isStatic(fontField.getModifiers()) && fontField.getType() == font.getClass()) {
                fontField.setAccessible(true); fontField.set(null, font);
            }
        }
        Settings.scale = 1;
        Settings.gamePref = new com.megacrit.cardcrawl.helpers.Prefs();
        Settings.soundPref = new com.megacrit.cardcrawl.helpers.Prefs();
        for (Field pref : com.megacrit.cardcrawl.unlock.UnlockTracker.class.getDeclaredFields()) {
            if (Modifier.isStatic(pref.getModifiers()) && pref.getType() == com.megacrit.cardcrawl.helpers.Prefs.class) {
                pref.setAccessible(true); pref.set(null, new com.megacrit.cardcrawl.helpers.Prefs());
            }
        }
        player = bare(Ironclad.class);
        player.powers = new ArrayList<>(); player.relics = new ArrayList<>();
        player.blights = new ArrayList<>(); player.orbs = new ArrayList<>();
        player.hand = new CardGroup(CardGroup.CardGroupType.HAND);
        player.stance = bare(NeutralStance.class);
        player.currentHealth = player.maxHealth = 70;
        AbstractDungeon.player = player;
        MapRoomNode node = bare(MapRoomNode.class);
        EmptyRoom room = bare(EmptyRoom.class);
        room.phase = com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase.COMBAT;
        room.monsters = new MonsterGroup(new AbstractMonster[0]);
        node.room = room;
        AbstractDungeon.currMapNode = node;
        roxymod.patches.FreezeMonsterTurnPatch.FreezeCounterField.counter.initialize(Monster.class, "freezeCounter");
        reset();
    }
    static void payments() throws Exception {
        for (int available = 0; available <= 6; available++) {
            for (int requested = 0; requested <= 8; requested++) {
                reset();
                ChantPower chant = new ChantPower(player, available);
                player.powers.add(chant);
                CounterRelic relic = bare(CounterRelic.class); player.relics.add(relic);
                new ConsumeChantAction(player, requested).update();
                int paid = Math.min(available, requested);
                check(chant.amount == available - paid, "remaining chant");
                check(relic.calls == (paid > 0 ? 1 : 0) && relic.consumed == paid, "one payment event, including exact/overpayment");
                check(queued(RemoveSpecificPowerAction.class) == (paid > 0 && paid == available ? 1 : 0), "remove exhausted chant only: available=" + available + ", requested=" + requested + ", queued=" + AbstractDungeon.actionManager.actions);
            }
        }
        reset();
        CounterRelic relic = bare(CounterRelic.class); player.relics.add(relic);
        player.powers.add(new ChantPower(player, 4));
        new QuietZero(player).atStartOfTurn();
        check(relic.calls == 0 && queued(RemoveSpecificPowerAction.class) == 2, "loss is not consumption");
    }
    static void reactions() throws Exception {
        for (int wet = 0; wet <= 3; wet++) {
            for (boolean artifact : new boolean[]{false, true}) {
                reset(); Monster m = monster();
                if (wet > 0) m.powers.add(new QuietWet(m, wet));
                if (artifact) m.powers.add(new ArtifactPower(m, 1));
                new ApplyFreezeAction(m, player, 2).update();
                ApplyPowerAction applied = (ApplyPowerAction)AbstractDungeon.actionManager.actions.stream()
                    .filter(ApplyPowerAction.class::isInstance).findFirst().get();
                AbstractPower freeze = (AbstractPower)field(applied, "powerToApply");
                boolean reacts = wet > 0 && !artifact;
                check(freeze.amount == (reacts ? 3 : 2), "Wet adds precisely one freeze");
                check(queued(ReducePowerAction.class) == (reacts ? 1 : 0), "Artifact preserves Wet");
            }
        }
        reset(); Monster dead = monster(); dead.isDead = true; dead.isDying = true;
        new ApplyFreezeAction(dead, player, 2).update();
        check(AbstractDungeon.actionManager.actions.isEmpty(), "ignore dead freeze target");
        reset(); Monster first = monster(), second = monster();
        QuietFreeze freeze = new QuietFreeze(first, 3);
        check(freeze.shouldSkipTurn(), "three freeze reaches first threshold");
        freeze.onSkipTurn();
        check(FreezePower.getCounter(first) == 4 && FreezePower.getCounter(second) == 3, "freeze thresholds are per monster");
        check(!freeze.shouldSkipTurn(), "three stacks no longer reach second threshold");
        check(queued(ReducePowerAction.class) == 1 && queued(RollMoveAction.class) == 1, "skip consumes freeze and rerolls intent");
    }
    static void highRankSpells() throws Exception {
        for (int available = 0; available <= 4; available++) {
            for (int required = 0; required <= 4; required++) {
                reset(); player.powers.add(new ChantPower(player, available));
                roxymod.util.HighRankSpellHelper.resolve(player, required);
                int missing = Math.max(0, required - available);
                int paid = Math.min(available, required);
                check(queued(ConsumeChantAction.class) == (paid > 0 ? 1 : 0), "high rank pays available chant only");
                check(queued(ApplyPowerAction.class) == (missing > 0 ? 1 : 0), "exhaustion only for shortage");
                if (missing > 0) {
                    AbstractGameAction last = AbstractDungeon.actionManager.actions.get(AbstractDungeon.actionManager.actions.size()-1);
                    check(last.amount == Math.min(2, missing), "exhaustion capped at two");
                }
            }
        }
    }
    static void powers() throws Exception {
        reset(); QuietDomain domain = new QuietDomain(player, 2);
        domain.onChantConsumed(0); domain.onChantConsumed(1); domain.onChantConsumed(3);
        check(queued(DrawCardAction.class) == 1, "domain draws once per turn");
        domain.atStartOfTurn(); domain.onChantConsumed(2);
        check(queued(DrawCardAction.class) == 2, "domain resets next turn");
        domain.onChantGained(3);
        check(AbstractDungeon.actionManager.actions.get(2).amount == 6, "stacked domain block matches description");
        reset(); EchoLessonPower echo = new EchoLessonPower(player, 3);
        echo.atEndOfTurn(false);
        check(queued(RemoveSpecificPowerAction.class) == 0, "echo survives nonplayer hook");
        echo.atEndOfTurn(true);
        check(queued(RemoveSpecificPowerAction.class) == 1, "all unused echo charges expire");
        reset(); Monster attacker = monster(); QuietCounter counter = new QuietCounter(player, 6);
        counter.onAttacked(new DamageInfo(attacker, 5, DamageInfo.DamageType.THORNS), 5);
        check(queued(DamageAction.class) == 0, "counter ignores thorns");
        counter.onAttacked(new DamageInfo(attacker, 5, DamageInfo.DamageType.NORMAL), 0);
        check(queued(DamageAction.class) == 0, "counter ignores fully blocked hit");
        counter.onAttacked(new DamageInfo(attacker, 5, DamageInfo.DamageType.NORMAL), 5);
        counter.onAttacked(new DamageInfo(attacker, 5, DamageInfo.DamageType.NORMAL), 5);
        check(queued(DamageAction.class) == 1, "counter fires once even before removal resolves");
    }
    static void lightning() throws Exception {
        for (int base : new int[]{24, 32}) {
          for (int wet = 0; wet <= 3; wet++) {
            for (int status = 0; status < 5; status++) {
                reset(); Monster m = monster();
                if (wet > 0) m.powers.add(new QuietWet(m, wet));
                if (status == 1 || status == 4) player.powers.add(new WeakPower(player, 1, false));
                if (status == 2 || status == 4) m.powers.add(new VulnerablePower(m, 1, false));
                if (status == 3) m.powers.add(new IntangiblePower(m, 1));
                RoxyLightning card = bare(RoxyLightning.class);
                card.baseDamage = base; card.damageTypeForTurn = DamageInfo.DamageType.NORMAL;
                card.type = AbstractCard.CardType.ATTACK; card.tags = new ArrayList<>();
                card.calculateCardDamage(m);
                int expected = status == 3 ? 1 : (int)Math.floor((base + 5 * wet)
                    * (status == 1 || status == 4 ? 0.75 : 1.0) * (status == 2 || status == 4 ? 1.5 : 1.0));
                check(card.damage == expected && card.baseDamage == base, "Wet participates in Weak/Vulnerable/Intangible calculation");
                new LightningStrikeAction(m, player, card).update();
                DamageAction damage = (DamageAction)AbstractDungeon.actionManager.actions.stream()
                    .filter(DamageAction.class::isInstance).findFirst().get();
                DamageInfo info = (DamageInfo)field(damage, "info");
                check(info.output == expected, "preview matches queued damage");
                check(queued(RemoveSpecificPowerAction.class) == (wet > 0 ? 1 : 0), "lightning consumes Wet once");
            }
          }
        }
    }
    static void review() throws Exception {
        reset(); RoxyLightning first = bare(RoxyLightning.class), second = bare(RoxyLightning.class);
        first.cardID = second.cardID = "roxymod:RoxyLightning";
        check(!ReviewHelper.isReview(first), "no history before first play");
        AbstractDungeon.actionManager.cardsPlayedThisCombat.add(first);
        check(!ReviewHelper.isReview(first), "first recorded play does not review");
        AbstractDungeon.actionManager.cardsPlayedThisCombat.add(second);
        check(ReviewHelper.isReview(second), "second same-ID copy reviews");
        reset(); check(!ReviewHelper.isReview(second), "review resets between combats");
    }
    public static void main(String[] args) throws Exception {
        initialize(); payments(); reactions(); highRankSpells(); powers(); lightning(); review();
        System.out.println("PASS: " + checks + " engine-backed mechanics assertions (GPU rendering excluded).");
    }
}
