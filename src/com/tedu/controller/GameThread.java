package com.tedu.controller;

import com.tedu.element.ElementObj;
import com.tedu.element.PaoPao;
import com.tedu.manager.AudioPlayer;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.GameRuntime;
import com.tedu.show.GameJFrame;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @说明 游戏的主线程，用于控制游戏加载，游戏关卡，游戏运行时自动化
 * 		游戏判定；游戏地图切换 资源释放和重新读取。。。
 * @author renjj
 * @继承 使用继承的方式实现多线程(一般建议使用接口实现)
 */
public class GameThread extends Thread{
	private ElementManager em;
	private final Random random = new Random();
	private long enemyAddTime;
	
	public GameThread() {
		em=ElementManager.getManager();
	}
	@Override
	public void run() {//游戏的run方法  主线程
		while(true) { //扩展,可以讲true变为一个变量用于控制结束
//		游戏开始前   读进度条，加载游戏资源(场景资源)
			gameLoad();
//		游戏进行时   游戏过程中
			gameRun();
//		游戏场景结束  游戏资源回收(场景资源)
			gameOver();
			try {
				sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * 游戏的加载
	 */
	private void gameLoad() {
		GameRuntime.resetForNewGame();
		em.init();
		GameLoad.loadImg(); //加载图片
		GameLoad.loadObj();
		loadMap();
		GameLoad.loadPlay();//也可以带参数，单机还是2人
		AudioPlayer.playBgmLoop("music/boss_lv.wav");
		
//		全部加载完成，游戏启动
	}

	private void loadMap() {
		ElementObj mapAObj = GameLoad.getObj("map");
		if (mapAObj == null) {
			return;
		}
		ElementObj mapA = mapAObj.createElement("0,0,map");
		em.addElement(mapA, GameElement.MAPS);

		ElementObj mapBObj = GameLoad.getObj("map");
		if (mapBObj == null) {
			return;
		}
		int mapWidth = Math.max(GameJFrame.GameX, mapA.getW());
		em.addElement(mapBObj.createElement(mapWidth + ",0,map"), GameElement.MAPS);
	}
	/**
	 * @说明  游戏进行时
	 * @任务说明  游戏过程中需要做的事情：1.自动化玩家的移动，碰撞，死亡
	 *                                 2.新元素的增加(NPC死亡后出现道具)
	 *                                 3.暂停等等。。。。。
	 * 先实现主角的移动
	 * */
	
	private void gameRun() {
		long gameTime=0L;//给int类型就可以啦
		while(true) {// 预留扩展   true可以变为变量，用于控制管关卡结束等
			Map<GameElement, List<ElementObj>> all = em.getGameElements();
			List<ElementObj> enemys = em.getElementsByKey(GameElement.ENEMY);
			List<ElementObj> playFiles = em.getElementsByKey(GameElement.PLAYFILE);
			List<ElementObj> enemyFiles = em.getElementsByKey(GameElement.ENEMYFILE);
			List<ElementObj> plays = em.getElementsByKey(GameElement.PLAY);
			spawnEnemy(gameTime);
			moveAndUpdate(all,gameTime);//	游戏元素自动化方法
			
			ElementPK(enemys,playFiles);
			ElementPK(enemyFiles,plays);
			EnemyHitPlay(enemys,plays,gameTime);
			GameRuntime.survivalTimeMs = System.currentTimeMillis() - GameRuntime.startTimeMs;
			if (plays.isEmpty()) {
				GameRuntime.waitingRestart = true;
				break;
			}
			
			gameTime++;//唯一的时间控制
			try {
				sleep(10);//默认理解为 1秒刷新100次 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void spawnEnemy(long gameTime) {
		if (gameTime - enemyAddTime < 80) {
			return;
		}
		enemyAddTime = gameTime;
		ElementObj enemyObj = GameLoad.getObj("enemy");
		if (enemyObj == null) {
			return;
		}
		ElementObj enemy = enemyObj.createElement((GameJFrame.GameX + 20) + ",0,enemy,2");
		int maxY = Math.max(0, GameJFrame.GameY - enemy.getH() - 20);
		int minY = Math.min(maxY, Math.max(100, maxY - 220));
		int y = minY;
		if (maxY > minY) {
			y = minY + random.nextInt(maxY - minY + 1);
		}
		enemy.setY(y);
		em.addElement(enemy, GameElement.ENEMY);
	}
	public void ElementPK(List<ElementObj> listA,List<ElementObj>listB) {
//		请大家在这里使用循环，做一对一判定，如果为真，就设置2个对象的死亡状态
		for(int i=0;i<listA.size();i++) {
			ElementObj a=listA.get(i);
			for(int j=0;j<listB.size();j++) {
				ElementObj b=listB.get(j);
				if(a.pk(b)) {
//					问题： 如果是boos，那么也一枪一个吗？？？？
//					将 setLive(false) 变为一个受攻击方法，还可以传入另外一个对象的攻击力
//					当收攻击方法里执行时，如果血量减为0 再进行设置生存为 false
//					扩展 留给大家
					AudioPlayer.playOnce("music/die.wav");
					a.setLive(false);
					b.setLive(false);
					if (listA == em.getElementsByKey(GameElement.ENEMY) && listB == em.getElementsByKey(GameElement.PLAYFILE)) {
						GameRuntime.killCount++;
					}
					break;
				}
			}
		}
	}

	public void EnemyHitPlay(List<ElementObj> enemys, List<ElementObj> plays, long gameTime) {
		for (int i = 0; i < enemys.size(); i++) {
			ElementObj enemy = enemys.get(i);
			for (int j = 0; j < plays.size(); j++) {
				ElementObj playObj = plays.get(j);
				if (!enemy.pk(playObj)) {
					continue;
				}
				if (playObj instanceof PaoPao) {
					PaoPao play = (PaoPao) playObj;
					long oldHurtTime = play.getHurtTime();
					play.hurt(gameTime, 1);
					if (play.getHurtTime() != oldHurtTime) {
						AudioPlayer.playOnce("music/die.wav");
					}
				} else {
					playObj.setLive(false);
				}
				enemy.setLive(false);
				break;
			}
		}
	}
	
	
	
	
//	游戏元素自动化方法
	public void moveAndUpdate(Map<GameElement, List<ElementObj>> all,long gameTime) {
//		GameElement.values();//隐藏方法  返回值是一个数组,数组的顺序就是定义枚举的顺序
		for(GameElement ge:GameElement.values()) {
			List<ElementObj> list = all.get(ge);
//			编写这样直接操作集合数据的代码建议不要使用迭代器。
//			for(int i=0;i<list.size();i++) {
			for(int i=list.size()-1;i>=0;i--){	
				ElementObj obj=list.get(i);//读取为基类
				if(!obj.isLive()) {//如果死亡
//					list.remove(i--);  //可以使用这样的方式
//					启动一个死亡方法(方法中可以做事情例如:死亡动画 ,掉装备)
					obj.die();//需要大家自己补充
					list.remove(i);
					continue;
				}
				obj.model(gameTime);//调用的模板方法 不是move
			}
		}	
	}
	

	
	/**游戏切换关卡*/
	private void gameOver() {
		AudioPlayer.stopBgm();
		while (!GameRuntime.restartRequested) {
			try {
				sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		GameRuntime.waitingRestart = false;
	}
	
}





