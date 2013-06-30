/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartmonkey.recrep;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.smartmonkey.recrep.actions.ClearRecordingAction;
import com.smartmonkey.recrep.actions.ExpandAllAction;
import com.smartmonkey.recrep.actions.FBLoginAction;
import com.smartmonkey.recrep.actions.ImageHelper;
import com.smartmonkey.recrep.actions.OpenFilesAction;
import com.smartmonkey.recrep.actions.OpenRecordFileAction;
import com.smartmonkey.recrep.actions.ReplayAction;
import com.smartmonkey.recrep.actions.SaveFilesAction;
import com.smartmonkey.recrep.actions.SaveRecordFileAction;
import com.smartmonkey.recrep.actions.ScreenshotAction;
import com.smartmonkey.recrep.actions.ToggleAutoRefreshAction;
import com.smartmonkey.recrep.actions.ToggleNafAction;
import com.smartmonkey.recrep.chimpevent.ChimpEvent;
import com.smartmonkey.recrep.tree.AttributePair;
import com.smartmonkey.recrep.tree.BasicTreeNode;
import com.smartmonkey.recrep.tree.BasicTreeNodeContentProvider;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

public class SMonkeyViewer extends ApplicationWindow {

	private static final int IMG_BORDER = 2;

	private Canvas mScreenshotCanvas;

	private Text mText;
	private Button mSendTextButton;

	private TreeViewer mTreeViewer;

	private TableViewer chimpEventTableViewer;

	private Action mOpenFilesAction;
	private Action mSaveFilesAction;
	private Action mExpandAllAction;
	private Action mScreenshotAction;
	private Action mToggleNafAction;
	private Action mToggleAutoRefreshAction;
	private Action mReplayAction;
	private Action mFBLoginAction;
	private Action mClearRecordingAction;
	private Action mOpenRecordFileAction;
	private Action mSaveRecordFileAction;
	
	private TableViewer mTableViewer;

	private float mScale = 1.0f;
	private int mDx, mDy;

	/**
	 * Create the application window.
	 */
	public SMonkeyViewer() {
		super(null);
		SMonkeyModel.createInstance(this);
		createActions();
		
	}

