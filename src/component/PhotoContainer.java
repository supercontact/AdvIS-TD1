package component;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import custom.ResourceManager;
import model.AnnotatedPhoto;
import model.PhotoApplicationModel;
import model.PhotoEvent;
import model.PhotoListener;

public class PhotoContainer extends JLayeredPane implements MouseMotionListener, PhotoListener {

	private static final long serialVersionUID = 1L;
	
	public final int controlPanelOpaqueHeight = 100;
	public final int controlPanelTransparentHeight = 200;
	
	public PhotoApplicationModel model;
	
	public PhotoComponent mainPhoto;
	public JPanel photoIconWall;
	public ArrayList<PhotoIcon> photoIcons;
	public FadePanel controlPanel;
	public FadePanel controlPanelEditMode;
	
	private JScrollPane scrollPane;
	private IconWallMouseEventHandler iconWallHandler;
	
	private Image background;
	
	private Point mousePos;
	private FadePanel currentControlPanel;
	private float controlPanelAlphaMultiplier = 1;
	
	public PhotoContainer(PhotoApplicationModel model) {
		this.model = model;
		initialize();
	}
	
	private void initialize() {
		scrollPane = new JScrollPane();
		add(scrollPane, 999);
		
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setOpaque(false);
		
		background = ResourceManager.backgroundImage;
		
		photoIconWall = new JPanel();
		photoIconWall.setOpaque(false);
		photoIconWall.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		iconWallHandler = new IconWallMouseEventHandler(this, photoIconWall);
		photoIconWall.addMouseListener(iconWallHandler);
		photoIconWall.addMouseMotionListener(iconWallHandler);
		photoIcons = new ArrayList<>();
		for (AnnotatedPhoto photo : model.album.photoList) {
			PhotoIcon newIcon = new PhotoIcon(photo);
			photoIcons.add(newIcon);
			photoIconWall.add(newIcon);
		}
		
		model.addPhotoListener(this);
	}
	
	private void switchViewMode(PhotoApplicationModel.ViewMode oldMode, PhotoApplicationModel.ViewMode newMode) {
		
		// Clean old stuff
		if (oldMode == PhotoApplicationModel.ViewMode.PhotoViewer) {
			scrollPane.setViewportView(null);
			mainPhoto.deinit();
		} else if (oldMode == PhotoApplicationModel.ViewMode.Browser) {
			
		}
		
		// Set new stuff
		if (newMode == PhotoApplicationModel.ViewMode.PhotoViewer) {
			scrollPane.setWheelScrollingEnabled(false);
			scrollPane.setViewportView(mainPhoto);
			mainPhoto.init();
		} else if (newMode == PhotoApplicationModel.ViewMode.Browser) {
			scrollPane.setWheelScrollingEnabled(true);
			scrollPane.setViewportView(photoIconWall);
		}
		
		updateControlPanel();
	}
	
	public void setMainPhotoComponent(PhotoComponent mainPhoto) {
		this.mainPhoto = mainPhoto;
		mainPhoto.container = this;
		mainPhoto.addMouseMotionListener(this);
	}
	
	public void requestControlPanelHiding(float alphaMultiplier) {
		controlPanelAlphaMultiplier = alphaMultiplier;
		updateControlPanelFade();
	}
	
	@Override
	public void paintComponent(Graphics graphics) {
		paintBackground(graphics);
		
		updateScrollPane();
		updateControlPanel();
		if (model.getViewMode() == PhotoApplicationModel.ViewMode.Browser) {
			updateIconWall();
		}
	}
	
	private void paintBackground(Graphics graphics) {
		// The background stays fixed when scrolling
		int w = getWidth();
		int h = getHeight();
		int imgW = background.getWidth(null);
		int imgH = background.getHeight(null);
		if (w * imgH > h * imgW) {
			// Width is too large
			int imgH2 = h * imgW / w;
			graphics.drawImage(background, 0, 0, w, h, 0, (imgH - imgH2) / 2, imgW, (imgH + imgH2) / 2, null);
		} else {
			// Height is too large
			int imgW2 = w * imgH / h;
			graphics.drawImage(background, 0, 0, w, h, (imgW - imgW2) / 2, 0, (imgW + imgW2) / 2, imgH, null);
		}
	}
	
	private void updateScrollPane() {
		scrollPane.setLocation(new Point(0, 0));
		scrollPane.setSize(getSize().width + 2, getSize().height + 1); // To avoid the 1 pixel margin at the border
		scrollPane.revalidate();
	}
	
	private void updateControlPanel() {
		if (controlPanel == null || controlPanelEditMode == null) return;
		if (model.getViewMode() != PhotoApplicationModel.ViewMode.PhotoViewer) {
			controlPanel.setVisible(false);
			controlPanelEditMode.setVisible(false);
			currentControlPanel = null;
		} else if (!model.isFlipped() && currentControlPanel != controlPanel) {
			controlPanelEditMode.setVisible(false);
			currentControlPanel = controlPanel;
		} else if (model.isFlipped() && currentControlPanel != controlPanelEditMode) {
			controlPanel.setVisible(false);
			currentControlPanel = controlPanelEditMode;
		}
		
		if (currentControlPanel != null) {
			Dimension panelSize = currentControlPanel.getPreferredSize();
			currentControlPanel.setSize(panelSize);
			Point location = new Point();
			location.x = getWidth() / 2 - panelSize.width / 2;
			location.y = getHeight() - panelSize.height - 20;
			currentControlPanel.setLocation(location);
		}
	}

