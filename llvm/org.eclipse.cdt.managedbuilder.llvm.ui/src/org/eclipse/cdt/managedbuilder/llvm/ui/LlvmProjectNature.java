/*******************************************************************************
 * Copyright (c) 2010-2013 Nokia Siemens Networks Oyj, Finland.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Nokia Siemens Networks - initial implementation
 *      Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.llvm.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Implements project nature for LLVM projects.
 * 
 */
public class LlvmProjectNature implements IProjectNature {

	private IProject project;

	/**
	 * Configure the project which have this project nature.
	 */
	@Override
	public void configure() throws CoreException {
		// Add nature-specific information
        // for the project, such as adding a builder
        // to a project's build spec.
	}

	/**
	 * Deconfigure those projects which have this project nature.
	 */
	@Override
	public void deconfigure() throws CoreException {
		// Remove the nature-specific information.
	}

	/**
	 * Return the project.
	 * 
	 * @return IProject
	 */
	@Override
	public IProject getProject() {
		return this.project;
	}

	/**
	 * Set the project.
	 * 
	 * @param proj IProject
	 */
	@Override
	public void setProject(IProject proj) {
		this.project = proj;
	}

}
