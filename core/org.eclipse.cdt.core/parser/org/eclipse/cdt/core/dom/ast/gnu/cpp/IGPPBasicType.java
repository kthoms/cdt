/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 10, 2004
 */
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;

/**
 * @author aniefer
 */
public interface IGPPBasicType extends ICPPBasicType {

	public static final int t_Complex = IGPPASTSimpleDeclSpecifier.t_Complex;
	public static final int t_Imaginary = IGPPASTSimpleDeclSpecifier.t_Imaginary;
	public static final int t_typeof = IGPPASTSimpleDeclSpecifier.t_typeof;
	
	public boolean isLongLong();
	
	public IType getTypeofType();
}
