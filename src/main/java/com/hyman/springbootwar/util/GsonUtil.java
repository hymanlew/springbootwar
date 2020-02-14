package com.hyman.springbootwar.util;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class GsonUtil {
	public static Gson gson;
	
	public static Gson getGson() {
		if(gson==null){
			return new Gson();
		}else {
			return gson;
		}
	}

	public static String ReadFile(String path) {
		FileInputStream file = null;
		BufferedReader reader = null;
		InputStreamReader inputFileReader = null;
		String content = "";
		String tempString = null;
		try {
			file = new FileInputStream(path);
			inputFileReader = new InputStreamReader(file, "utf-8");
			reader = new BufferedReader(inputFileReader);
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				content += tempString;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return content;
	}
}
