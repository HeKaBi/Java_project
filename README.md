# MetalSlugCourseProject

基于 `hnsfGameFram` 参考工程重新搭建的课程作业版《合金弹头》原型项目。  
本项目继续沿用 `Java SE + Swing/AWT + 双线程刷新 + ElementManager + Properties/obj.pro/map` 的参考技术路线，没有引入额外游戏引擎。

## 当前完成度

当前版本完成了第一阶段骨架和一部分第二阶段逻辑：

- 新建独立项目目录，不污染参考工程
- 保留参考工程的包分层方式
- 已接入首批合金弹头素材
- 已打通 `GameData.pro`、`obj.pro`、`1.map`
- 已实现背景、场景碰撞块、玩家、敌人、玩家子弹、爆炸特效
- 已支持横版摄像机跟随
- 已支持基础 HUD 和重开逻辑

当前可玩内容：

- `A / D` 或方向键左右移动
- `W / Space` 或上方向键跳跃
- `S` 或下方向键下蹲
- `J / Z` 射击
- `K / X` 近战
- 清空敌人后显示 `Stage Clear`
- 死亡后显示 `Game Over`
- `R` 重新开始

## 项目结构

```text
MetalSlugCourseProject
├─ image
│  └─ mslug
│     ├─ background      背景图
│     ├─ player          玩家动作帧
│     ├─ enemy           敌人士兵动作帧
│     ├─ bullet          子弹素材
│     └─ effect          爆炸特效
├─ src
│  └─ com
│     └─ tedu
│        ├─ controller   输入监听、主线程
│        ├─ element      游戏对象
│        ├─ game         启动入口
│        ├─ manager      管理器、运行态、加载器
│        ├─ show         窗口与面板
│        └─ text         配置文件、关卡文件
└─ README.md
```

## 关键设计

- `GameStart`：启动入口
- `GameJFrame`：窗口初始化
- `GameMainJPanel`：绘制与 HUD
- `GameThread`：主循环与碰撞处理
- `ElementManager`：按层统一管理元素
- `GameLoad`：加载资源、对象映射、关卡配置
- `GameRuntime`：保存摄像机、分数、关卡状态

## 启动说明

### 方式 1：IDEA / Eclipse

1. 打开 `MetalSlugCourseProject` 目录
2. 将 `src` 标记为源码目录
3. 直接运行 `src/com/tedu/game/GameStart.java`
4. 运行工作目录保持在项目根目录

### 方式 2：PowerShell 命令行

在项目根目录执行：

```powershell
New-Item -ItemType Directory -Force -Path .\bin | Out-Null
$sources = Get-ChildItem .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d .\bin $sources
java -cp .\bin;.\src com.tedu.game.GameStart
```

或者直接执行：

```powershell
.\run.ps1
```

注意：

- 必须在 `MetalSlugCourseProject` 根目录下运行
- 运行时需要让 `.\src` 也在 classpath 中，因为 `GameData.pro`、`obj.pro`、`1.map` 放在 `src/com/tedu/text`

## 后续建议

下一阶段建议继续补齐：

- 敌方子弹
- Boss
- 人质与掉落物
- 音效播放
- 更多关卡配置
- 通关/失败专用界面与状态流转
