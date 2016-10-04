import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PhotoApplication extends JFrame {

    private static final long serialVersionUID = 1L;
    
    public static PhotoApplication app;
    
    public AnnotatedAlbum album;
    
    JLabel status;
    JMenuBar menuBar;
    JMenu fileMenu, viewMenu;
    JMenuItem importItem, deleteItem, clearItem, quitItem;
    JMenuItem photoItem, browserItem, splitItem;
    JCheckBoxMenuItem scaleSmallImageItem;
    JMenuItem originalSizeItem, fitWindowItem, fitWidthItem, fitHeightItem;
    JToolBar tool;
    ArrayList<JToggleButton> categories; 
    PhotoContainer photoContainer;
    PhotoComponent photoComponent;
    FadePanel controlPanel, controlPanelEditMode;
    JButton prev, next;
    ImageIcon prevIcon, nextIcon;
    JToggleButton toggleStroke, toggleStraightLine, toggleRectangle, toggleEllipse;
    ImageIcon toggleStrokeIcon, toggleStraightLineIcon, toggleRectangleIcon, toggleEllipseIcon;
    ButtonGroup toggleDrawingGroup;
    JButton setColor;
    BufferedImage originalColorImage;
    ImageIcon colorIcon;
    JSlider setStrokeWidth, setTextSize;
    JLabel setStrokeWidthLabel, setTextSizeLabel;
    ImageIcon setStrokeWidthIcon, setTextSizeIcon;
    JComboBox<String> setFont;

    public static void main(String[] args) {
    	SavedSettings.loadSettings();
    	new PhotoApplication();
    }
   
    public PhotoApplication() {
        super("PhotoX");
        
        app = this;
        
        album = AnnotatedAlbum.loadAlbum();
        if (album == null) {
        	album = new AnnotatedAlbum();
        }
        album.loadAllPhotos();
        
        ResourceManager.loadResources();
        loadIcons();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        setPreferredSize(new Dimension(1024, 768));
        setMinimumSize(new Dimension(300, 400));
        setVisible(true);
       
        setupMenuBar();
        setupToolBar();
        setupMainArea();
        setupControlPanel();
        
        //photoComponent.init();
       
        pack();
    }
    
    private void loadIcons() {
    	prevIcon = new ImageIcon(ResourceManager.prevIcon);
    	nextIcon = new ImageIcon(ResourceManager.nextIcon);
    	toggleStrokeIcon = new ImageIcon(ResourceManager.lineIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
    	toggleStraightLineIcon = new ImageIcon(ResourceManager.straightLineIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
    	toggleRectangleIcon = new ImageIcon(ResourceManager.rectangleIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
    	toggleEllipseIcon = new ImageIcon(ResourceManager.ellipseIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
    	setStrokeWidthIcon = new ImageIcon(ResourceManager.lineWidthIcon.getScaledInstance(30, 30, Image.SCALE_SMOOTH));
    	setTextSizeIcon = new ImageIcon(ResourceManager.textSizeIcon.getScaledInstance(30, 30, Image.SCALE_SMOOTH));
    	originalColorImage = ResourceManager.colorIcon;
    	colorIcon = new ImageIcon(ResourceManager.cloneImage(ResourceManager.colorIcon));
    }
    
    private void setupMenuBar() {
    	menuBar = new JMenuBar();
    	setJMenuBar(menuBar);
        
    	fileMenu = new JMenu("File");
        viewMenu = new JMenu("View");
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
       
        importItem = new JMenuItem("Import");
        deleteItem = new JMenuItem("Delete");
        clearItem = new JMenuItem("Clean");
        quitItem = new JMenuItem("Quit");
        fileMenu.add(importItem);
        fileMenu.add(deleteItem);
        fileMenu.add(clearItem);
        fileMenu.add(quitItem);
        
        importItem.addActionListener(
        		event -> importImages()
        );
        deleteItem.addActionListener(
        		event -> {
        			if (photoComponent.deleteCurrentPhoto()) {
        				showStatusText("Photo removed (The original file is still there).");
        			} else {
        				showStatusText("No photo to remove!");
        			}
        		}
        );
        clearItem.addActionListener(
        		event -> {
        			if (photoComponent.clearCurrentPhoto()) {
        				showStatusText("All annotations and strokes are removed from the photo.");
        			} else {
        				showStatusText("No photo to clean!");
        			}
        		}
        );
        quitItem.addActionListener(
        		event -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING))
        );
       
        photoItem = new JMenuItem("Photo viewer");
        browserItem = new JMenuItem("Browser");
        splitItem = new JMenuItem("Split mode");
        scaleSmallImageItem = new JCheckBoxMenuItem("Scale small photos");
        originalSizeItem = new JMenuItem("Original size (100%)");
        fitWindowItem = new JMenuItem("Fit to window");
        fitWidthItem = new JMenuItem("Fit to width");
        fitHeightItem = new JMenuItem("Fit to height");
        viewMenu.add(photoItem);
        viewMenu.add(browserItem);
        viewMenu.add(splitItem);
        viewMenu.add(new JSeparator());
        viewMenu.add(scaleSmallImageItem);
        viewMenu.add(new JSeparator());
        viewMenu.add(originalSizeItem);
        viewMenu.add(fitWindowItem);
        viewMenu.add(fitWidthItem);
        viewMenu.add(fitHeightItem);
        
        photoItem.addActionListener(
        		event -> {
        			photoContainer.switchViewMode(PhotoContainer.ViewMode.PhotoViewer);
        			showStatusText("Switched to photo viewer mode.");
        		}
        );
        browserItem.addActionListener(
        		event -> {
        			photoContainer.switchViewMode(PhotoContainer.ViewMode.Browser);
        			showStatusText("Switched to browser mode.");
        		}
        );
        splitItem.addActionListener(
        		event -> showStatusText("Switched to split mode (Not yet supported).")
        );
        scaleSmallImageItem.addActionListener(
        		event -> {
        			photoComponent.scaleSmallPhoto = scaleSmallImageItem.isSelected();
        			photoComponent.fitPhoto();
        		}
        		
        );
        originalSizeItem.addActionListener(
        		event -> photoComponent.setScaleMode(PhotoComponent.ScaleMode.OriginalSize)
        );
        fitWindowItem.addActionListener(
        		event -> photoComponent.setScaleMode(PhotoComponent.ScaleMode.FitWindow)
        );
        fitWidthItem.addActionListener(
        		event -> photoComponent.setScaleMode(PhotoComponent.ScaleMode.FitWidth)
        );
        fitHeightItem.addActionListener(
        		event -> photoComponent.setScaleMode(PhotoComponent.ScaleMode.FitHeight)
        );
        
    }
    
    private void setupToolBar() {
    	tool = new JToolBar();
    	add(tool, BorderLayout.NORTH);
    	
    	categories = new ArrayList<>(); 
    	categories.add(new JToggleButton("Family"));
        categories.add(new JToggleButton("Vacation"));
        categories.add(new JToggleButton("School"));
        
        for (JToggleButton button : categories) {
        	button.setMinimumSize(new Dimension(80, 30));
        	button.setPreferredSize(new Dimension(80, 30));
        	button.setMaximumSize(new Dimension(80, 30));
        	tool.add(button);
        	 
        	button.addItemListener(
        			event -> {
        				if (event.getStateChange() == ItemEvent.SELECTED) {
        					showStatusText("Catogory " + ((JToggleButton)event.getItem()).getText() + " is selected (Not yet supported).");
        				} else {
        					showStatusText("Catogory " + ((JToggleButton)event.getItem()).getText() + " is deselected (Not yet supported).");
        				}
        			}
        	);
        }
    }
    
    private void setupMainArea() {
    	photoContainer = new PhotoContainer();
        photoComponent = new PhotoComponent();
    	add(photoContainer, BorderLayout.CENTER);
        photoContainer.setMainPhotoComponent(photoComponent);
        
        photoContainer.switchViewMode(PhotoContainer.ViewMode.Browser);
    	
    	status = new JLabel();
        add(status, BorderLayout.SOUTH);
       
        showStatusText("Status");
    }
    
    private void setupControlPanel() {
    	Insets normalInsets = new Insets(0, 10, 0, 10);
    	Insets zeroInsets = new Insets(0, 0, 0, 0);
    	
    	controlPanel = new FadePanel();
        controlPanelEditMode = new FadePanel();
    	photoContainer.add(controlPanel, 0);
    	photoContainer.add(controlPanelEditMode, 1);
        photoContainer.controlPanel = controlPanel;
        photoContainer.controlPanelEditMode = controlPanelEditMode;
        
        prev = new JButton();
        next = new JButton();
        controlPanel.add(prev);
        controlPanel.add(next);
        controlPanel.setOpaque(false);
        
        prev.setIcon(prevIcon);
        prev.setMargin(normalInsets);
        prev.addActionListener(
        		event -> photoComponent.prevPhoto()
        );
        next.setIcon(nextIcon);
        next.setMargin(normalInsets);
        next.addActionListener(
        		event -> photoComponent.nextPhoto()
        );
        
        toggleStroke = new JToggleButton();
        toggleStraightLine = new JToggleButton();
        toggleRectangle = new JToggleButton();
        toggleEllipse = new JToggleButton();
        setColor = new JButton();
        setStrokeWidthLabel = new JLabel();
        setStrokeWidth = new JSlider();
        setTextSizeLabel = new JLabel();
        setTextSize = new JSlider();
        setFont = new JComboBox<String>(GlobalSettings.fontStrings);
        controlPanelEditMode.setOpaque(false);
        controlPanelEditMode.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        controlPanelEditMode.add(toggleStroke);
        controlPanelEditMode.add(toggleStraightLine);
        controlPanelEditMode.add(toggleRectangle);
        controlPanelEditMode.add(toggleEllipse);
        controlPanelEditMode.add(new JLabel("    "));
        controlPanelEditMode.add(setColor);
        controlPanelEditMode.add(new JLabel("   "));
        controlPanelEditMode.add(setStrokeWidthLabel);
        controlPanelEditMode.add(setStrokeWidth);
        controlPanelEditMode.add(new JLabel("   "));
        controlPanelEditMode.add(setTextSizeLabel);
        controlPanelEditMode.add(setTextSize);
        controlPanelEditMode.add(new JLabel("   "));
        controlPanelEditMode.add(setFont);
        
        toggleDrawingGroup = new ButtonGroup();
        toggleDrawingGroup.add(toggleStroke);
        toggleDrawingGroup.add(toggleStraightLine);
        toggleDrawingGroup.add(toggleRectangle);
        toggleDrawingGroup.add(toggleEllipse);
        
        toggleStroke.setSelected(true);
        toggleStroke.setMargin(zeroInsets);
        toggleStroke.setIcon(toggleStrokeIcon);
        toggleStroke.addActionListener(
        		event -> photoComponent.isCreatingPrimitive = false
        );
        toggleStraightLine.setIcon(toggleStraightLineIcon);
        toggleStraightLine.setMargin(zeroInsets);
        toggleStraightLine.addActionListener(
        		event -> {
        			photoComponent.isCreatingPrimitive = true;
        			photoComponent.currentPrimitiveType = AnnotatedPhoto.PrimitiveMark.Type.StraightLine;
        		}
        );
        toggleRectangle.setIcon(toggleRectangleIcon);
        toggleRectangle.setMargin(zeroInsets);
        toggleRectangle.addActionListener(
        		event -> {
        			photoComponent.isCreatingPrimitive = true;
        			photoComponent.currentPrimitiveType = AnnotatedPhoto.PrimitiveMark.Type.Rectangle;
        		}
        );
        toggleEllipse.setIcon(toggleEllipseIcon);
        toggleEllipse.setMargin(zeroInsets);
        toggleEllipse.addActionListener(
        		event -> {
        			photoComponent.isCreatingPrimitive = true;
        			photoComponent.currentPrimitiveType = AnnotatedPhoto.PrimitiveMark.Type.Ellipse;
        		}
        );
        
        setStrokeWidthLabel.setIcon(setStrokeWidthIcon);
        setStrokeWidth.setOpaque(false);
        setStrokeWidth.setMinimum(1);
        setStrokeWidth.setMaximum(30);
        setStrokeWidth.setValue(5);
        setStrokeWidth.setPreferredSize(new Dimension(150, 30));
        setStrokeWidth.addChangeListener(
        		event -> photoComponent.currentStrokeWidth = setStrokeWidth.getValue()
        );
        
        setTextSizeLabel.setIcon(setTextSizeIcon);
        setTextSize.setOpaque(false);
        setTextSize.setMinimum(5);
        setTextSize.setMaximum(100);
        setTextSize.setValue(15);
        setTextSize.setPreferredSize(new Dimension(150, 30));
        setTextSize.addChangeListener(
        		event -> photoComponent.currentTextSize = setTextSize.getValue()
        );
        
        setColor.setMargin(zeroInsets);
        setColor.setIcon(colorIcon);
        setColor.addActionListener(
        		event -> {
        			Color chosenColor = JColorChooser.showDialog(this, "Choose stroke and text color", photoComponent.currentColor);
        			if (chosenColor != null) {
        				photoComponent.currentColor = chosenColor;
        				colorImage(photoComponent.currentColor, originalColorImage ,(BufferedImage)colorIcon.getImage());
        			}
        		}
        );
        colorImage(new Color(0x19, 0x2C, 0x3C), originalColorImage ,(BufferedImage)colorIcon.getImage());
        
        setFont.addActionListener(
        		event -> photoComponent.currentFontName = (String)setFont.getSelectedItem()
        );
    }
    
    private void colorImage(Color c, BufferedImage oldImg, BufferedImage newImg) {
    	for (int x = 0; x < newImg.getWidth(); x++) {
    		for (int y = 0; y < newImg.getHeight(); y++) {
    			int rgb = oldImg.getRGB(x, y);
    			int b = Integer.remainderUnsigned(rgb, 0x100) ;
    			int g = (rgb >>> 8) % 0x100;
    			int r = (rgb >>> 16) % 0x100;
    			int a = rgb >>> 24;
    			int newRgb = (a << 24) + ((r * c.getRed() / 255) << 16) + ((g * c.getGreen() / 255) << 8) + (b * c.getBlue() / 255);
    			newImg.setRGB(x, y, newRgb);
    		}
    	}
    }
	
	public void importImages() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(SavedSettings.settings.defaultFileLocation);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
		
		int state = fileChooser.showOpenDialog(this);
		if (state == JFileChooser.CANCEL_OPTION) {
			showStatusText("Operation canceled!");
			return;
		}
		if (state == JFileChooser.ERROR_OPTION) {
			showStatusText("An error happened!");
			return;
		}
		File[] files = fileChooser.getSelectedFiles();
		photoComponent.addPhotos(files);
		SavedSettings.settings.defaultFileLocation = files[0].getParentFile();
		SavedSettings.saveSettings();
		showStatusText(files.length + " photo(s) are selected. Showing the first photo.");
	}
	
	public static void showStatusText(String text) {
		app.status.setText(text);
	}

}