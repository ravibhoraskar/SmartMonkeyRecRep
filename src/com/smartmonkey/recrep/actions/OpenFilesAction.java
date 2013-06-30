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

package com.smartmonkey.recrep.actions;

import com.smartmonkey.recrep.OpenDialog;
import com.smartmonkey.recrep.SMonkeyModel;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.ApplicationWindow;

public class OpenFilesAction extends Action {

    ApplicationWindow mWindow;

    public OpenFilesAction(ApplicationWindow window) {
        super("&Open");
        mWindow = window;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageHelper.loadImageDescriptorFromResource("images/open-folder.png");
    }

    @Override
    public void run() {
        OpenDialog d = new OpenDialog(mWindow.getShell());
        if (d.open() == OpenDialog.OK) {
            SMonkeyModel.getModel().loadScreenshotAndXmlDump(
                    d.getScreenshotFile(), d.getXmlDumpFile());
        }
    }
}
