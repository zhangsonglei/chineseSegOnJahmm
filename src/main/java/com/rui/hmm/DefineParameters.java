package com.rui.hmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/*
 * get the form of parameters and chinese dict.
 */
public class DefineParameters {

	// 隐藏状态的字典对应关系
	private final static HashMap<Character, Integer> map = new HashMap<Character, Integer>();
	private final static HashMap<Integer, Character> remap = new HashMap<Integer, Character>();

	// 加载序列化的词典
	//private final static HashMap<Character, Integer> dict;

	static {
		map.put('B', 0);
		map.put('M', 1);
		map.put('E', 2);
		map.put('S', 3);
		remap.put(0, 'B');
		remap.put(1, 'M');
		remap.put(2, 'E');
		remap.put(3, 'S');
	}

	public static HashMap<Character, Integer> getMap() {
		return map;
	}

	public static HashMap<Integer, Character> getRemap() {
		return remap;
	}


	public static void main(String[] args) {
	}
}