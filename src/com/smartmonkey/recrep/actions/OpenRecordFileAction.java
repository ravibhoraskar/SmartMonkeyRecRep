package com.smartmonkey.recrep.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.FileDialog;

import com.smartmonkey.recrep.OpenDialog;
import com.smartmonkey.recrep.UiAutomatorModel;
import com.smartmonkey.recrep.UiAutomatorViewer;
import com.smartmonkey.recrep.chimpevent.ChimpEvent;

public class OpenRecordFileAction extends Action {

	UiAutomatorViewer mWindow;

	public OpenRecordFileAction(UiAutomatorViewer window) {
		super("&Open");
		mWindow = window;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageHelper
				.loadImageDescriptorFromResource("images/open-folder.png");
	}

	@Override
	public void run() {
		FileDialog dialog = new FileDialog(mWindow.getShell());
		String fileToOpen = dialog.open();
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileToOpen));
			List<ChimpEvent> x =  (List<ChimpEvent>) in.readObject();
			UiAutomatorModel.getModel().clearLog();
			UiAutomatorModel.getModel().getLog().addAll(x);
			mWindow.getChimpEventTableViewer().refresh();
			in.close();
			
		} catch (FileNotFoundException e) {
			MessageDialog.openError(mWindow.getShell(), "Error", "File not Found");
			e.printStackTrace();
		} catch (IOException e) {
			MessageDialog.openError(mWindow.getShell(), "Error", "File couldn't be read");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			MessageDialog.openError(mWindow.getShell(), "Error", "File not in correct format");
			e.printStackTrace();
		}

	}
}
