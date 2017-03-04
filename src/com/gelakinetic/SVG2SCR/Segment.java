package com.gelakinetic.SVG2SCR;

public class Segment {

	char instruction;
	double x;
	double y;

	public Segment(char instruction, double x, double y) {
		this.x = x;
		this.y = y;
		this.instruction = instruction;
	}

	public Segment(Segment segment) {
		this.x = segment.x;
		this.y = segment.y;
	}

	public Segment applyTransform(Transform transform) {
		Segment newSeg = new Segment(this);
		if (null != transform.type) {
			switch (transform.type) {
			case "translate":
				newSeg.x += transform.params[0];
				newSeg.y += transform.params[1];
				break;
			case "matrix":
				double newX = (transform.params[0] * newSeg.x) + (transform.params[2] * newSeg.y) + transform.params[4];
				double newY = (transform.params[1] * newSeg.x) + (transform.params[3] * newSeg.y) + transform.params[5];
				newSeg.x = newX;
				newSeg.y = newY;
				break;
			}
		}
		return newSeg;
	}
	
	public String toString() {
		return String.format("(%.04f %.04f) ", x, y);
	}

}
