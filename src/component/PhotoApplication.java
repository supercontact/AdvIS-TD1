package component;
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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import custom.GlobalSettings;
import custom.ResourceManager;
import custom.SavedSettings;
import model.PhotoApplicationModel;
import model.PhotoEvent;
import model.PhotoListener;
import util.ImageProcessing;

// The main photo browser application
public class PhotoApplication extends JFrame implements PhotoListener {

    private static final long serialVersionUID = 1L;
    
    public static PhotoApplication app;
    
    public PhotoApplicationModel model;
    
    JLabel status;
    JMenuBar menuBar;
    JMenu fileMenu, viewMenu;
    JMenuItem importItem, deleteItem, clearItem, quitItem;
    JMenuItem photoItem, browserItem, splitItem;
    JCheckBoxMenuItem scaleSmallImageItem;
    JMenuItem originalSizeItem, fitWindowItem, fitWidthItem, fitHeightItem;
    JToolBar tool;
    JButton manageTags;
    ArrayList<JToggleButton> tagsToggle;
    JDialog tagMenu;
    JScrollPane tagMenuScrollPane;
    JPanel tagMenuContent;
    ArrayList<TagOption> tagOptions;
    JButton addTag;
    PhotoContainer photoContainer;
    PhotoComponent photoComponent;
    FadePanel controlPanel, controlPanelEditMode;
    JButton prev, next, returnToBrowser;
    ImageIcon prevIcon, nextIcon, returnIcon;
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
        
        ResourceManager.loadResources();
        loadIcons();
        
        model = new PhotoApplicationModel();
        model.setAlbumLocation(GlobalSettings.savedAlbumLocation);
        model.setErrorImage(ResourceManager.errorImage);
        model.setErrorImageThumbnail(ResourceManager.errorImageThumbnail);
        model.setThumbnailSize(GlobalSettings.thumbnailSize);
        model.loadAlbum();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        setPreferredSize(new Dimension(1114, 907));
        setMinimumSize(new Dimension(300, 400));
        setVisible(true);
       
        setupMenuBar();
        setupToolBar();
        setupMainArea();
        setupControlPanel();
       
        pack();
        
