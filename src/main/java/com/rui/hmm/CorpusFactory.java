package com.rui.hmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/*
 * 语料库过大，要避免重复加载语料库
 */
public class CorpusFactory {
	public static HashMap<Character, Integer> observerList = new HashMap<Character, Integer>();

	public CorpusFactory() throws IOException {

	}

	static {
		
		FileInputStream in;
		try {
			in = new FileInputStream(new File("src/main/resources/msr_training.utf8"));
			BufferedReader br=new BufferedReader(new InputStreamReader(in, "utf-8"));
			String line="";
			//观察序列索引从1开始
			Integer index=1;
			while((line=br.readLine())!=null){
				String[]tokens=line.split("\\s+");
				for(int i=0;i<tokens.length;i++){
					String words=tokens[i].trim();
					for(int j=0;j<words.length();j++){
						if(!observerList.containsKey(words.charAt(j))){
							observerList.put(words.charAt(j), index);
							index++;
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public static void main(String[] args) throws IOException {
		
		System.out.println(CorpusFactory.observerList.toString());
		System.out.println(CorpusFactory.observerList.size());
	}
	
}
