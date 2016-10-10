package scene;

import java.awt.Point;

// A primitive node is a ShapeNode that can be controlled by 2 input points (or some other parameters).
public abstract class PrimitiveNode extends ShapeNode {
	
	private static final long serialVersionUID = 1L;
	
	public Point p1, p2;
	
	public PrimitiveNode() {}
	public PrimitiveNode(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
}