        model.addPhotoListener(this);
        model.setViewMode(PhotoApplicationModel.ViewMode.Browser);
    }
    
    private void loadIcons() {
    	prevIcon = new ImageIcon(ResourceManager.prevIcon);
    	nextIcon = new ImageIcon(ResourceManager.nextIcon);
    	returnIcon = new ImageIcon(ResourceManager.returnIcon);
    	toggleStrokeIcon = new ImageIcon(ResourceManager.lineIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
    	toggleStraightLineIcon = new ImageIcon(ResourceManager.straightLineIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
    	toggleRectangleIcon = new ImageIcon(ResourceManager.rectangleIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
    	toggleEllipseIcon = new ImageIcon(ResourceManager.ellipseIcon.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
    	setStrokeWidthIcon = new ImageIcon(ResourceManager.lineWidthIcon.getScaledInstance(30, 30, Image.SCALE_SMOOTH));
    	setTextSizeIcon = new ImageIcon(ResourceManager.textSizeIcon.getScaledInstance(30, 30, Image.SCALE_SMOOTH));
    	originalColorImage = ResourceManager.colorIcon;
    	colorIcon = new ImageIcon(ImageProcessing.cloneImage(ResourceManager.colorIcon));
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
        		event -> deleteImages()
        );
        clearItem.addActionListener(
        		event -> clearImages()
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
        			model.setViewMode(PhotoApplicationModel.ViewMode.PhotoViewer);
        			showStatusText("Switched to photo viewer mode.");
        		}
        );
        browserItem.addActionListener(
        		event -> {
        			model.setViewMode(PhotoApplicationModel.ViewMode.Browser);
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
    	
    	manageTags = new JButton("+Tags");
    	manageTags.setMinimumSize(new Dimension(50, 30));
    	manageTags.setPreferredSize(new Dimension(50, 30));
    	manageTags.setMaximumSize(new Dimension(50, 30));
    	manageTags.setOpaque(false);
    	manageTags.addActionListener(
    			event -> showTagMenu()
    	);
    	
    	tool.add(manageTags);
    	
    	tagsToggle = new ArrayList<>(); 
    	tagOptions = new ArrayList<>();
    	for (String tag: model.album.tags) {
	    	tagsToggle.add(new JToggleButton(tag));
	    	tagOptions.add(new TagOption(tag));
    	}
        
    	setupToolbarTags();
        
        tagMenu = new JDialog();
        tagMenu.setTitle("Manage Tags");
        tagMenu.setPreferredSize(new Dimension(340, 500));
        tagMenu.setMinimumSize(new Dimension(340, 100));
        tagMenu.setMaximumSize(new Dimension(340, Integer.MAX_VALUE));
        
        tagMenuContent = new JPanel();
        BoxLayout columnLayout = new BoxLayout(tagMenuContent, BoxLayout.Y_AXIS);
        tagMenuContent.setLayout(columnLayout);
        tagMenuContent.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        
        tagMenuScrollPane = new JScrollPane(tagMenuContent);
        tagMenuScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tagMenuScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tagMenu.add(tagMenuScrollPane);
        
        addTag = new JButton("Add new tag");
        addTag.setPreferredSize(new Dimension(300, 30));
        addTag.setMinimumSize(new Dimension(300, 30));
        addTag.setMaximumSize(new Dimension(300, 30));
        addTag.addActionListener(
        		event -> addNewTag()
        );
        tagMenuContent.add(addTag);
        
        setupTagMenuOptions();
        tagMenu.pack();
    }
    
    private void setupToolbarTags() {
    	while (tool.getComponentCount() > 1) {
    		tool.remove(1);
    	}
    	
    	for (JToggleButton button : tagsToggle) {
        	button.setMinimumSize(new Dimension(80, 30));
        	button.setPreferredSize(new Dimension(80, 30));
        	button.setMaximumSize(new Dimension(80, 30));
        	tool.add(button);
        	 
        	button.addItemListener(
        			event -> {
        				String tag = ((JToggleButton)event.getItem()).getText();
        				if (event.getStateChange() == ItemEvent.SELECTED) {
        					model.selectTag(tag);
        					showStatusText("Catogory " + tag + " is selected.");
        				} else {
        					model.deselectTag(tag);
        					showStatusText("Catogory " + tag + " is deselected.");
        				}
        			}
        	);
        }
    	tool.revalidate();
    	tool.repaint();
    }
    
    private void setupTagMenuOptions() {
    	while (tagMenuContent.getComponentCount() > 1) {
    		tagMenuContent.remove(0);
    	}
    	
    	for (TagOption tagOption: tagOptions) {
    		tagOption.setAlignmentX(0);
    		tagMenuContent.add(tagOption, tagMenuContent.getComponentCount() - 1);
        }
    	tagMenuContent.revalidate();
    	tagMenuContent.repaint();
    }
    
    private void setupMainArea() {
    	photoContainer = new PhotoContainer(model);
        photoComponent = new PhotoComponent(model);
    	add(photoContainer, BorderLayout.CENTER);
        photoContainer.setMainPhotoComponent(photoComponent);
    	
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
        returnToBrowser = new JButton();
        controlPanel.add(prev);
        controlPanel.add(returnToBrowser);
        controlPanel.add(next);
        controlPanel.setOpaque(false);
        
        prev.setIcon(prevIcon);
        prev.setMargin(normalInsets);
        prev.addActionListener(
        		event -> model.prevPhoto()
        );
        next.setIcon(nextIcon);
        next.setMargin(normalInsets);
        next.addActionListener(
        		event -> model.nextPhoto()
        );
        returnToBrowser.setIcon(returnIcon);
        returnToBrowser.setMargin(zeroInsets);
        returnToBrowser.addActionListener(
        		event -> model.setViewMode(PhotoApplicationModel.ViewMode.Browser)
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
        			photoComponent.currentPrimitiveType = PhotoComponent.PrimitiveType.StraightLine;
        		}
        );
        toggleRectangle.setIcon(toggleRectangleIcon);
        toggleRectangle.setMargin(zeroInsets);
        toggleRectangle.addActionListener(
        		event -> {
        			photoComponent.isCreatingPrimitive = true;
        			photoComponent.currentPrimitiveType = PhotoComponent.PrimitiveType.Rectangle;
        		}
        );
        toggleEllipse.setIcon(toggleEllipseIcon);
        toggleEllipse.setMargin(zeroInsets);
        toggleEllipse.addActionListener(
        		event -> {
        			photoComponent.isCreatingPrimitive = true;
        			photoComponent.currentPrimitiveType = PhotoComponent.PrimitiveType.Ellipse;
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
        				ImageProcessing.colorImage(photoComponent.currentColor, originalColorImage ,(BufferedImage)colorIcon.getImage());
        			}
        		}
        );
        ImageProcessing.colorImage(new Color(0x19, 0x2C, 0x3C), originalColorImage ,(BufferedImage)colorIcon.getImage());
        
        setFont.addActionListener(
        		event -> photoComponent.currentFontName = (String)setFont.getSelectedItem()
        );
    }
    
	
    // Button callback
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
		model.addPhotos(files);
		SavedSettings.settings.defaultFileLocation = files[0].getParentFile();
		SavedSettings.saveSettings();
		showStatusText(files.length + " photo(s) are selected. Showing the first photo.");
	}
	
	public void deleteImages() {
		if (model.isShowingPhoto()) {
			if (model.getViewMode() == PhotoApplicationModel.ViewMode.PhotoViewer) {
				int selection = JOptionPane.showOptionDialog(this, 
						"Do you really want to remove this photo from your collection?",
					    "Delete Images",
					    JOptionPane.YES_NO_CANCEL_OPTION,
					    JOptionPane.WARNING_MESSAGE,
					    null, null, null);
				if (selection == JOptionPane.YES_OPTION) {
					model.deletePhoto();
					showStatusText("Current photo removed (The original file is still there).");
				}
			} else if (model.getViewMode() == PhotoApplicationModel.ViewMode.Browser) {
				int selection = JOptionPane.showOptionDialog(this, 
						"Do you really want to remove all the selected photos from your collection?",
					    "Delete Images",
					    JOptionPane.YES_NO_CANCEL_OPTION,
					    JOptionPane.WARNING_MESSAGE,
					    null, null, null);
				if (selection == JOptionPane.YES_OPTION) {
					model.deleteSelectedPhotos();
					showStatusText("Selected photos removed (The original files are still there).");
				}
			}
		} else {
			showStatusText("No photo to remove!");
		}
	}
	
	public void clearImages() {
		if (model.isShowingPhoto()) {
			int selection = JOptionPane.showOptionDialog(this, 
					"Do you really want to remove all the annotations from this photo?",
				    "Clean Images",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.WARNING_MESSAGE,
				    null, null, null);
			if (selection == JOptionPane.YES_OPTION) {
				model.clearPhoto();
				showStatusText("All annotations and strokes are removed from the photo.");
			}
		} else {
			showStatusText("No photo to clean!");
		}
	}
	
	public void showTagMenu() {
		tagMenu.setVisible(true);
		for (TagOption option : tagOptions) {
			option.setRelatedPhotos(model.selectedPhotos);
		}
	}
	
	public void addNewTag() {
		int i = 1;
		while (model.album.tags.contains("New Tag #" + i)) {
			i++;
		}
		String name = "New Tag #" + i;
		model.createNewTag(name);
		tagsToggle.add(new JToggleButton(name));
		TagOption newOption = new TagOption(name);
		newOption.setRelatedPhotos(model.selectedPhotos);
		tagOptions.add(newOption);
		
		setupToolbarTags();
		setupTagMenuOptions();
		
		newOption.startEditing();
	}
	
	public void removeTag(String tag) {
		model.removeTag(tag);
		for (JToggleButton toggle : tagsToggle) {
			if (toggle.getText().equals(tag)) {
				tagsToggle.remove(toggle);
				break;
			}
		}
		for (TagOption option : tagOptions) {
			if (option.name.equals(tag)) {
				tagOptions.remove(option);
				break;
			}
		}
		setupToolbarTags();
		setupTagMenuOptions();
	}
	
	public void renameTag(String oldTagName, String newTagName) {
		model.renameTag(oldTagName, newTagName);
		for (JToggleButton toggle : tagsToggle) {
			if (toggle.getText().equals(oldTagName)) {
				toggle.setText(newTagName);
				break;
			}
		}
	}
	
	public static void showStatusText(String text) {
		app.status.setText(text);
	}

	// PhotoListener: Process photo model events.
	@Override
	public void photoEventReceived(PhotoEvent e) {
		if (e.type == PhotoEvent.Type.ViewModeChanged) {
			if (e.newViewMode == PhotoApplicationModel.ViewMode.Browser) {
				clearItem.setEnabled(false);
				scaleSmallImageItem.setEnabled(false);
			    originalSizeItem.setEnabled(false);
			    fitWindowItem.setEnabled(false);
			    fitWidthItem.setEnabled(false);
			    fitHeightItem.setEnabled(false);
			} else if (e.newViewMode == PhotoApplicationModel.ViewMode.PhotoViewer) {
				clearItem.setEnabled(true);
				scaleSmallImageItem.setEnabled(true);
			    originalSizeItem.setEnabled(true);
			    fitWindowItem.setEnabled(true);
			    fitWidthItem.setEnabled(true);
			    fitHeightItem.setEnabled(true);
			}
		}
	}

}