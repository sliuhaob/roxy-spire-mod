# 实机试玩指南

更新时间：2026-11-07。

## 安装

```powershell
.\build.ps1 -Verify -Install
```

安装到游戏 mods 目录的 RoxyMod.jar。旧的 StarterMod.jar 已移出 mods 目录，备份在 outputs/installed_backup_20261107/。从 Steam 选择“带模组启动”时，在 ModTheSpire 启动器里勾选 BaseMod 和“Roxy · 水与咏唱”。

## 带战斗日志试玩

```powershell
.\tools\play.ps1
.\tools\play.ps1 -Mods basemod,stslib,roxymod
```

需要先打开 Steam。脚本跳过 ModTheSpire 启动器，直接以指定模组启动游戏，并打开仅供开发使用的战斗日志。日志追加写入 outputs/playtest/combat-log.tsv，可用 Excel 打开。从 Steam 启动时日志保持关闭，正常游玩不受影响。

每行包含种子、楼层、遭遇、回合、事件和上一张打出的牌。按 event 筛选 combat_end，即可得到每场战斗的回合数、损失生命和各项合计。

| event | amount | total | detail |
| --- | --- | --- | --- |
| combat_start | 当前生命 | 最大生命 | 房间类型 |
| turn_start | 当前咏唱 | 当前生命 | 格挡 |
| card | 本回合费用 | 打出时的咏唱 | 卡牌类型 |
| chant_gain | 获得的咏唱（含贤者时间加成） | 获得后合计 | |
| chant_spend | 支付的咏唱 | 支付后剩余 | |
| chant_lost | 失去的咏唱 | 0 | 原因，如 zero_chant |
| high_rank_spell | 需要的咏唱 | 实际支付 | 获得的枯竭 |
| continuation_grant / consume / expire | 1 | 0 | expire 表示回合结束时未使用 |
| freeze_apply | 本次冰冻 | 冰冻合计 | 怪物与当前计数器 |
| freeze_skip | 跳过时的计数器 | 新计数器 | 怪物与顺延的行动编号 |
| combat_end | 回合数 | 损失生命 | 胜负与本场各项合计 |

## 自动冒烟测试

```powershell
.\tools\smoke-test.ps1
```

脚本构建 RoxyMod.jar 和仅供开发的 RoxySmokeTest.jar，从 build/smoke-sandbox 的沙盒副本启动游戏。沙盒使用 preferences 的复制品，不会改动真实存档、跑图记录和 mods 目录。游戏窗口约一分钟后自动关闭，运行期间请勿点击窗口。

测试流程：在选人界面选择洛琪希并以固定种子启程，绘制全部 75 张卡的普通与升级卡面，随后与地精大块头、六火亡魂各战斗一场，检查冰冻后的击晕意图、行动顺延、计数器和六火亡魂的分裂伤害，最后进入战斗奖励界面。报告、截图、战斗日志和游戏日志都在 build/smoke/out。下次构建会清空 build 目录。

## 控制台命令

BaseMod 控制台用 ` 键打开。遭遇名中的空格写成下划线。

| 命令 | 用途 |
| --- | --- |
| fight Gremlin_Nob | 进入指定遭遇，如 Lagavulin、3_Sentries、Gremlin_Leader、Slime_Boss、The_Guardian、Champ、Collector、Automaton、Awakened_One、Time_Eater |
| hand add roxymod:RoxyLightning 1 1 | 加入手牌：卡牌 ID、数量、升级次数 |
| deck add roxymod:RoxyIciclePrison | 加入牌组 |
| relic add roxymod:RoxyWaterSigilRelic | 获得遗物 |
| energy inf | 无限能量，再输入一次关闭 |
| kill all | 结束当前战斗 |
| gold add 999 | 获得金币 |

卡牌 ID 见 src/main/resources/roxymod/localization/zhs/CardStrings.json，遗物 ID 见同目录的 RelicStrings.json。

## 发布检查清单的覆盖情况

冒烟测试已覆盖：ModTheSpire 实际加载与冰冻补丁注入、全部普通与升级卡面、选人背景与头像、战斗站姿、冰冻的击晕意图与行动顺延、精英战后的奖励界面。

仍需手动检查，括号内为建议的命令：

- 关键词悬停和卡牌大图。
- 倒地与营火的显示位置。
- 雷光在虚弱、易伤、无实体下的数值与动画（fight Nemesis，hand add roxymod:RoxyLightning）。
- 冰柱牢、冰幕药水、水之护符与人工制品、湿润的交互（fight 3_Sentries，哨卫有人工制品）。
- 多个怪物连续冻结、阶段切换与召唤物（fight Gremlin_Leader、Slime_Boss、The_Guardian、Reptomancer、Collector）。
- 回声授课复制结式与接续遗物的结算动画。
- 满手时的迷宫地图、再咏唱与复习减费。
- Boss 奖励、宝箱，以及中途保存后继续。
- 至少 10 场完整战斗，带日志，记录回合数、受伤量与循环。

## 2026-11-07 实机发现

2026-11-11 已全部修正，并经冒烟测试截图确认。

1. 卡面上的 [E] 小图标显示为纯色青色方块（魔力储备、魔力细线、潮汐切割、魔力凝缩），在文字中像缺字方框。图标改为卡牌费用徽章的缩小版，由 art_sources/ui/generate_small_orb.ps1 生成。
2. 水针、结晶领域的说明换行后，行首出现逗号。改为在逗号后主动换行，效果不变。
3. 圣级水枪、雷光的说明有 6 行，第一行压住“攻击”类型标签。精简为 4 行和 5 行，效果不变。两份总览表格同步了这 4 张卡的说明。
4. 冰冻图标的“3/3”遮挡大半个图标。改为缩小并下移，与原版数字位置一致。
5. 选人界面里起始遗物说明的 [E] 显示为红色能量图标：此时尚无角色，游戏退回铁甲战士的图标。新增补丁，选中洛琪希时使用她的图标。

另：战斗中左下角的能量球原为原版红色，2026-11-11 已换成洛琪希的专属能量球，见 [API 美术补全](art_api_20261111.md)。
