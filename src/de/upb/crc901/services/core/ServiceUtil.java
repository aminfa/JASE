/**
 * ServiceUtil.java
 * Copyright (C) 2017 Paderborn University, Germany
 * 
 * @author: Felix Mohr (mail@felixmohr.de)
 */

/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.upb.crc901.services.core;

import java.util.HashMap;
import java.util.Map;

import de.upb.crc901.configurationsetting.compositiondomain.CompositionDomain;
import de.upb.crc901.configurationsetting.operation.ConfigurationState;
import de.upb.crc901.configurationsetting.operation.Operation;
import de.upb.crc901.configurationsetting.operation.OperationInvocation;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.VariableParam;

public class ServiceUtil {

	public static OperationInvocation getOperationInvocation(String serviceCall, Object... inputs) {
		return getOperationInvocation(serviceCall, getObjectInputMap(inputs));
	}

	public static OperationInvocation getOperationInvocation(String serviceCall, Map<String, ? extends Object> inputs) {

		/* first create operation */
		CompositionDomain emptyDomain = new CompositionDomain();
		ConfigurationState precond = new ConfigurationState();
		inputs.keySet().stream().forEach(v -> precond.addVariableParam(new VariableParam(v)));
		ConfigurationState effect = new ConfigurationState();
		effect.addVariableParam(new VariableParam("out"));
		Operation op = new Operation(serviceCall, precond, effect, emptyDomain.getNFModule().newEmptyVector());

		/* creat input and output mapping */
		Map<VariableParam, LiteralParam> inputMap = new HashMap<>();
		for (VariableParam input : op.getInputParameters()) {
			inputMap.put(input, input);
		}
		Map<VariableParam, VariableParam> outputMap = new HashMap<>();
		VariableParam outputParam = op.getOutputParameters().get(0);
		outputMap.put(outputParam, new VariableParam("out"));
		return new OperationInvocation(op, inputMap, outputMap);
	}

	public static Map<String, Object> getObjectInputMap(Object[] objects) {
		Map<String, Object> inputs = new HashMap<>();
		int i = 1;
		for (Object o : objects) {
			inputs.put("i" + (i++), o);
		}
		return inputs;
	}
}
