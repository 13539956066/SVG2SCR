package com.gelakinetic.SVG2SCR;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transform {
	String type = null;
	double params[] = null;

	public Transform(String transform) {
		if (transform != null) {
			String pattern = "([a-z]+)\\(([,0-9\\.-]+)\\)";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher(transform);
			if (m.find()) {
				type = m.group(1);
				String paramsS[] = m.group(2).split(",");
				params = new double[paramsS.length];
				for (int i = 0; i < paramsS.length; i++) {
					params[i] = Double.parseDouble(paramsS[i]);
				}
			}
		}
	}
}
