package scene;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;

public class StraightLineNode extends ShapeNode {
	
	public Point p1, p2;
	
	public StraightLineNode(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	@Override
	Shape getShape() {
		return new Line2D.Double(p1.x, p1.y, p2.x, p2.y);
	}
	
}
