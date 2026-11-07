# 洛琪希 · 水与咏唱

《杀戮尖塔》角色模组，当前版本 **0.2.1（2026-10-23）**。

- 75 张卡牌：4 基础、22 普通、33 罕见、16 稀有；21 攻击、45 技能、9 能力。
- 10 件专属遗物、3 种专属药水；角色为 70 生命、3 能量、每回合抽 5 张牌。
- 初始卡组：4 水弹术、4 格挡、初级咏唱、水幕。
- 初始遗物：每回合首次消耗接续时获得 1 能量；Boss 升级遗物额外获得 1 咏唱。

## 玩法

咏唱跨回合保留，用于强化卡牌。起式获得本回合有效的接续，结式消耗接续触发额外收益。高阶术式在咏唱不足时仍可施放，缺少层数转化为下回合扣能量的枯竭，最多 2 层。

湿润最多 3 层，每轮结束减少 1 层。一次冰冻施加最多消耗 1 层湿润、增加 1 层冰冻；人工制品阻止冰冻时保留湿润。雷光消耗全部湿润，每层增加 5 点基础伤害，再参与正常伤害修正。

每个怪物的冰冻阈值初始为 3，达到阈值时跳过行动，扣除等量冰冻，下次阈值增加 1。复习从本场战斗第二次打出相同卡牌 ID 时触发，升级版本共用记录。

## 构建与验证

需要完整 JDK（支持 Java 8 目标编译）、游戏本体、ModTheSpire 与 BaseMod。优先使用 JAVA_HOME，否则使用 jar.exe 所在的 JDK。

```powershell
.\build.ps1 -Verify
.\build.ps1 -Verify -Install
```

产物：build/libs/StarterMod.jar。默认安装到：

```text
C:\Program Files (x86)\Steam\steamapps\common\SlayTheSpire\mods\StarterMod.jar
```

可通过 STS_GAME_DIR、MTS_JAR、BASEMOD_JAR 配置依赖位置。安装后校验 SHA-256；游戏已运行时需重新加载模组。

-Verify 运行实际游戏类上的无界面机制测试，并检查卡牌注册、本地化、图片尺寸及 JAR 内容。图形环境使用测试适配层，不能验证实际渲染、补丁注入或存档流程。测试文件不会进入发行包。日志在 build/mechanics-tests.log。

## 内容与美术

- [数值与代码修改记录](docs/polish_20261023.md)
- [Q 版美术改版记录](docs/chibi_art_20261023.md)
- [发布检查清单](docs/release_checklist.md)
- [卡牌总览](outputs/roxy_card_overview/roxy_card_overview.xlsx)
- [完整内容总览](outputs/roxy_content_overview/roxy_content_overview.xlsx)

0.2.1 使用内置 image_gen 重绘全部 75 张卡图，以及战斗、倒地、营火、选人背景和头像 5 张角色原画，导出 150 张大小卡图与 7 张角色资源。画风参考劣人 TV 的 Q 版比例、粗线条、平涂色块与夸张表情，保留洛琪希的蓝色双辫、魔女帽和水魔法主题。

原图和完整提示词位于 art_sources/cards/chibi_20261023/ 与 art_sources/character/chibi_20261023/。tools/import-card-art.ps1、tools/import-character-art.ps1 可重新导出游戏资源；tools/preview-card-art.ps1 输出全部卡图的实际尺寸检查拼图。0.2.1 沿用 0.2.0 的数值与机制修正。

为保留现有存档标识，本版沿用 startermod 命名空间及全部卡牌 ID。旧 E1/E2 改名之前的存档仍遵循原有兼容性限制。默认不再自动标记全部卡牌已见；开发者可使用 JVM 参数 -Droxy.dev.revealCards=true 临时开启。
