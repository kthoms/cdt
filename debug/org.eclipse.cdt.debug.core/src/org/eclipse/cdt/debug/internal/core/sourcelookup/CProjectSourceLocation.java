/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.sourcelookup;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * Locates source elements in a Java project. Returns instances of <code>IFile</code>.
 * 
 * @since Sep 23, 2002
 */
public class CProjectSourceLocation implements IProjectSourceLocation
{
	private static final String ELEMENT_NAME = "cProjectSourceLocation";
	private static final String ATTR_PROJECT = "project";
	private static final String ATTR_GENERIC = "generic";

	/**
	 * The project associated with this source location
	 */
	private IProject fProject;

	private IResource[] fFolders;

	private HashMap fCache = new HashMap( 20 );
	
	private HashSet fNotFoundCache = new HashSet( 20 );

	private boolean fGenerated = true;

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation()
	{
	}

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation( IProject project )
	{
		setProject( project );
		fGenerated = true;
		initializeFolders();
	}

	/**
	 * Constructor for CProjectSourceLocation.
	 */
	public CProjectSourceLocation( IProject project, boolean generated )
	{
		setProject( project );
		fGenerated = generated;
		initializeFolders();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#findSourceElement(String)
	 */
	public Object findSourceElement( String name ) throws CoreException
	{
		Object result = null;
		if ( getProject() != null && !notFoundCacheLookup( name ) )
		{
			result = cacheLookup( name );
			if ( result == null )
			{ 
				result = doFindSourceElement( name );
				if ( result != null )
				{
					cacheSourceElement( name, result );
				}
			}
			if ( result == null )
			{
				cacheNotFound( name );
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICSourceLocation.class ) )
			return this;
		if ( adapter.equals( CProjectSourceLocation.class ) )
			return this;
		if ( adapter.equals( IProject.class ) )
			return getProject();
		return null;
	}

	/**
	 * Sets the project in which source elements will be searched for.
	 * 
	 * @param project the project
	 */
	private void setProject( IProject project )
	{
		fProject = project;
	}

	/**
	 * Returns the project associated with this source location.
	 * 
	 * @return project
	 */
	public IProject getProject()
	{
		return fProject;
	}

	private Object doFindSourceElement( String name )
	{
		File file = new File( name );
		return ( file.isAbsolute() ) ? findFileByAbsolutePath( name ) : 
									   findFileByRelativePath( name );
	}

	private Object findFileByAbsolutePath( String name )
	{
		File file = new File( name );
		Object result = null;
		if ( file.isAbsolute() && file.exists() ) 
			result = findFile( file );
		return result;
	}

	private Object findFileByRelativePath( String fileName )
	{
		IResource[] folders = getFolders();
		LinkedList list = new LinkedList();
		for ( int i = 0; i < folders.length; ++i )
		{
			if ( list.size() > 0 && !searchForDuplicateFileNames() )
				break;
			IPath path = folders[i].getLocation().append( fileName );
			Object result = findFile( new File( path.toOSString() ) );
			if ( result instanceof List )
				list.addAll( (List)result );
			else if ( result != null )
				list.add( result );
		}
		if ( list.size() == 1 || (list.size() > 0 && !searchForDuplicateFileNames()) )
			return list.getFirst();
		if ( list.size() > 0 )
			return list;
		return null;
	}

	private Object findFile( final File file )
	{
		if ( file != null )
		{
			final String name = file.getName();
			IResource[] folders = getFolders();
			final LinkedList list = new LinkedList();
			for ( int i = 0; i < folders.length; ++i )
			{
				// The workspace resources are case-sensitive, so we can not just 
				// append the file name to the folder name and check if the result exists.
				if ( list.size() > 0 && !searchForDuplicateFileNames() )
					break;
				try
				{
					folders[i].accept( 
								new IResourceProxyVisitor()
									{
										public boolean visit( IResourceProxy proxy ) throws CoreException
										{
											// use equalsIgnoreCase to make it work for Wondows
											if ( proxy.getType() == IResource.FILE && proxy.getName().equalsIgnoreCase( name ) )
											{
												IResource resource = proxy.requestResource();
												File file1 = new File( resource.getLocation().toOSString() );
												if ( file1.exists() && file1.equals( file ) )
													list.addLast( resource );
												return false;
											}
											return true;
										}
									},
								IResource.NONE );
				}
				catch( CoreException e )
				{
				}
			}
			if ( list.size() == 1 || (list.size() > 0 && !searchForDuplicateFileNames()) )
				return list.getFirst();
			if ( list.size() > 0 )
				return list;
		}
		return null;
	}

	private Object cacheLookup( String name )
	{
		return fCache.get( name );
	}
	
	private boolean notFoundCacheLookup( String name )
	{
		return fNotFoundCache.contains( name );
	}
	
	private void cacheSourceElement( String name, Object element )
	{
		fCache.put( name, element );
	}

	private void cacheNotFound( String name )
	{
		fNotFoundCache.add( name );
	}

	protected void dispose()
	{
		fCache.clear();
		fNotFoundCache.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#getMemento()
	 */
	public String getMemento() throws CoreException
	{
		Document doc = new DocumentImpl();
		Element node = doc.createElement( ELEMENT_NAME );
		doc.appendChild( node );
		node.setAttribute( ATTR_PROJECT, getProject().getName() );
		node.setAttribute( ATTR_GENERIC, new Boolean( isGeneric() ).toString() );
		try
		{
			return CDebugUtils.serializeDocument( doc, " " );
		}
		catch( IOException e )
		{
			abort( MessageFormat.format( "Unable to create memento for C/C++ project source location {0}.", new String[] { getProject().getName() } ), e );
		}
		// execution will not reach here
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation#initializeFrom(java.lang.String)
	 */
	public void initializeFrom( String memento ) throws CoreException
	{
		Exception ex = null;
		try
		{
			Element root = null;
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			StringReader reader = new StringReader( memento );
			InputSource source = new InputSource( reader );
			root = parser.parse( source ).getDocumentElement();

			String name = root.getAttribute( ATTR_PROJECT );
			if ( isEmpty( name ) )
			{
				abort( "Unable to initialize source location - missing project name", null );
			}
			else
			{
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( name );
				setProject( project );
			}
			String isGeneric = root.getAttribute( ATTR_GENERIC );
			if ( isGeneric == null || isGeneric.trim().length() == 0 )
				isGeneric = Boolean.FALSE.toString();
			setGenerated( isGeneric.equals( Boolean.TRUE.toString() ) );
			initializeFolders();
			return;
		}
		catch( ParserConfigurationException e )
		{
			ex = e;
		}
		catch( SAXException e )
		{
			ex = e;
		}
		catch( IOException e )
		{
			ex = e;
		}
		abort( "Exception occurred initializing source location.", ex );
	}

	/**
	 * Throws an internal error exception
	 */
	private void abort( String message, Throwable e ) throws CoreException
	{
		IStatus s = new Status( IStatus.ERROR,
								CDebugCorePlugin.getUniqueIdentifier(),
								CDebugCorePlugin.INTERNAL_ERROR,
								message,
								e );
		throw new CoreException( s );
	}

	private boolean isEmpty( String string )
	{
		return string == null || string.length() == 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.sourcelookup.IProjectSourceLocation#isGenerated()
	 */
	public boolean isGeneric()
	{
		return fGenerated;
	}

	public void setGenerated( boolean b )
	{
		fGenerated = b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj )
	{
		if ( obj instanceof IProjectSourceLocation && getProject() != null )
			return getProject().equals( ((IProjectSourceLocation)obj).getProject() );
		return false;
	}

	private void initializeFolders()
	{
		final LinkedList list = new LinkedList();
		if ( getProject() != null )
		{
			list.add( getProject() );
			try
			{
				getProject().accept( 
						new IResourceProxyVisitor()
							{
								public boolean visit( IResourceProxy proxy ) throws CoreException
								{
									switch( proxy.getType() )
									{
										case IResource.FILE:
											return false;
										case IResource.FOLDER:
											list.addLast( proxy.requestResource() );
											return true;
									}
									return true;
								}
	
							}, 
						IResource.NONE );
			}
			catch( CoreException e )
			{
			}
		}
		fFolders = (IResource[])list.toArray( new IResource[list.size()] );
	}

	protected IResource[] getFolders()
	{
		return fFolders;
	}

	protected boolean searchForDuplicateFileNames()
	{
		// for now
		return false;
	}
}
