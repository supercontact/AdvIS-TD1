package scene;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class PathNode extends ShapeNode {
	
	private static final long serialVersionUID = 1L;
	
	public List<Point> path;
	
	public PathNode() {
		path = new ArrayList<>();
		fillInside = false;
	}
	public PathNode(List<Point> points) {
		this.path = points;
		drawBorder = false;
	}
	
	@Override
	Shape getShape() {
		Path2D result = new Path2D.Double();
		if (path.size() > 0) {
			result.moveTo(path.get(0).x, path.get(0).y);
			for (int i = 1; i < path.size(); i++) {
				result.lineTo(path.get(i).x, path.get(i).y);
			}
		}
		return result;
	}
	
}
