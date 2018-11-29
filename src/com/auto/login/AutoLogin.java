package com.auto.login;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class AutoLogin {

	static String separatorChar = String.valueOf(File.separatorChar);
	static String userPath = System.getProperty("user.dir");
	static String path = userPath + separatorChar + "config" + separatorChar;

	private String pageEncode = "UTF8";

	public static void main(String[] args) throws Exception {
		doJS();

	}

	public static void doJS() throws Exception {
		String result2 = "";
		AutoLogin t = new AutoLogin();
		String aa = t.getPageSource();
		int begin = aa.indexOf("hexMD5");
		String bb = aa.substring(begin + 8, begin + 12);
		int begin2 = aa.indexOf("document.login.password.value");
		String cc = aa.substring(begin2 + 33, begin2 + 97);
		ScriptEngineManager maneger = new ScriptEngineManager();
		ScriptEngine engine = maneger.getEngineByName("JavaScript");
		String jsFile = path + "domd5.js";
		FileInputStream fileInputStream = new FileInputStream(new File(jsFile));
		Reader scriptReader = new InputStreamReader(fileInputStream, "utf-8");
		File file = new File(path + "md5.js");
		FileInputStream fis = new FileInputStream(file);
		int len = 0;
		String js = "";
		byte[] buf = new byte[200];
		while ((len = fis.read(buf)) != -1) {
			js = js + new String(buf, 0, len);
		}
		fis.close();
		String val = bb + getProperty("password") + cc;

		js = js + "var asd='" + val + "';";
		js = js + "function hexMD5 () { return binl2hex(coreMD5( str2binl(asd))) }";
		File file2 = new File(path + "domd5.js");
		FileOutputStream fos = new FileOutputStream(file2);
		fos.write(js.getBytes());
		fos.flush();
		fos.close();

		try {
			engine.eval(scriptReader);
			if (engine instanceof Invocable) {
				Invocable invocable = (Invocable) engine;
				result2 = (String) invocable.invokeFunction("hexMD5");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scriptReader.close();
		}

		String param = "username=" + getProperty("userName") + "&password=" + result2 + "&dst=&popup=true";
		sendPost(getProperty("loginUrl"), param);

	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();

			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public String getPageSource() {
		StringBuffer sb = new StringBuffer();
		try {
			// 构建一URL对象
			URL url = new URL(getProperty("loginUrl"));
			// 使用openStream得到一输入流并由此构造一个BufferedReader对象
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), pageEncode));
			String line;
			// 读取www资源
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
		} catch (Exception ex) {
			System.err.println(ex);
		}
		return sb.toString();
	}

	public static String getProperty(String propertyName) {

		Properties pro = new Properties();
		try {
			String path = System.getProperty("user.dir") + separatorChar + "config" + separatorChar
					+ "config.properties";
			FileInputStream in = new FileInputStream(path);
			pro.load(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = pro.getProperty(propertyName).trim();
		// message = new String(message.getBytes(StandardCharsets.ISO_8859_1),
		// StandardCharsets.UTF_8);
		return message;
	}

}
