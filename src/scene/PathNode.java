package scene;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.List;

public class PathNode extends ShapeNode {
	
	public List<Point> points;
	
	public PathNode(List<Point> points) {
		this.points = points;
		drawBorder = false;
	}
	
	@Override
	Shape getShape() {
		Path2D path = new Path2D.Double();
		if (points.size() > 0) {
			path.moveTo(points.get(0).x, points.get(0).y);
			for (int i = 1; i < points.size(); i++) {
				path.lineTo(points.get(i).x, points.get(i).y);
			}
		}
		return path;
	}
	
}
