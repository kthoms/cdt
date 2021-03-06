/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ericsson             - Updated to latest platform code
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions; 

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
 
/**
 * A resume at line action that can be contributed to a an editor. The action
 * will perform the "resume at line" operation for editors that provide
 * an appropriate <code>IResumeAtLineTarget</code> adapter.
 */
public class ResumeAtLineActionDelegate implements IEditorActionDelegate, IActionDelegate2, IViewActionDelegate {
	
	private IWorkbenchPart fActivePart = null;
	private IResumeAtLineTarget fPartTarget = null;
	private IAction fAction = null;
	private DebugContextListener fContextListener = new DebugContextListener();
	private ISuspendResume fTargetElement = null;
	
	class DebugContextListener implements IDebugContextListener {

		protected void contextActivated(ISelection selection) {
			fTargetElement = null;
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (ss.size() == 1) {
                    fTargetElement = (ISuspendResume)
                        DebugPlugin.getAdapter(ss.getFirstElement(), ISuspendResume.class);
				}
			}
			update();
		}

		@Override
		public void debugContextChanged(DebugContextEvent event) {
			contextActivated(event.getContext());
		}
		
	}		
	
	/*(non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	@Override
	public void dispose() {
		DebugUITools.getDebugContextManager().getContextService(fActivePart.getSite().getWorkbenchWindow()).removeDebugContextListener(fContextListener);
		fActivePart = null;
		fPartTarget = null;
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		if (fPartTarget != null && fTargetElement != null) {
			try {
				fPartTarget.resumeAtLine(fActivePart, fActivePart.getSite().getSelectionProvider().getSelection(), fTargetElement);
			} catch (CoreException e) {
				ErrorDialog.openError( fActivePart.getSite().getWorkbenchWindow().getShell(), ActionMessages.getString( "ResumeAtLineActionDelegate.1" ), ActionMessages.getString( "ResumeAtLineActionDelegate.2" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.fAction = action;
		update();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		if (fAction == null) {
			return;
		}
		Runnable r = new Runnable() {
			@Override
			public void run() {
				boolean enabled = false;
				if (fPartTarget != null && fTargetElement != null) {
					IWorkbenchPartSite site = fActivePart.getSite();
					if (site != null) {
					    ISelectionProvider selectionProvider = site.getSelectionProvider();
					    if (selectionProvider != null) {
					        ISelection selection = selectionProvider.getSelection();
					        enabled = fTargetElement.isSuspended() && fPartTarget.canResumeAtLine(fActivePart, selection, fTargetElement);
					    }
					}
				}
				fAction.setEnabled(enabled);				
			}
		};
		CDebugUIPlugin.getStandardDisplay().asyncExec(r);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
		this.fAction = action; 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		init(action);
		bindTo(targetEditor);	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		bindTo(view);
	}
	
	/**
	 * Binds this action to operate on the given part's IResumeAtLineTarget adapter.
	 *  
	 * @param part
	 */
	private void bindTo(IWorkbenchPart part) {
		IDebugContextManager manager = DebugUITools.getDebugContextManager();
		if (fActivePart != null && !fActivePart.equals(part)) {
			manager.getContextService(fActivePart.getSite().getWorkbenchWindow()).removeDebugContextListener(fContextListener);
		}
		fPartTarget = null;
		fActivePart = part;
		if (part != null) {
			IWorkbenchWindow workbenchWindow = part.getSite().getWorkbenchWindow();
			IDebugContextService service = manager.getContextService(workbenchWindow);
			service.addDebugContextListener(fContextListener);
			fPartTarget  = part.getAdapter(IResumeAtLineTarget.class);
			if (fPartTarget == null) {
				IAdapterManager adapterManager = Platform.getAdapterManager();
				// TODO: we could restrict loading to cases when the debugging context is on
				if (adapterManager.hasAdapter(part, IResumeAtLineTarget.class.getName())) {
					fPartTarget = (IResumeAtLineTarget) adapterManager.loadAdapter(part, IResumeAtLineTarget.class.getName());
				}
			}
			ISelection activeContext = service.getActiveContext();
			fContextListener.contextActivated(activeContext);
		}
		update();			
	}
}
