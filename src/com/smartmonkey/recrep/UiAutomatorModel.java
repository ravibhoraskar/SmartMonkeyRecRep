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

import com.android.chimpchat.core.IChimpDevice;
import com.android.chimpchat.core.PhysicalButton;
import com.android.chimpchat.core.TouchPressType;
import com.android.ddmlib.Log;
import com.smartmonkey.recrep.actions.ScreenshotAction;
import com.smartmonkey.recrep.chimpevent.ButtonChimpEvent;
import com.smartmonkey.recrep.chimpevent.ChimpEvent;
import com.smartmonkey.recrep.chimpevent.FBLoginEvent;
import com.smartmonkey.recrep.chimpevent.NoOpEvent;
import com.smartmonkey.recrep.chimpevent.TextChimpEvent;
import com.smartmonkey.recrep.chimpevent.TouchChimpEvent;
import com.smartmonkey.recrep.tree.BasicTreeNode;
import com.smartmonkey.recrep.tree.UiHierarchyXmlLoader;
import com.smartmonkey.recrep.tree.UiNode;
import com.smartmonkey.recrep.tree.BasicTreeNode.IFindNodeListener;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.TouchEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UiAutomatorModel {

	private static UiAutomatorModel inst = null;

	private File mScreenshotFile, mXmlDumpFile;
	private IChimpDevice chimp;
	private UiAutomatorViewer mView;
	private Image mScreenshot;
	private BasicTreeNode mRootNode;
	private BasicTreeNode mSelectedNode;
	private Rectangle mCurrentDrawingRect;
	private List<Rectangle> mNafNodes;
	private List<ChimpEvent> log;
	
	
	// determines whether we lookup the leaf UI node on mouse move of screenshot
	// image
	private boolean mExploreMode = true;

	private boolean mShowNafNodes = false;

	// determines whether the screen should be refreshed automatically after
	// every event
	private boolean mAutoRefresh = true;

	private long lastEventTime;

	private int x=0;

	private int y=0;

	private UiAutomatorModel(UiAutomatorViewer view) {
		mView=view;
		log=new ArrayList<ChimpEvent>();

	}

	public static UiAutomatorModel createInstance(UiAutomatorViewer view) {
		if (inst != null) {
			throw new IllegalStateException("instance already created!");
		}
		inst = new UiAutomatorModel(view);
		return inst;
	}

	public static UiAutomatorModel getModel() {
		if (inst == null) {
			throw new IllegalStateException("instance not created yet!");
		}
		return inst;
	}

	public File getScreenshotFile() {
		return mScreenshotFile;
	}

	public File getXmlDumpFile() {
		return mXmlDumpFile;
	}

	public IChimpDevice getChimp() {
		
		return chimp;
	}

	public void setChimp(IChimpDevice device) {
		this.chimp = device;
	}

	public UiAutomatorViewer getmView() {
		return mView;
	}

	public boolean loadScreenshotAndXmlDump(File screenshotFile,
			File xmlDumpFile) {
		if (screenshotFile != null && xmlDumpFile != null
				&& screenshotFile.isFile() && xmlDumpFile.isFile()) {
			ImageData[] data = null;
			Image img = null;
			try {
				// use SWT's ImageLoader to read png from path
				data = new ImageLoader().load(screenshotFile.getAbsolutePath());
			} catch (SWTException e) {
				e.printStackTrace();
				return false;
			}
			// "data" is an array, probably used to handle images that has
			// multiple frames
			// i.e. gifs or icons, we just care if it has at least one here
			if (data.length < 1)
				return false;
			UiHierarchyXmlLoader loader = new UiHierarchyXmlLoader();
			BasicTreeNode rootNode = loader.parseXml(xmlDumpFile
					.getAbsolutePath());
			if (rootNode == null) {
				System.err.println("null rootnode after parsing.");
				return false;
			}
			mNafNodes = loader.getNafNodes();
			try {
				// Image is tied to ImageData and a Display, so we only need to
				// create once
				// per new image
				img = new Image(getmView().getShell().getDisplay(), data[0]);
			} catch (SWTException e) {
				e.printStackTrace();
				return false;
			}
			// only update screenhot and xml if both are loaded successfully
			if (mScreenshot != null) {
				mScreenshot.dispose();
			}
			mScreenshot = img;
			if (mRootNode != null) {
				mRootNode.clearAllChildren();
			}

			// TODO: we should verify here if the coordinates in the XML matches
			// the png
			// or not: think loading a phone screenshot with a tablet XML dump
			mRootNode = rootNode;
			mScreenshotFile = screenshotFile;
			mXmlDumpFile = xmlDumpFile;
			mExploreMode = true;
			getmView().loadScreenshotAndXml();
			return true;
		}
		return false;
	}

	public BasicTreeNode getXmlRootNode() {
		return mRootNode;
	}

	public Image getScreenshot() {
		return mScreenshot;
	}

	public BasicTreeNode getSelectedNode() {
		return mSelectedNode;
	}

	/**
	 * change node selection in the Model recalculate the rect to highlight,
	 * also notifies the View to refresh accordingly
	 * 
	 * @param node
	 */
	public void setSelectedNode(BasicTreeNode node) {
		mSelectedNode = node;
		if (mSelectedNode != null && mSelectedNode instanceof UiNode) {
			UiNode uiNode = (UiNode) mSelectedNode;
			mCurrentDrawingRect = new Rectangle(uiNode.x, uiNode.y,
					uiNode.width, uiNode.height);
		} else {
			mCurrentDrawingRect = null;
		}
		getmView().updateScreenshot();
		if (mSelectedNode != null) {
			getmView().loadAttributeTable();
		}
	}

	public Rectangle getCurrentDrawingRect() {
		return mCurrentDrawingRect;
	}

	/**
	 * Do a search in tree to find a leaf node or deepest parent node containing
	 * the coordinate
	 * 
	 * @param x
	 * @param y
	 */
	public void updateSelectionForCoordinates(int x, int y) {
		this.x=x;
		this.y=y;
		if (mRootNode == null)
			return;
		MinAreaFindNodeListener listener = new MinAreaFindNodeListener();
		boolean found = mRootNode.findLeafMostNodesAtPoint(x, y, listener);
		if (found && listener.mNode != null
				&& !listener.mNode.equals(mSelectedNode)) {
			getmView().updateTreeSelection(listener.mNode);
		}
	}

	public boolean isExploreMode() {
		return mExploreMode;
	}

	public void toggleExploreMode() {
		mExploreMode = !mExploreMode;
		getmView().updateScreenshot();
	}

	public void setExploreMode(boolean exploreMode) {
		mExploreMode = exploreMode;
	}

	private static class MinAreaFindNodeListener implements IFindNodeListener {
		BasicTreeNode mNode = null;

		@Override
		public void onFoundNode(BasicTreeNode node) {
			if (mNode == null) {
				mNode = node;
			} else {
				if ((node.height * node.width) < (mNode.height * mNode.width)) {
					mNode = node;
				}
			}
		}
	}

	public List<Rectangle> getNafNodes() {
		return mNafNodes;
	}

	public void toggleShowNaf() {
		mShowNafNodes = !mShowNafNodes;
		getmView().updateScreenshot();
	}

	public boolean shouldShowNafNodes() {
		return mShowNafNodes;
	}

	public boolean autoRefresh() {
		return mAutoRefresh;
	}

	public void toggleAutoRefresh() {
		mAutoRefresh = !mAutoRefresh;
	}

	/**
	 * Touch the center of the view that is currently selected
	 */
	public void touch() {
		int x = 0, y = 0;
		
		//To touch at the center of the selected view, use this
		//x = mSelectedNode.x + (mSelectedNode.width / 2);
		//y = mSelectedNode.y + (mSelectedNode.height / 2);
		//
		
		//To touch at the place where it is clicked, use this
		x=this.x;
		y=this.y;
		
		
		System.out.println("Touching the screen at coordinates (" + x + "," + y + ")");
		TouchChimpEvent event = new TouchChimpEvent(getWait(),x,y);
		event.invokenow(chimp);
		//TODO: This should ideally store the view ID, not the coordinates, to facilitate cross-device replay
		addToLog(event);
		
		if (autoRefresh()) {
			ScreenshotAction action = new ScreenshotAction(getmView());
			action.run();
		}
	}
	
	public void pressButton(PhysicalButton button)
	{
		chimp.press(button, TouchPressType.DOWN_AND_UP);
		System.out.println("Pressing the "+ button +" button");
		addToLog(new ButtonChimpEvent(getWait(),button));
		if (autoRefresh()) {
			ScreenshotAction action = new ScreenshotAction(getmView());
			action.run();
		}
	}
	
	public void noop() {
		addToLog(new NoOpEvent(getWait()));
		if (autoRefresh()) {
			ScreenshotAction action = new ScreenshotAction(getmView());
			action.run();
		}

	}
	
	public void sendText(String text) {
		chimp.type(text);
		System.out.println("Typing text: "+text);
		addToLog(new TextChimpEvent(getWait(),text));
		if (autoRefresh()) {
			ScreenshotAction action = new ScreenshotAction(getmView());
			action.run();
		}
	}
	
	public void FBLogin()
	{
		FBLoginEvent event = new FBLoginEvent(getWait());
		addToLog(event);
		event.invokenow(chimp);
		if (autoRefresh()) {
			ScreenshotAction action = new ScreenshotAction(getmView());
			action.run();
		}
	}
	
	private void addToLog(ChimpEvent textChimpEvent) {
		log.add(textChimpEvent);
		mView.getChimpEventTableViewer().refresh();
		
	}

	private long getWait() {
		if (log.isEmpty())
		{
			lastEventTime=System.currentTimeMillis();
			return 0;
		}
		else
		{
			long cur = System.currentTimeMillis();
			long toret = cur - lastEventTime;
			lastEventTime=cur;
			return toret;
		}
	}

	public List<ChimpEvent> getLog() {
		return log;
	}

	public void clearLog() {
		log.clear();
		mView.getChimpEventTableViewer().refresh();
	}




}