	/**
	 * Create contents of the application window.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		parent.setLayout(new FillLayout());
		SashForm baseSash = new SashForm(parent, SWT.HORIZONTAL | SWT.NONE);
		// draw the canvas with border, so the divider area for sash form can be
		// highlighted
		SashForm leftSash = new SashForm(baseSash, SWT.VERTICAL);
		mScreenshotCanvas = new Canvas(leftSash, SWT.BORDER
				| SWT.NO_REDRAW_RESIZE);
		mScreenshotCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 3) // Right Click
				{
					SMonkeyModel.getModel().toggleExploreMode();
				} else if (e.button == 1) // Left Click
				{
					SMonkeyModel.getModel().touch();
				}
			}
		});

		mScreenshotCanvas.setBackground(getShell().getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND));
		mScreenshotCanvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Image image = SMonkeyModel.getModel().getScreenshot();
				if (image != null) {
					updateScreenshotTransformation();
					// shifting the image here, so that there's a border around
					// screen shot
					// this makes highlighting red rectangles on the screen shot
					// edges more visible
					Transform t = new Transform(e.gc.getDevice());
					t.translate(mDx, mDy);
					t.scale(mScale, mScale);
					e.gc.setTransform(t);
					e.gc.drawImage(image, 0, 0);
					// this resets the transformation to identity transform,
					// i.e. no change
					// we don't use transformation here because it will cause
					// the line pattern
					// and line width of highlight rect to be scaled, causing to
					// appear to be blurry
					e.gc.setTransform(null);
					if (SMonkeyModel.getModel().shouldShowNafNodes()) {
						// highlight the "Not Accessibility Friendly" nodes
						e.gc.setForeground(e.gc.getDevice().getSystemColor(
								SWT.COLOR_YELLOW));
						e.gc.setBackground(e.gc.getDevice().getSystemColor(
								SWT.COLOR_YELLOW));
						for (Rectangle r : SMonkeyModel.getModel()
								.getNafNodes()) {
							e.gc.setAlpha(50);
							e.gc.fillRectangle(mDx + getScaledSize(r.x), mDy
									+ getScaledSize(r.y),
									getScaledSize(r.width),
									getScaledSize(r.height));
							e.gc.setAlpha(255);
							e.gc.setLineStyle(SWT.LINE_SOLID);
							e.gc.setLineWidth(2);
							e.gc.drawRectangle(mDx + getScaledSize(r.x), mDy
									+ getScaledSize(r.y),
									getScaledSize(r.width),
									getScaledSize(r.height));
						}
					}
					// draw the mouseover rects
					Rectangle rect = SMonkeyModel.getModel()
							.getCurrentDrawingRect();
					if (rect != null) {
						e.gc.setForeground(e.gc.getDevice().getSystemColor(
								SWT.COLOR_RED));
						if (SMonkeyModel.getModel().isExploreMode()) {
							// when we highlight nodes dynamically on mouse
							// move,
							// use dashed borders
							e.gc.setLineStyle(SWT.LINE_DASH);
							e.gc.setLineWidth(1);
						} else {
							// when highlighting nodes on tree node selection,
							// use solid borders
							e.gc.setLineStyle(SWT.LINE_SOLID);
							e.gc.setLineWidth(2);
						}
						e.gc.drawRectangle(mDx + getScaledSize(rect.x), mDy
								+ getScaledSize(rect.y),
								getScaledSize(rect.width),
								getScaledSize(rect.height));
					}
				}
			}
		});
		mScreenshotCanvas.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if (SMonkeyModel.getModel().isExploreMode()) {
					SMonkeyModel.getModel().updateSelectionForCoordinates(
							getInverseScaledSize(e.x - mDx),
							getInverseScaledSize(e.y - mDy));
				}
			}
		});

		// Lower Left Base contains the physical buttons on the phone
		SashForm lowerLeftBase = new SashForm(leftSash, SWT.HORIZONTAL);

		Composite buttonComposite = new Composite(lowerLeftBase, SWT.BORDER);
		ToolBarManager physicalButtonToolbarManager = new ToolBarManager();
		physicalButtonToolbarManager.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageHelper
						.loadImageDescriptorFromResource("images/back_gray.png");
			}

			public void run() {
				SMonkeyModel.getModel().pressButton(PhysicalButton.BACK);
			}
		});
		physicalButtonToolbarManager.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageHelper
						.loadImageDescriptorFromResource("images/menu_gray.png");
			}

			public void run() {
				SMonkeyModel.getModel().pressButton(PhysicalButton.MENU);
			}
		});
		physicalButtonToolbarManager.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageHelper
						.loadImageDescriptorFromResource("images/home_gray.png");
			}

			public void run() {
				SMonkeyModel.getModel().pressButton(PhysicalButton.HOME);
			}
		});
		physicalButtonToolbarManager.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return ImageHelper
						.loadImageDescriptorFromResource("images/noop.png");
			}

			public void run() {
				SMonkeyModel.getModel().noop();
			}
		});
		physicalButtonToolbarManager.add(mFBLoginAction);
		
		physicalButtonToolbarManager.createControl(buttonComposite);

		Composite textComposite = new Composite(lowerLeftBase, SWT.BORDER);
		mText = new Text(textComposite, SWT.SINGLE);
		mSendTextButton = new Button(textComposite, SWT.PUSH);
		mSendTextButton.setText("Send\nText");
		mSendTextButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				SMonkeyModel.getModel().sendText(mText.getText());
			}
		});
		textComposite.setLayout(new FillLayout());

		leftSash.setWeights(new int[] { 6, 1 });

		// middle sash contains the list of events, which are highlighted as
		// they happen.
		// TODO: Add a fast forward button to perform the next event, skipping
		// the wait
		SashForm middleSash = new SashForm(baseSash, SWT.VERTICAL);
		
		ToolBarManager replayToolbarManager = new ToolBarManager(SWT.FLAT);
		replayToolbarManager.add(mClearRecordingAction);
		replayToolbarManager.add(mOpenRecordFileAction);
		replayToolbarManager.add(mSaveRecordFileAction);
		replayToolbarManager.add(mToggleAutoRefreshAction);
		replayToolbarManager.add(mReplayAction);
		
		replayToolbarManager.createControl(middleSash);

		chimpEventTableViewer = new TableViewer(middleSash,SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		
		chimpEventTableViewer.getTable().setMenu(new Menu(chimpEventTableViewer.getTable()));
				
		
		TableViewerColumn waitColumn = new TableViewerColumn(
				chimpEventTableViewer, SWT.NONE);
		waitColumn.getColumn().setText("Wait time");
		waitColumn.getColumn().setWidth(100);
		waitColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ChimpEvent) {
					ChimpEvent chimp = (ChimpEvent) element;
					return chimp.getWait();
				}
				return super.getText(element);
			}
		});

		TableViewerColumn typeColumn = new TableViewerColumn(
				chimpEventTableViewer, SWT.NONE);
		typeColumn.getColumn().setText("Type");
		typeColumn.getColumn().setWidth(100);
		typeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ChimpEvent) {
					ChimpEvent chimp = (ChimpEvent) element;
					return chimp.getType();
				}
				return super.getText(element);
			}
		});

		TableViewerColumn miscColumn = new TableViewerColumn(
				getChimpEventTableViewer(), SWT.NONE);
		miscColumn.getColumn().setText("Misc");
		miscColumn.getColumn().setWidth(100);
		miscColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ChimpEvent) {
					ChimpEvent chimp = (ChimpEvent) element;
					return chimp.getMisc();
				}
				return super.getText(element);
			}
		});
		chimpEventTableViewer.getTable().setLinesVisible(true);
		chimpEventTableViewer.getTable().setHeaderVisible(true);
		chimpEventTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		chimpEventTableViewer.setInput(SMonkeyModel.getModel().getLog());

		middleSash.setWeights(new int[] { 1, 20 });

		// right sash is split into 2 parts: upper-right and lower-right
		// both are composites with borders, so that the horizontal divider can
		// be highlighted by
		// the borders
		SashForm rightSash = new SashForm(baseSash, SWT.VERTICAL);

		// upper-right base contains the toolbar and the tree
		Composite upperRightBase = new Composite(rightSash, SWT.BORDER);
		upperRightBase.setLayout(new GridLayout(1, false));
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		toolBarManager.add(mOpenFilesAction);
		toolBarManager.add(mSaveFilesAction);
		toolBarManager.add(mExpandAllAction);
		toolBarManager.add(mScreenshotAction);
		toolBarManager.add(mToggleNafAction);

		// toolBarManager.add(mClearRecordingAction);
		// toolBarManager.add(mSaveRecordingAction);
		toolBarManager.createControl(upperRightBase);

		// Button b = new Button(upperRightBase.getShell(),SWT.CHECK);
		// b.setText("Auto");

		mTreeViewer = new TreeViewer(upperRightBase, SWT.NONE);
		mTreeViewer.setContentProvider(new BasicTreeNodeContentProvider());
		// default LabelProvider uses toString() to generate text to display
		mTreeViewer.setLabelProvider(new LabelProvider());
		mTreeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection().isEmpty()) {
							SMonkeyModel.getModel().setSelectedNode(null);
						} else if (event.getSelection() instanceof IStructuredSelection) {
							IStructuredSelection selection = (IStructuredSelection) event
									.getSelection();
							Object o = selection.toArray()[0];
							if (o instanceof BasicTreeNode) {
								SMonkeyModel.getModel().setSelectedNode(
										(BasicTreeNode) o);
							}
						}
					}
				});
		Tree tree = mTreeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// move focus so that it's not on tool bar (looks weird)
		tree.setFocus();

		// lower-right base contains the detail group
		Composite lowerRightBase = new Composite(rightSash, SWT.BORDER);
		lowerRightBase.setLayout(new FillLayout());
		Group grpNodeDetail = new Group(lowerRightBase, SWT.NONE);
		grpNodeDetail.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpNodeDetail.setText("Node Detail");

		Composite tableContainer = new Composite(grpNodeDetail, SWT.NONE);

		TableColumnLayout columnLayout = new TableColumnLayout();
		tableContainer.setLayout(columnLayout);

		mTableViewer = new TableViewer(tableContainer, SWT.NONE
				| SWT.FULL_SELECTION);
		Table table = mTableViewer.getTable();
		table.setLinesVisible(true);
		// use ArrayContentProvider here, it assumes the input to the
		// TableViewer
		// is an array, where each element represents a row in the table
		mTableViewer.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn tableViewerColumnKey = new TableViewerColumn(
				mTableViewer, SWT.NONE);
		TableColumn tblclmnKey = tableViewerColumnKey.getColumn();
		tableViewerColumnKey.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AttributePair) {
					// first column, shows the attribute name
					return ((AttributePair) element).key;
				}
				return super.getText(element);
			}
		});
		columnLayout.setColumnData(tblclmnKey, new ColumnWeightData(1,
				ColumnWeightData.MINIMUM_WIDTH, true));

		TableViewerColumn tableViewerColumnValue = new TableViewerColumn(
				mTableViewer, SWT.NONE);
		tableViewerColumnValue
				.setEditingSupport(new AttributeTableEditingSupport(
						mTableViewer));
		TableColumn tblclmnValue = tableViewerColumnValue.getColumn();
		columnLayout.setColumnData(tblclmnValue, new ColumnWeightData(2,
				ColumnWeightData.MINIMUM_WIDTH, true));
		tableViewerColumnValue.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AttributePair) {
					// second column, shows the attribute value
					return ((AttributePair) element).value;
				}
				return super.getText(element);
			}
		});
		// sets the ratio of the vertical split: left 5 vs middle 3 vs right 3
		baseSash.setWeights(new int[] { 5, 5, 3 });
		return baseSash;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		mOpenFilesAction = new OpenFilesAction(this);
		mSaveFilesAction = new SaveFilesAction(this);
		mExpandAllAction = new ExpandAllAction(this);
		mScreenshotAction = new ScreenshotAction(this);
		mToggleNafAction = new ToggleNafAction();
		mToggleAutoRefreshAction = new ToggleAutoRefreshAction();
		mReplayAction = new ReplayAction();
		mFBLoginAction = new FBLoginAction();
		mClearRecordingAction = new ClearRecordingAction();
		mOpenRecordFileAction = new OpenRecordFileAction(this);
		mSaveRecordFileAction = new SaveRecordFileAction(this);
		
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			SMonkeyViewer window = new SMonkeyViewer();
			window.setBlockOnOpen(true);
			window.open();
			window.getShell().setMaximized(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * 
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("UI Automator Viewer");
		newShell.setMaximized(true);
	}

	/**
	 * Asks the Model for screenshot and xml tree data, then populates the
	 * screenshot area and tree view accordingly
	 */
	public void loadScreenshotAndXml() {
		mScreenshotCanvas.redraw();
		// load xml into tree
		BasicTreeNode wrapper = new BasicTreeNode();
		// putting another root node on top of existing root node
		// because Tree seems to like to hide the root node
		wrapper.addChild(SMonkeyModel.getModel().getXmlRootNode());
		mTreeViewer.setInput(wrapper);
		mTreeViewer.getTree().setFocus();
	}

