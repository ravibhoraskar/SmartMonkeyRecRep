package com.smartmonkey.recrep.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import com.android.chimpchat.core.IChimpDevice;
import com.smartmonkey.recrep.UiAutomatorModel;
import com.smartmonkey.recrep.UiAutomatorViewer;
import com.smartmonkey.recrep.chimpevent.ChimpEvent;

public class ReplayAction extends Action {

    public ReplayAction() {
        super("&Replay", IAction.AS_PUSH_BUTTON);
    }
    
    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageHelper.loadImageDescriptorFromResource("images/play.png");
    }
    
    @Override
    public void run() {
    	Thread t = new Thread (){
    		@Override
    		public void run()
    		{
    			IChimpDevice chimp = UiAutomatorModel.getModel().getChimp();
    	        boolean refresh = UiAutomatorModel.getModel().autoRefresh();
    	        ScreenshotAction action = new ScreenshotAction(UiAutomatorModel.getModel().getmView());
    	        for (ChimpEvent e: UiAutomatorModel.getModel().getLog())
    	        {
    	        	e.invoke(chimp);
    	        	if (refresh)
    	        	{
    	        		
    	    			action.run();
    	        	}
    	        }
    		}
    	};
        t.run();
    }
}