	private void updateControlPanelFade() {
		if (currentControlPanel == null) return;
		if (mousePos == null) return;
		int height = getHeight() - mousePos.y;
		if (height <= controlPanelOpaqueHeight) {
			currentControlPanel.setAlpha(controlPanelAlphaMultiplier);
			currentControlPanel.setVisible(true);
		} else if (height >= controlPanelTransparentHeight) {
			currentControlPanel.setAlpha(0);
			currentControlPanel.setVisible(false);
		} else {
			currentControlPanel.setAlpha(controlPanelAlphaMultiplier * (height - controlPanelTransparentHeight) / (controlPanelOpaqueHeight - controlPanelTransparentHeight));
			currentControlPanel.setVisible(true);
		}
		currentControlPanel.repaint();
	}
	
	private void updateIconWall() {
		int epsilon = 10;
		
		Dimension wallSize = getSize();
		wallSize.width -= epsilon + scrollPane.getVerticalScrollBar().getWidth();
		wallSize.height = 0;
		photoIconWall.setPreferredSize(wallSize);
		photoIconWall.revalidate();
		
		if (model.isShowingPhoto()) {
			PhotoIcon lastIcon = photoIcons.get(photoIcons.size() - 1);
			wallSize.height = lastIcon.getY() + lastIcon.getHeight();
			photoIconWall.setPreferredSize(wallSize);
			photoIconWall.revalidate();
		}
		photoIconWall.repaint();
	}

	

	@Override
	public void mouseMoved(MouseEvent e) {
		Point pos = getMousePosition(true);
		if (pos != null) {
			mousePos = pos;
			updateControlPanelFade();
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		Point pos = getMousePosition(true);
		if (pos != null) {
			mousePos = pos;
			updateControlPanelFade();
		}
	}

	
	private class IconWallMouseEventHandler implements MouseListener, MouseMotionListener {
		
		private PhotoContainer container;
		private JPanel iconWall;
		private PhotoIcon currentTarget;
		
		public IconWallMouseEventHandler(PhotoContainer container, JPanel iconWall) {
			this.container = container;
			this.iconWall = iconWall;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2 && currentTarget != null) {
				container.model.setViewMode(PhotoApplicationModel.ViewMode.PhotoViewer);
				container.model.setCurrentViewingIndex(currentTarget.photo.getIndex());
			}
		}
		@Override
		public void mousePressed(MouseEvent e) {
			if (currentTarget != null) {
				currentTarget.setPressed(true);
				currentTarget.setSelected(!currentTarget.isSelected());
				if (currentTarget.isSelected()) {
					model.selectPhoto(currentTarget.photo.getIndex());
				} else {
					model.deselectPhoto(currentTarget.photo.getIndex());
				}
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (currentTarget != null) {
				currentTarget.setPressed(false);
			}
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			Component targetComponent = iconWall.getComponentAt(e.getPoint());
			PhotoIcon target = (targetComponent == iconWall) ? null : (PhotoIcon)targetComponent;
			if (target != currentTarget) {
				if (currentTarget != null) {
					currentTarget.setRollover(false);
				}
				if (target != null) {
					target.setRollover(true);
				}
				currentTarget = target;
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			Component targetComponent = iconWall.getComponentAt(e.getPoint());
			PhotoIcon target = (targetComponent == iconWall) ? null : (PhotoIcon)targetComponent;
			if (target != currentTarget) {
				if (currentTarget != null) {
					currentTarget.setRollover(false);
					currentTarget.setPressed(false);
				}
				if (target != null) {
					target.setRollover(true);
					target.setPressed(true);
					target.setSelected(!target.isSelected());
					if (target.isSelected()) {
						model.selectPhoto(target.photo.getIndex());
					} else {
						model.deselectPhoto(target.photo.getIndex());
					}
				}
				currentTarget = target;
			}
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			// Do nothing
		}
		@Override
		public void mouseExited(MouseEvent e) {
			// Do nothing
		}
	}


	@Override
	public void photoEventReceived(PhotoEvent e) {
		if (e.type == PhotoEvent.Type.ViewModeChanged) {
			switchViewMode(e.oldViewMode, e.newViewMode);
			
		} else if (e.type == PhotoEvent.Type.PhotoAdded) {
			PhotoIcon newIcon = new PhotoIcon(e.photo);
			photoIcons.add(newIcon);
			photoIconWall.add(newIcon);
			updateIconWall();
			
		} else if (e.type == PhotoEvent.Type.PhotoRemoved) {
			PhotoIcon iconRemoved = null;
			for (int i = 0; i < photoIcons.size(); i++) {
				if (photoIcons.get(i).photo == e.photo) {
					iconRemoved = photoIcons.get(i);
					photoIcons.remove(i);
					break;
				}
			}
			photoIconWall.remove(iconRemoved);
			updateIconWall();
		}
	}
}
