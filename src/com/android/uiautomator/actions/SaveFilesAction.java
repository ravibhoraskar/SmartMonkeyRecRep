package com.android.uiautomator.actions;

import java.io.File;
import java.io.IOException;

import com.android.uiautomator.OpenDialog;
import com.android.uiautomator.UiAutomatorModel;
import com.android.uiautomator.UiAutomatorViewer;
import com.google.common.io.Files;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;

public class SaveFilesAction extends Action {
	ApplicationWindow mWindow;

	public SaveFilesAction(ApplicationWindow window) {
		super("&Save");
		mWindow = window;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageHelper.loadImageDescriptorFromResource("images/save.png");
	}

	@Override
	public void run() {
		UiAutomatorModel model = UiAutomatorModel.getModel();
		String message = "Saved to /tmp/dump.xml and /tmp/screenshot.png";
		try {
			File xmlin = model.getXmlDumpFile();
			File xmlout = new File("/tmp/dump.xml");
			Files.copy(xmlin, xmlout);
			File pngin = model.getScreenshotFile();
			File pngout = new File("/tmp/screenshot.png");
			Files.copy(pngin, pngout);
		} catch (IOException e) {
			message = "Couldn't save";
		}

		MessageDialog dialog = new MessageDialog(mWindow.getShell(), "Save",
				null, message, MessageDialog.ERROR, new String[] { "OK" }, 0);
		dialog.open();

	}
}
