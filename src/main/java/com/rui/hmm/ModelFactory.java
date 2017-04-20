package com.rui.hmm;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;

public class ModelFactory {
	private ParameterFactory parameters;

	private Hmm<ObservationInteger> hmm;

	public ModelFactory(ParameterFactory parameterFactory) {
		this.parameters = parameterFactory;
		this.hmm=buildHMM();
	}

	// 训练hmm模型
	public Hmm<ObservationInteger> buildHMM() {
		// Hmm构造器，参数：隐藏状态数，观察状态工厂????转型问题
		Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(parameters.getStatenum(),
				new OpdfIntegerFactory(parameters.getObserveNum()));
		int i, j;
		// set pi
		for (i = 0; i < 4; i++) {
			hmm.setPi(i, parameters.getPi()[i]);
		}
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				// set MatrixA
				hmm.setAij(i, j, parameters.getTransferMatrix()[i][j]);
			}
			// set MatrixB
			hmm.setOpdf(i, new OpdfInteger(parameters.getMixedMatrix()[i]));
		}
		return hmm;
	}

	public Hmm<ObservationInteger> getHmm() {
		return hmm;
	}

}
