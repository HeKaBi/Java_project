# Game1

`Game1` 是一个基于 Java Swing 的横版动作射击游戏原型，整体风格参考《合金弹头》。

项目目前已经具备一条完整的本地可运行链路：

- 开始界面
- 三个可游玩关卡
- 横向推进与关卡转场
- 人质营救与补给掉落
- Boss 战
- 失败 / 通关结算
- `R` 键重新开始

程序入口：

`src/com/tedu/game/GameStart.java`

启动后会打开一个 `1000 x 640` 的游戏窗口，标题为 `Game1 - Metal Slug Prototype`。

## 当前实现

- 3 个关卡，包含独立地图、BGM、关卡推进和 Boss 战
- 玩家支持左右移动、二段跳、上瞄、下蹲、平台下落、射击、近战和手雷
- 横向卷屏、地形采样、平台碰撞和关卡转场遮罩
- 地面敌兵、精英敌兵、飞行单位、Boss 行为与受击判定
- 人质营救与补给掉落系统
- 武器切换系统：步枪、Heavy Machine Gun、Rocket Launcher
- HUD 信息显示：生命、手雷、武器、击杀数、生存时间、关卡进度、Boss 血条
- 开始界面、任务横幅、失败/通关结算与重开流程

## 关卡流程

当前版本包含 3 个可游玩的关卡：

- `Stage 1`：基础推进关卡，人质奖励为重机枪
- `Stage 2`：敌人密度更高，加入更多空中压制，人质奖励为手雷补给
- `Stage 3`：最终关卡，人质奖励为火箭筒

推进逻辑不是单纯“打完 Boss 就过关”。想进入下一关，玩家需要同时完成以下目标：

- 救出当前关卡的人质
- 清掉场上残余敌人
- 击败当前关卡 Boss

如果 Boss 已经被击败，但还有目标没完成，界面横幅会继续提示剩余任务。

## 操作说明

| 按键 | 功能 |
| --- | --- |
| `Enter` | 在开始界面进入游戏 |
| `A` / `←` | 向左移动 |
| `D` / `→` | 向右移动 |
| `W` | 跳跃，可二段跳 |
| `E` / `↑` | 向上瞄准 |
| `Ctrl` / `↓` / `S` | 下蹲 |
| `S` 连按两次 | 从当前平台下落 |
| `J` | 射击，按住可持续开火 |
| `L` | 近战小刀 |
| `U` | 投掷手雷 |
| `1` | 切换步枪 |
| `2` | 切换重机枪（解锁后可用） |
| `3` | 切换火箭筒（解锁后可用） |
| `Q` | 在已解锁武器间循环切换 |
| `R` | 结算界面重新开始 |

## 目录结构

```text
Game1
├─ src
│  └─ com/tedu
│     ├─ game        # 程序入口
│     ├─ show        # 窗口与渲染面板
│     ├─ controller  # 输入监听、主循环、关卡推进
│     ├─ manager     # 资源加载、运行时状态、音频、元素管理
│     ├─ element     # 玩家、敌人、Boss、子弹、特效、补给等实体
│     └─ text        # 对象与资源配置
├─ image             # 图片资源
├─ music             # 音频资源
├─ out               # 编译输出目录
├─ out_check         # 本地编译检查产物
├─ out_tmp           # 调试临时产物
├─ out_verify        # 其他验证产物
└─ Game1.iml         # IntelliJ IDEA 工程文件
```

## 运行环境

- JDK 8 及以上
- IntelliJ IDEA，或使用命令行手动编译运行
- 当前代码已按现有目录结构通过 `javac -encoding UTF-8` 编译校验

说明：

- 这是一个纯 Java 工程，当前没有接入 Maven 或 Gradle
- 项目依赖本地图片和音频资源，建议从项目根目录启动

## 在 IntelliJ IDEA 中运行

1. 用 IDEA 打开项目根目录。
2. 确认 `Project SDK` 已配置为可用的 JDK。
3. 直接运行 `src/com/tedu/game/GameStart.java` 中的 `main` 方法。
4. 建议保持运行工作目录为项目根目录，避免资源路径解析异常。

## 命令行运行

在项目根目录执行：

```powershell
New-Item -ItemType Directory -Force -Path out\run | Out-Null
$sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out\run $sources
java -cp "out\run;src" com.tedu.game.GameStart
```

## 资源与配置说明

项目中的资源主要包括：

- `image/images/...`：角色、敌人、Boss、背景、子弹、爆炸、补给等贴图
- `music/...`：关卡背景音乐与战斗音效

核心配置文件：

- `src/com/tedu/text/obj.pro`：对象名称到 Java 类的映射
- `src/com/tedu/text/GameData.pro`：基础资源键值映射

资源加载逻辑同时兼容以下几种方式：

- 从类路径读取
- 从项目目录读取
- 从 `src` 目录下读取

因此最稳妥的启动方式依然是从项目根目录运行程序。

## 当前实现亮点

- 玩家上下半身动画拆分，便于实现移动、上瞄、开火和近战组合表现
- 地形高度采样与平台系统，让角色移动不再依赖单一平地
- 多阶段任务目标设计，关卡推进不只依赖击败 Boss
- 关卡间黑幕开合式转场，开始界面和 HUD 也都已经独立成型
- 音频系统区分 BGM 和一次性音效，适合继续扩展更多反馈

## 后续可以继续扩展的方向

- 开始菜单、暂停菜单和设置界面
- 更多关卡、更多敌兵类型与 Boss 技能
- 分数系统、评价结算与存档
- 更完整的资源整理与打包流程
- 迁移到 Maven / Gradle，补齐构建脚本
