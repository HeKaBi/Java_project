package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.show.GameJFrame;
import java.awt.Graphics;
import javax.swing.ImageIcon;

/**
 * @author renjj
 * 1.继承父类
 * 2.重写各种方法，实现业务逻辑
 * 3.编写主线程的碰撞
 * 4.如果配置文件格式有改变，请重写 GameLoad里面的 加载方法
 */
public class PaoPao extends ElementObj{
	private int imgx=0;
	private int imgy=0;//由方向来控制
	private long imgtime=0;//用于控制图片变化速度
	private int hp = 5;
	private long hurtTime = -1000;
	private static final long INVINCIBLE_WINDOW = 80;
	private int speed = 4;
	private double vy = 0;
	private boolean onGround = true;
	private final double gravity = 0.8;
	private final double jumpVelocity = -12;
	private boolean up;
	private boolean left;
	private boolean right;
	private boolean faceRight = true;
	private boolean firing;
	private long fireTime;
	private ElementManager em = ElementManager.getManager();
	@Override
	public void showElement(Graphics g) {
		if (imgtime - hurtTime < INVINCIBLE_WINDOW && ((imgtime / 4) % 2 == 0)) {
			return;
		}
		if (this.getIcon() != null) {
			if (faceRight) {
				g.drawImage(this.getIcon().getImage(),
						this.getX() + this.getW(), this.getY(),
						this.getX(), this.getY() + this.getH(),
						null);
			} else {
				g.drawImage(this.getIcon().getImage(),
						this.getX(), this.getY(),
						this.getX() + this.getW(), this.getY() + this.getH(),
						null);
			}
		}
	}
	
	@Override
	protected void updateImage(long time) {
		if(time -imgtime>10) {
			imgtime=time;
			imgx++;
			if(imgx>3) {
				imgx=0;
			}
		}
	}
	@Override
	protected void move() {
		int x = this.getX();
		double y = this.getY();
		if (left) {
			x -= speed;
		}
		if (right) {
			x += speed;
		}
		if (!onGround) {
			vy += gravity;
			y += vy;
		}
		int groundY = GameJFrame.GameY - this.getH() - 20;
		if (y >= groundY) {
			y = groundY;
			vy = 0;
			onGround = true;
		}
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
			vy = 0;
		}
		if (x > GameJFrame.GameX - this.getW()) {
			x = GameJFrame.GameX - this.getW();
		}
		this.setX(x);
		this.setY((int) y);
	}
	public void keyClick(boolean bl,int key) {
		switch (key) {
		case 37:
			left = bl;
			if (bl) {
				faceRight = false;
			}
			break;
		case 38:
			up = bl;
			if (bl && onGround) {
				onGround = false;
				vy = jumpVelocity;
			}
			break;
		case 39:
			right = bl;
			if (bl) {
				faceRight = true;
			}
			break;
		case 32:
			firing = bl;
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void add(long gameTime) {
		if (!firing) {
			return;
		}
		if (gameTime - fireTime < 12) {
			return;
		}
		fireTime = gameTime;
		ElementObj bulletTemplate = GameLoad.getObj("bullet");
		if (bulletTemplate == null) {
			return;
		}
		int bulletX = faceRight ? this.getX() + this.getW() - 8 : this.getX() - 12;
		int bulletY = this.getY() + this.getH() / 2 - 8;
		int bulletSpeed = faceRight ? 12 : -12;
		ElementObj bullet = bulletTemplate.createElement(bulletX + "," + bulletY + ",bullet," + bulletSpeed);
		em.addElement(bullet, GameElement.PLAYFILE);
	}
	
	
	@Override  //"500,500,paopao"
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		this.setX(Integer.parseInt(split[0]));
		this.setY(Integer.parseInt(split[1]));
		ImageIcon icon=GameLoad.imgMap.get(split[2]);
		this.setIcon(icon);
		this.setW(icon == null ? 48 : icon.getIconWidth());
		this.setH(icon == null ? 48 : icon.getIconHeight());
		return this; //注意别忘啦 刚刚返回的是父类
	}

	public void hurt(long gameTime, int damage) {
		if (gameTime - hurtTime < INVINCIBLE_WINDOW) {
			return;
		}
		hurtTime = gameTime;
		hp -= damage;
		if (hp <= 0) {
			hp = 0;
			this.setLive(false);
		}
	}

	public int getHp() {
		return hp;
	}

	public long getHurtTime() {
		return hurtTime;
	}
	

}





