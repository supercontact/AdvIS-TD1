package scene;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Stack;

public class Context {
	
	public Graphics2D graphics;
	
	public boolean showBounds = false; // Debug option
	
	private Stack<ContextState> stack;	
	
	public Context(Graphics2D graphics) {
		this.graphics = graphics;
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		stack = new Stack<>();
		ContextState defaultState = new ContextState();
		defaultState.transform = graphics.getTransform();
		defaultState.strokeWidth = 1f;
		defaultState.strokeColor = Color.BLACK;
		defaultState.fillColor = Color.BLACK;
		defaultState.textColor = Color.BLACK;
		defaultState.textSize = 16;
		defaultState.textStyle = Font.PLAIN;
		defaultState.font = Font.SANS_SERIF;
		defaultState.alpha = 1;
		defaultState.clip = graphics.getTransform().createTransformedShape(graphics.getClip());
		stack.push(defaultState);
	}
	
	// Begin a new state
	public void newState() {
		stack.push(currentState().Clone());
	}
	// Revert to the last state
	public void revertState() {
		if (stack.size() > 1) stack.pop();
	}
	
	private ContextState currentState() {
		return stack.peek();
	}
	
	// Context info and modification
	public AffineTransform getTransform() {
		return currentState().transform;
	}
	public void concatenateTransform(AffineTransform transform) {
		AffineTransform oldTransform = getTransform();
		AffineTransform newTransform = new AffineTransform(oldTransform);
		newTransform.concatenate(transform);
		currentState().transform = newTransform;
	}
	
	public float getStrokeWidth() {
		return currentState().strokeWidth;
	}
	public void setStrokeWidth(float strokeWidth) {
		currentState().strokeWidth = strokeWidth;
	}
	
	public Color getStrokeColor() {
		return currentState().strokeColor;
	}
	public void setStrokeColor(Color strokeColor) {
		currentState().strokeColor = strokeColor;
	}
	
	public Color getFillColor() {
		return currentState().fillColor;
	}
	public void setFillColor(Color fillColor) {
		currentState().fillColor = fillColor;
	}
	
	public Color getTextColor() {
		return currentState().textColor;
	}
	public void setTextColor(Color textColor) {
		currentState().textColor = textColor;
	}
	
	public int getTextSize() {
		return currentState().textSize;
	}
	public void setTextSize(int textSize) {
		currentState().textSize = textSize;
	}
	
	public int getTextStyle() {
		return currentState().textStyle;
	}
	public void setTextStyle(int textStyle) {
		currentState().textStyle = textStyle;
	}
	
	public String getFont() {
		return currentState().font;
	}
	public void setFont(String font) {
		currentState().font = font;
	}
	
	public float getAlpha() {
		return currentState().alpha;
	}
	public void combineAlpha(float alpha) {
		currentState().alpha = getAlpha() * alpha;
	}
	
	public Shape getClip() {
		return currentState().clip;
	}
	public void combineClip(Shape clip) {
		// Please call this function after the transform is set.
		graphics.setClip(getClip());
		graphics.clip(getTransform().createTransformedShape(clip));
		currentState().clip = graphics.getClip();
	}
	
	// Apply the context (which updates the Graphics2D)
	public void apply() {
		graphics.setTransform(getTransform());
		graphics.setStroke(new BasicStroke(getStrokeWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.setFont(new Font(getFont(), getTextStyle(), getTextSize()));
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
		if (getClip() != null) {
			try {
				graphics.setClip(getTransform().createInverse().createTransformedShape(getClip()));
			} catch (NoninvertibleTransformException e) {
				graphics.setClip(null);
			}
		} else {
			graphics.setClip(null);
		}
	}
	// Switch to specific color
	public void beginStroke() {
		graphics.setColor(getStrokeColor());
	}
	public void beginFill() {
		graphics.setColor(getFillColor());
	}
	public void beginText() {
		graphics.setColor(getTextColor());
	}
	
	// A class that represent a saved state of the graphical context.
	private static class ContextState {
		public AffineTransform transform;
		public float strokeWidth;
		public Color strokeColor;
		public Color fillColor;
		public Color textColor;
		public int textSize;
		public int textStyle;
		public String font;
		public float alpha;
		public Shape clip;
		
		public ContextState Clone() {
			ContextState newState = new ContextState();
			newState.transform = transform;
			newState.strokeWidth = strokeWidth;
			newState.strokeColor = strokeColor;
			newState.fillColor = fillColor;
			newState.textColor = textColor;
			newState.textSize = textSize;
			newState.textStyle = textStyle;
			newState.font = font;
			newState.alpha = alpha;
			newState.clip = clip;
			return newState;
		}
	}
}
