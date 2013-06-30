package com.smartmonkey.recrep.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.smartmonkey.recrep.UiAutomatorModel;
import com.smartmonkey.recrep.UiAutomatorViewer;
import com.smartmonkey.recrep.chimpevent.ChimpEvent;

public class SaveRecordFileAction extends Action {

    UiAutomatorViewer mWindow;

    public SaveRecordFileAction(UiAutomatorViewer window) {
        super("&Save");
        mWindow = window;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageHelper.loadImageDescriptorFromResource("images/save.png");
    }
    
	@Override
	public void run() {
		FileDialog dialog = new FileDialog(mWindow.getShell(),SWT.SAVE);
		String fileToOpen = dialog.open();
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileToOpen));
			out.writeObject(UiAutomatorModel.getModel().getLog());
			out.close();
			
		} catch (FileNotFoundException e) {
			MessageDialog.openError(mWindow.getShell(), "Error", "File not Found");
			e.printStackTrace();
		} catch (IOException e) {
			MessageDialog.openError(mWindow.getShell(), "Error", "File couldn't be written");
			e.printStackTrace();
		}

	}

}