	/*
	 * Causes a redraw of the canvas.
	 * 
	 * The drawing code of canvas will handle highlighted nodes and etc based on
	 * data retrieved from Model
	 */
	public void updateScreenshot() {
		mScreenshotCanvas.redraw();
	}

	public void expandAll() {
		mTreeViewer.expandAll();
	}

	public void updateTreeSelection(BasicTreeNode node) {
		mTreeViewer.setSelection(new StructuredSelection(node), true);
	}

	public void loadAttributeTable() {
		// udpate the lower right corner table to show the attributes of the
		// node
		mTableViewer.setInput(SMonkeyModel.getModel().getSelectedNode()
				.getAttributesArray());
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	private void updateScreenshotTransformation() {
		Rectangle canvas = mScreenshotCanvas.getBounds();
		Rectangle image = SMonkeyModel.getModel().getScreenshot()
				.getBounds();
		float scaleX = (canvas.width - 2 * IMG_BORDER - 1)
				/ (float) image.width;
		float scaleY = (canvas.height - 2 * IMG_BORDER - 1)
				/ (float) image.height;
		// use the smaller scale here so that we can fit the entire screenshot
		mScale = Math.min(scaleX, scaleY);
		// calculate translation values to center the image on the canvas
		mDx = (canvas.width - getScaledSize(image.width) - IMG_BORDER * 2) / 2
				+ IMG_BORDER;
		mDy = (canvas.height - getScaledSize(image.height) - IMG_BORDER * 2)
				/ 2 + IMG_BORDER;
	}

	private int getScaledSize(int size) {
		if (mScale == 1.0f) {
			return size;
		} else {
			return new Double(Math.floor((size * mScale))).intValue();
		}
	}

	private int getInverseScaledSize(int size) {
		if (mScale == 1.0f) {
			return size;
		} else {
			return new Double(Math.floor((size / mScale))).intValue();
		}
	}

	public TableViewer getChimpEventTableViewer() {
		return chimpEventTableViewer;
	}

	public void setChimpEventTableViewer(TableViewer chimpEventTableViewer) {
		this.chimpEventTableViewer = chimpEventTableViewer;
	}

	private class AttributeTableEditingSupport extends EditingSupport {

		private TableViewer mViewer;

		public AttributeTableEditingSupport(TableViewer viewer) {
			super(viewer);
			mViewer = viewer;
		}

		@Override
		protected boolean canEdit(Object arg0) {
			return true;
		}

		@Override
		protected CellEditor getCellEditor(Object arg0) {
			return new TextCellEditor(mViewer.getTable());
		}

		@Override
		protected Object getValue(Object o) {
			return ((AttributePair) o).value;
		}

		@Override
		protected void setValue(Object arg0, Object arg1) {
		}

	}
}
