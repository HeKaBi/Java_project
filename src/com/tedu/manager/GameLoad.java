package com.tedu.manager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;
import com.tedu.show.GameJFrame;

/**
 * @说明  加载器(工具：用户读取配置文件的工具)工具类,大多提供的是 static方法
 * @author renjj
 *
 */
public class GameLoad {
//	得到资源管理器
	private static ElementManager em=ElementManager.getManager();
	
//	图片集合  使用map来进行存储     枚举类型配合移动(扩展)
	public static Map<String,ImageIcon> imgMap = new HashMap<>();
	
	public static Map<String,List<ImageIcon>> imgMaps;

	private static final String GAME_DATA_PATH = "com/tedu/text/GameData.pro";
	private static final String OBJ_DATA_PATH = "com/tedu/text/obj.pro";

	/**
	 * @说明 传入地图id有加载方法依据文件规则自动产生地图文件名称，加载文件
	 * @param mapId  文件编号 文件id
	 */
	public static void MapLoad(int mapId) {
//		得到啦我们的文件路径
		String mapName="com/tedu/text/"+mapId+".map";
		Properties mapPro = loadProperties(mapName);
		if(mapPro.isEmpty()) {
			System.out.println("配置文件读取异常,请重新安装");
			return;
		}
		Enumeration<?> names = mapPro.propertyNames();
		while(names.hasMoreElements()) {//获取是无序的
//			这样的迭代都有一个问题：一次迭代一个元素。
			String key=names.nextElement().toString();
			String value = mapPro.getProperty(key);
			if (value == null || value.trim().isEmpty()) {
				continue;
			}
//			就可以自动的创建和加载 我们的地图啦 
			String [] arrs=value.split(";");
			for(int i=0;i<arrs.length;i++) {
				ElementObj obj=getObj("map");
				if (obj == null) {
					continue;
				}
				ElementObj element = obj.createElement(key+","+arrs[i]);
				em.addElement(element, GameElement.MAPS);
			}
		}
	}
	/**
	 *@说明 加载图片代码
	 *加载图片 代码和图片之间差 一个 路径问题 
	 */
	public static void loadImg() {//可以带参数，因为不同的关也可能需要不一样的图片资源
		Properties imgPro = loadProperties(GAME_DATA_PATH);
		imgMap.clear();
		Set<Object> set = imgPro.keySet();//是一个set集合
		for(Object o:set) {
			String key = o.toString();
			String url=imgPro.getProperty(key);
			ImageIcon icon = loadIcon(url);
			if (icon != null) {
				imgMap.put(key, icon);
			} else {
				System.out.println("图片加载失败:" + key + " -> " + url);
			}
		}
	}
	/**
	 * 加载玩家
	 */
	public static void loadPlay() {
		loadObj();
		String playStr="120,0,paopao";//没有放到配置文件中
		ElementObj obj=getObj("paopao");  //因为我们是依靠的字符串来读取和创建对象
		if (obj == null) {
			return;
		}
//		这个字符串是key  也是 唯一 id 相当于为 每个类起啦一个唯一的id名称
//		这个字符串名称一定要和 obj.pro中的key相同
		ElementObj play = obj.createElement(playStr);
		if (play == null) {
			return;
		}
		int groundY = GameJFrame.GameY - play.getH() - 20;
		play.setY(Math.max(0, groundY));
//		ElementObj play = new Play().createElement(playStr);
//		解耦,降低代码和代码之间的耦合度 可以直接通过 接口或者是抽象父类就可以获取到实体对象
//		通过配置文件的耦合，降低代码的耦合度
		em.addElement(play, GameElement.PLAY);
	}
	
	public static ElementObj getObj(String str) {
		try {
			Class<?> class1 = objMap.get(str);
			Object newInstance = class1.newInstance();
			if(newInstance instanceof ElementObj) {
				return (ElementObj)newInstance;   //这个对象就和 new Play()等价
//				新建立啦一个叫  GamePlay的类
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 扩展： 使用配置文件，来实例化对象 通过固定的key(字符串来实例化)
	 * @param args
	 */
	private static Map<String,Class<?>> objMap=new HashMap<>();
	
	public static void loadObj() {
		Properties objPro = loadProperties(OBJ_DATA_PATH);
		objMap.clear();
		Set<Object> set = objPro.keySet();//是一个set集合
		for(Object o:set) {
			String classUrl=objPro.getProperty(o.toString());
//			使用反射的方式直接将 类进行获取
			try {
				Class<?> forName = Class.forName(classUrl);
				objMap.put(o.toString(), forName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private static Properties loadProperties(String resourcePath) {
		Properties properties = new Properties();
		try {
			InputStream input = openStream(resourcePath);
			if (input == null) {
				System.out.println("配置文件读取异常:" + resourcePath);
				return properties;
			}
			try (InputStream in = input;
					InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	private static ImageIcon loadIcon(String resourcePath) {
		if (resourcePath == null) {
			return null;
		}
		String path = resourcePath.trim();
		if (path.isEmpty()) {
			return null;
		}
		try (InputStream stream = openStream(path)) {
			if (stream != null) {
				ImageIcon icon = new ImageIcon(readBytes(stream));
				if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
					return icon;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		ImageIcon fileIcon = new ImageIcon(path);
		if (fileIcon.getIconWidth() > 0 && fileIcon.getIconHeight() > 0) {
			return fileIcon;
		}
		return null;
	}

	private static InputStream openStream(String resourcePath) throws IOException {
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream stream = classLoader.getResourceAsStream(resourcePath);
		if (stream != null) {
			return stream;
		}
		File file = new File(resourcePath);
		if (file.exists()) {
			return new FileInputStream(file);
		}
		File sourceFile = new File("src", resourcePath);
		if (sourceFile.exists()) {
			return new FileInputStream(sourceFile);
		}
		return null;
	}

	private static byte[] readBytes(InputStream stream) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len;
		while ((len = stream.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		return out.toByteArray();
	}
	
	
	
//	用于测试
	public static void main(String[] args) {
		MapLoad(5);
		
		
		try {
//			通过类路径名称， com.tedu.Play
			Class<?> forName = Class.forName("");
//			通过类名  可以直接访问到这个类
			Class<?> forName1=GameLoad.class;
//			通过实体对象 获取 反射对象
			GameLoad gameLoad = new GameLoad();
			Class<? extends GameLoad> class1 = gameLoad.getClass();
			
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
}
