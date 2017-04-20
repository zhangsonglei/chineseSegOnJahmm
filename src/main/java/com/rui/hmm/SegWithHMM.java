package com.rui.hmm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.ViterbiCalculator;

/*
 * to seg a chinese sentenesa by a HMM model.
 */
public class SegWithHMM {
	private Hmm<ObservationInteger> hmm;

	public SegWithHMM(Hmm hmm) {
		this.hmm = hmm;
	}

	public void handleTxt(String txtPath) throws IOException {
		FileOutputStream out=new FileOutputStream("output/out.utf8");
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(out,"utf-8"));
		
		FileInputStream in = new FileInputStream(new File(txtPath));
		BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
		String line = "";
		
		bw.write(CorpusFactory.observerList.size()+">>>"+CorpusFactory.observerList.toString());
		bw.newLine();
		bw.flush();
		
		while ((line = br.readLine()) != null) {
			String tokens = line.trim();
			bw.write(seg(tokens));
			bw.newLine();
			bw.flush();
		}
		
		bw.write(CorpusFactory.observerList.size()+">>>"+CorpusFactory.observerList.toString());
		bw.newLine();
		bw.flush();
	}

	

	public String seg(String sen) throws IOException {
		// 将句子的每个观察字符列索引装入list
		List<ObservationInteger> oseq = getOseq(sen);
		// 维特比类需要两个参数：hmm模型(pi,A,B)，观察序列的索引集合
		ViterbiCalculator vc = new ViterbiCalculator(oseq, hmm);
		// 维特比预测隐藏状态的索引
		int[] segrs = vc.stateSequence();

		return decode(sen, segrs);
	}

	// 对观察字符进行编码，注意这里需要减一，其目的在于使下标从0开始
	private List<ObservationInteger> getOseq(String sen) throws IOException {
		List<ObservationInteger> oseq = new ArrayList<ObservationInteger>();
		for (int i = 0; i < sen.length(); i++) {
			if(CorpusFactory.observerList.get(sen.charAt(i))==null){
				System.out.println("未登录词："+sen.charAt(i));
				ParameterFactory parameterFactory=new ParameterFactory().reSetParameters(sen.charAt(i));
				hmm=new ModelFactory(parameterFactory).getHmm();
			}
			// 词典的编号从1开始。ObservationInteger构造器的参数是 观察的字符在混淆矩阵中的列索引
			oseq.add(new ObservationInteger(CorpusFactory.observerList.get(sen.charAt(i)) - 1));
		}
		return oseq;
	}

	// 对文本分词后的文本进行解码
	private String decode(String sen, int[] seqrs) {
		StringBuilder sb = new StringBuilder();
		char ch;
		for (int i = 0; i < sen.length(); i++) {
			sb.append(sen.charAt(i));
			ch = DefineParameters.getRemap().get(seqrs[i]);
			if (ch == 'E' || ch == 'S')
				sb.append(" ");
		}
		System.out.println(sb.toString());
		return  sb.toString();
	}

	public static void main(String[] args) throws IOException {
		System.out.println(CorpusFactory.observerList.size()+">>>"+CorpusFactory.observerList.toString());
		ParameterFactory factory = new ParameterFactory();
		Hmm<ObservationInteger> hmm = (new ModelFactory(factory)).getHmm();
		SegWithHMM tr = new SegWithHMM(hmm);
		tr.handleTxt("msr_test.utf8");
		System.out.println(CorpusFactory.observerList.size()+">>>"+CorpusFactory.observerList.toString());
	}
}