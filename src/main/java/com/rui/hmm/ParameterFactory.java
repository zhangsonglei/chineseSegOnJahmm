package com.rui.hmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public class ParameterFactory {
	private int stateNum = DefineParameters.getMap().size();
	private int freqA[][] = new int[stateNum][stateNum];
	private int freqB[][] = new int[stateNum][(int) ((CorpusFactory.observerList.size())*1.2)];
	private int freqState[] = new int[stateNum];
	private int observeNum = freqB[0].length;
	
	private double[][] transferMatrix = new double[stateNum][stateNum];
	private double[][] mixedMatrix = new double[stateNum][(int) ((CorpusFactory.observerList.size())*1.2)];
	// M和E不可能出现在句子的首位
	private double[] Pi = { 0.5, 0.0, 0.0, 0.5 };

	// 无参构造器
	public ParameterFactory() throws IOException {
		readFile("src/main/resources/msr_training.utf8");
	}

	public ParameterFactory reSetParameters(Character unknownWords ){
		//未登录词加入观察序列
		CorpusFactory.observerList.put(unknownWords, CorpusFactory.observerList.size()+1);
		//观察态+1
		observeNum++;
		//三种计数矩阵+1,
		for(int i=0;i<stateNum;i++){
			freqState[i]++;
			for(int j=0;j<freqA[0].length;j++){
				freqA[i][j]++;
			}
			freqB[i][CorpusFactory.observerList.get(unknownWords)]++;
		}
		//再次计算两个概率矩阵
		calStatus();
		calMixed();
		return this;
	}
	
	public void readFile(String fileName) {
		BufferedReader br = null;
		String line, temp;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName)), "utf-8"));
			while ((line = br.readLine()) != null) {
				if ("".equals(line.trim()))
					continue;
				// 统计转移矩阵，不需要带原字符
				temp = encode(line, false);
				stmStatus(temp);

				// 统计混淆矩阵，需要带原字符
				temp = encode(line, true);
				stmMixed(temp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 根据freq和count矩阵来计算转移矩阵
		calStatus();
		calMixed();
	}

	// 参数：一行文本，
	private String encode(String content, boolean withContent) {
		if (content == null || "".equals(content.trim()))
			return null;

		StringBuilder sb = new StringBuilder();
		int start, end, len;
		start = end = 0;
		// len为字符的长度
		len = content.length();
		// 根据空格对文本进行分词
		while (end < len) {
			if (Character.isWhitespace(content.charAt(end))) {
				if (end > start) {
					// 得到一个词
					if (withContent)
						insertWithContent(content, sb, start, end);
					else
						insert(sb, start, end);
					// end先+1,再赋值给start
					++end;
					start = end;

				} else {
					// ？何时会出现此情况
					++start;
					++end;
				}

			} else {
				++end;
			}
		}
		// 最后的情况处理
		if (end > start) {
			if (withContent)
				insertWithContent(content, sb, start, end);
			else
				insert(sb, start, end);
		}

		return sb.toString();
	}

	// sb为""的辅助字符串变量
	private void insert(StringBuilder sb, int start, int end) {
		if (end - start > 1) {
			sb.append('B');
			for (int i = 0; i < end - start - 2; ++i) {
				sb.append('M');
			}
			sb.append('E');
		} else {
			sb.append('S');
		}
	}

	/*
	 * 带文本内容,比如：你 现在 应该 去 幼儿园 了， 输出的结果为：你S现B在E应B该E去S幼B儿M园E了S 测试时用
	 */
	private void insertWithContent(String content, StringBuilder sb, int start, int end) {
		if (end - start > 1) {
			sb.append(content.charAt(start));
			sb.append('B');
			for (int i = 0; i < end - start - 2; ++i) {
				sb.append(content.charAt(start + i + 1));
				sb.append('M');
			}
			sb.append(content.charAt(end - 1));
			sb.append('E');
		} else {
			sb.append(content.charAt(end - 1));
			sb.append('S');
		}
	}

	/*
	 * “ 一点 外语 知识 、 数理化 知识 也 没有 ， 还 攀 什么 高峰 ？ 对一段文本按BEMS规则进行编码，标点符号有两种处理方法：
	 *
	 * 1、算作单字成词。 2、直接过滤，不予考虑。 个人认为方案2比较合理，单字成词受到字出现的语境有影响，而标点符号永远是单一的。
	 * 在训练的过程中，其实用content.split("\\s{1,}");会更简单，清晰，但个人觉得 用这种方法在数据量大的情况下，性能不咋地
	 * 
	 * @param content,需要编码的文本
	 * 
	 * @param withContent,编码后的文本是否带原文
	 * 
	 * @return 编码后的文本
	 */

	/*
	 * 统计每一行编码
	 */
	private void stmStatus(String encodeStr) {
		int i, j, len;
		len = encodeStr.length();
		if (len <= 0)
			return;
		for (i = 0; i < len - 1; ++i) {
			++freqState[DefineParameters.getMap().get(encodeStr.charAt(i))];
			j = i + 1;
			++freqA[DefineParameters.getMap().get(encodeStr.charAt(i))][DefineParameters.getMap().get(encodeStr.charAt(j))];
		}
		// 数组的尾部字符一定要处理，尾部字符不存在下一个转移状态
		++freqState[DefineParameters.getMap().get(encodeStr.charAt(len - 1))];
	}

	// 计算状态转移矩阵
	private void calStatus() {
		int i, j;
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				transferMatrix[i][j] = (double) freqA[i][j] / freqState[i];
			}
		}
	}

	public double[][] getStatus() {

		return transferMatrix;
	}

	// 计算混淆矩阵
	private void calMixed() {
		int i, j;
		for (i = 0; i < 4; i++) {
			for (j = 0; j < CorpusFactory.observerList.size(); j++) {
				// 拉普拉斯平滑处理 Laplace Smoothing
				mixedMatrix[i][j] = (double) (freqB[i][j] + 1) / freqState[i];
			}
		}
	}

	// 读入训练文本

	/*
	 * 这里的话就需要两个字符两个字符一读 你S现B在E应B该E去S幼B儿M园E了S
	 */
	private void stmMixed(String encodeStr) {
		int i, j, len;
		len = encodeStr.length();
		// 有错误的句子，直接忽略，encodeStr长度一定为2的倍数
		if (len % 2 != 0)
			return;
		Integer c, o;
		for (i = 0; i < len; i += 2) {
			j = i + 1;
			c = DefineParameters.getMap().get(encodeStr.charAt(j));
			o = CorpusFactory.observerList.get(encodeStr.charAt(i));
			if (c == null || o == null) {
				// System.out.println(encodeStr.charAt(i));
				continue;
			}
			// 词典序号-1
			++freqB[c][o - 1];
		}
	}

	private void print(double[][] A) {
		int i, j;
		char[] chs = { 'B', 'M', 'E', 'S' };
		System.out.println("\t\t" + "B" + "\t\t\t" + "M" + "\t\t\t" + "E" + "\t\t\t" + "S");
		for (i = 0; i < 4; i++) {
			System.out.print(chs[i] + "\t");
			for (j = 0; j < 4; j++) {
				System.out.format("%.12f\t\t", A[i][j]);

			}
			System.out.println();
		}
	}

	public static HashMap<Character, Integer> getMap() {
		return DefineParameters.getMap();
	}

	public static HashMap<Integer, Character> getRemap() {
		return DefineParameters.getRemap();
	}

	public int[][] getFreqA() {
		return freqA;
	}

	public int[][] getFreqB() {
		return freqB;
	}

	public int[] getFreqState() {
		return freqState;
	}

	public double[][] getTransferMatrix() {
		return transferMatrix;
	}

	public double[][] getMixedMatrix() {
		return mixedMatrix;
	}

	public double[] getPi() {
		return Pi;
	}

	public int getStatenum() {
		return stateNum;
	}

	public int getObserveNum() {
		return observeNum;
	}

	public static void main(String[] args) throws IOException {
		/*
		 * [0.0, 0.16923376752510688, 0.8307662324748931, 0.0] [0.0,
		 * 0.4800079191077805, 0.5199920808922195, 0.0] [0.5825681614795665,
		 * 0.0, 0.0, 0.3602418216167843] [0.6182595316351136, 0.0, 0.0,
		 * 0.3617050520902141]
		 */
		ParameterFactory tr = new ParameterFactory();
		for (int i = 0; i < tr.transferMatrix.length; i++) {

			System.out.println(Arrays.toString(tr.transferMatrix[i]));
		}
	}

}
