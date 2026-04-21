# Game1

一个基于 Java Swing 的横版动作射击小游戏原型，整体风格接近《合金弹头》。

项目当前已经具备完整的本地运行链路，包括：

- 主角移动、跳跃、下蹲、抬枪、射击、近战、手雷
- 普通敌人、侦察兵、Boss
- 两个关卡流程
- 人质救援与补给掉落
- HUD 信息显示
- 背景音乐和音效
- 失败后按 `R` 重新开始

## 项目预览

这是一个桌面端原型项目，不依赖浏览器，也没有接入 Maven 或 Gradle。程序入口是：

`src/com/tedu/game/GameStart.java`

启动后会打开一个 `1000 x 640` 的游戏窗口，标题为 `Game1 - Metal Slug Prototype`。

## 当前玩法

游戏目前包含 2 个关卡：

- `Stage 1`：推进、清敌、救人质、打 Boss
- `Stage 2`：继续推进并完成最终 Boss 战

玩家在推进过程中会遇到：

- 普通敌人
- 侦察型敌人
- Boss
- 可救援人质
- 人质掉落的补给箱

界面右上和左上会显示当前战斗信息，包括：

- HP
- 手雷数量
- 当前武器
- 重武器是否已解锁
- 击杀数
- 生存时间
- 关卡进度
- 当前关卡
- Boss 血量

## 操作说明

| 按键 | 功能 |
| --- | --- |
| `A` / `←` | 向左移动 |
| `D` / `→` | 向右移动 |
| `W` / `↑` | 向上移动纵深 |
| `S` / `↓` | 向下移动纵深 |
| `E` | 向上瞄准 |
| `Shift` | 下蹲 |
| `Space` | 跳跃 |
| `F` | 开火 |
| `L` | 小刀近战 |
| `U` | 投掷手雷 |
| `1` | 切回步枪 |
| `2` | 切换重机枪（需要先救出人质获得补给） |
| `R` | 游戏结束后重新开始 |

## 目录结构

```text
Game1
├─ src
│  └─ com/tedu
│     ├─ game        # 程序入口
│     ├─ show        # 窗口和绘制面板
│     ├─ controller  # 输入监听、主循环
│     ├─ manager     # 资源加载、运行时状态、音频、元素管理
│     ├─ element     # 玩家、敌人、Boss、子弹、特效、补给等实体
│     └─ text        # 对象与资源配置
├─ image            # 图片资源
├─ music            # 音频资源
├─ out              # 编译输出目录
├─ out_tmp          # 临时调试/验证产物
└─ Game1.iml        # IntelliJ IDEA 工程文件
```

## 运行环境

建议环境：

- JDK 8 及以上
- IntelliJ IDEA 运行，或使用命令行手动编译运行

我已按当前代码结构做过一次命令行编译校验，项目可以在 `JDK 24` 下通过编译。

## 在 IntelliJ IDEA 中运行

1. 用 IDEA 打开当前项目根目录。
2. 确认 Project SDK 已配置为可用的 JDK。
3. 直接运行 `src/com/tedu/game/GameStart.java` 中的 `main` 方法。
4. 建议保持运行工作目录为项目根目录，否则图片和音频资源可能无法正确加载。

## 命令行运行

在项目根目录执行：

```powershell
New-Item -ItemType Directory -Force -Path out\run | Out-Null
$sources = rg --files src -g "*.java"
javac -encoding UTF-8 -d out\run $sources
java -cp "out\run;src" com.tedu.game.GameStart
```

如果你的环境里没有 `rg`，也可以改成：

```powershell
New-Item -ItemType Directory -Force -Path out\run | Out-Null
$sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out\run $sources
java -cp "out\run;src" com.tedu.game.GameStart
```

## 资源说明

项目使用了大量本地图片和音频资源，主要包括：

- `image/images/...`：角色、敌人、Boss、背景、子弹、爆炸、补给等贴图
- `music/...`：背景音乐与音效

资源加载逻辑同时支持：

- 从类路径读取
- 从项目目录读取
- 从 `src` 目录下读取

因此最稳妥的方式仍然是在项目根目录下启动程序。

## 配置文件

项目中有两个核心配置文件：

- `src/com/tedu/text/obj.pro`：定义对象名到 Java 类的映射
- `src/com/tedu/text/GameData.pro`：定义部分资源键值映射

其中更复杂的角色动画帧资源，当前主要是直接从资源目录读取。

## 当前实现特点

从现有代码来看，这个项目已经不是最初的空壳 demo，而是一个具备基础玩法闭环的原型：

- 玩家动作拆分为上下半身动画
- 支持步枪和重机枪两种武器
- 支持跳跃、下蹲、抬枪和近战
- 关卡推进采用横向卷屏
- 敌人、Boss、补给和人质都已接入主循环
- 结算界面支持失败/通关展示和重开

## 已知说明

- 这是一个原型项目，当前没有完整的打包脚本。
- 项目采用相对路径读取资源，启动位置不对时可能会出现资源加载失败。
- 仓库内包含 `out`、`out_tmp` 等编译或调试产物目录，它们不是核心源码的一部分。

## 后续可扩展方向

如果你后面还要继续完善这个项目，比较自然的方向包括：

- 加入开始菜单、暂停、设置页面
- 增加更多关卡和敌人类型
- 完善角色受击、死亡和 Boss 技能表现
- 接入存档、分数系统和排行榜
- 补充构建脚本，迁移到 Maven 或 Gradle
- 增加发布包和演示截图

## 入口文件

主入口：

`src/com/tedu/game/GameStart.java`

核心主循环：

`src/com/tedu/controller/GameThread.java`

主角逻辑：

`src/com/tedu/element/PaoPao.java`
