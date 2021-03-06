/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.core.internal.Messages;
import org.eclipse.cdt.arduino.core.internal.build.ArduinoBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ArduinoManager {

	public static final ArduinoManager instance = new ArduinoManager();

	// Build tool ids
	public static final String BOARD_OPTION_ID = "org.eclipse.cdt.arduino.option.board"; //$NON-NLS-1$
	public static final String PLATFORM_OPTION_ID = "org.eclipse.cdt.arduino.option.platform"; //$NON-NLS-1$
	public static final String PACKAGE_OPTION_ID = "org.eclipse.cdt.arduino.option.package"; //$NON-NLS-1$
	public static final String AVR_TOOLCHAIN_ID = "org.eclipse.cdt.arduino.toolChain.avr"; //$NON-NLS-1$

	public static final String LIBRARIES_URL = "http://downloads.arduino.cc/libraries/library_index.json"; //$NON-NLS-1$
	private List<PackageIndex> packageIndices;
	private LibraryIndex libraryIndex;

	public void loadIndices() {
		new Job(Messages.ArduinoBoardManager_0) {
			protected IStatus run(IProgressMonitor monitor) {
				String[] boardUrls = ArduinoPreferences.getBoardUrls().split("\n"); //$NON-NLS-1$
				packageIndices = new ArrayList<>(boardUrls.length);
				for (String boardUrl : boardUrls) {
					loadPackageIndex(boardUrl, true);
				}

				loadLibraryIndex(true);
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	private void loadPackageIndex(String url, boolean download) {
		try {
			URL packageUrl = new URL(url.trim());
			Path packagePath = ArduinoPreferences.getArduinoHome()
					.resolve(Paths.get(packageUrl.getPath()).getFileName());
			File packageFile = packagePath.toFile();
			if (download) {
				Files.copy(packageUrl.openStream(), packagePath, StandardCopyOption.REPLACE_EXISTING);
			}
			if (packageFile.exists()) {
				try (Reader reader = new FileReader(packageFile)) {
					PackageIndex index = new Gson().fromJson(reader, PackageIndex.class);
					index.setOwners(ArduinoManager.this);
					packageIndices.add(index);
				}
			}
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	public List<PackageIndex> getPackageIndices() throws CoreException {
		if (packageIndices == null) {
			String[] boardUrls = ArduinoPreferences.getBoardUrls().split("\n"); //$NON-NLS-1$
			packageIndices = new ArrayList<>(boardUrls.length);
			for (String boardUrl : boardUrls) {
				loadPackageIndex(boardUrl, false);
			}
		}
		return packageIndices;
	}

	private void loadLibraryIndex(boolean download) {
		try {
			URL librariesUrl = new URL(LIBRARIES_URL);
			Path librariesPath = ArduinoPreferences.getArduinoHome()
					.resolve(Paths.get(librariesUrl.getPath()).getFileName());
			File librariesFile = librariesPath.toFile();
			if (download) {
				Files.copy(librariesUrl.openStream(), librariesPath, StandardCopyOption.REPLACE_EXISTING);
			}
			if (librariesFile.exists()) {
				try (Reader reader = new FileReader(librariesFile)) {
					libraryIndex = new Gson().fromJson(reader, LibraryIndex.class);
				}
			}
		} catch (IOException e) {
			Activator.log(e);
		}

	}

	public LibraryIndex getLibraryIndex() throws CoreException {
		if (libraryIndex == null) {
			loadLibraryIndex(false);
		}
		return libraryIndex;
	}

	public ArduinoBoard getBoard(String boardName, String platformName, String packageName) throws CoreException {
		for (PackageIndex index : packageIndices) {
			ArduinoPackage pkg = index.getPackage(packageName);
			if (pkg != null) {
				ArduinoPlatform platform = pkg.getPlatform(platformName);
				if (platform != null) {
					ArduinoBoard board = platform.getBoard(boardName);
					if (board != null) {
						return board;
					}
				}
			}
		}
		return null;
	}

	public List<ArduinoBoard> getBoards() throws CoreException {
		List<ArduinoBoard> boards = new ArrayList<>();
		for (PackageIndex index : packageIndices) {
			for (ArduinoPackage pkg : index.getPackages()) {
				for (ArduinoPlatform platform : pkg.getLatestPlatforms()) {
					boards.addAll(platform.getBoards());
				}
			}
		}
		return boards;
	}

	public List<ArduinoBoard> getInstalledBoards() throws CoreException {
		List<ArduinoBoard> boards = new ArrayList<>();
		for (PackageIndex index : packageIndices) {
			for (ArduinoPackage pkg : index.getPackages()) {
				for (ArduinoPlatform platform : pkg.getInstalledPlatforms()) {
					boards.addAll(platform.getBoards());
				}
			}
		}
		return boards;
	}

	public ArduinoPackage getPackage(String packageName) {
		for (PackageIndex index : packageIndices) {
			ArduinoPackage pkg = index.getPackage(packageName);
			if (pkg != null) {
				return pkg;
			}
		}
		return null;
	}

	public ArduinoTool getTool(String packageName, String toolName, String version) {
		for (PackageIndex index : packageIndices) {
			ArduinoPackage pkg = index.getPackage(packageName);
			if (pkg != null) {
				ArduinoTool tool = pkg.getTool(toolName, version);
				if (tool != null) {
					return tool;
				}
			}
		}
		return null;
	}

	private static final String LIBRARIES = "libraries"; //$NON-NLS-1$

	private IEclipsePreferences getSettings(IProject project) {
		return (IEclipsePreferences) new ProjectScope(project).getNode(Activator.getId());
	}

	public Collection<ArduinoLibrary> getLibraries(IProject project) throws CoreException {
		IEclipsePreferences settings = getSettings(project);
		String librarySetting = settings.get(LIBRARIES, "[]"); //$NON-NLS-1$
		Type stringSet = new TypeToken<Set<String>>() {
		}.getType();
		Set<String> libraryNames = new Gson().fromJson(librarySetting, stringSet);
		LibraryIndex index = ArduinoManager.instance.getLibraryIndex();
		List<ArduinoLibrary> libraries = new ArrayList<>(libraryNames.size());
		for (String name : libraryNames) {
			libraries.add(index.getLibrary(name));
		}
		return libraries;
	}

	public void setLibraries(final IProject project, final Collection<ArduinoLibrary> libraries) throws CoreException {
		List<String> libraryNames = new ArrayList<>(libraries.size());
		for (ArduinoLibrary library : libraries) {
			libraryNames.add(library.getName());
		}
		IEclipsePreferences settings = getSettings(project);
		settings.put(LIBRARIES, new Gson().toJson(libraryNames));
		try {
			settings.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}

		new Job("Install libraries") {
			protected IStatus run(IProgressMonitor monitor) {
				MultiStatus mstatus = new MultiStatus(Activator.getId(), 0, "Installing libraries", null);
				for (ArduinoLibrary library : libraries) {
					IStatus status = library.install(monitor);
					if (!status.isOK()) {
						mstatus.add(status);
					}
				}

				// Clear the scanner info caches to pick up new includes
				try {
					for (IBuildConfiguration config : project.getBuildConfigs()) {
						ArduinoBuildConfiguration arduinoConfig = config.getAdapter(ArduinoBuildConfiguration.class);
						arduinoConfig.clearScannerInfo();
					}
				} catch (CoreException e) {
					mstatus.add(e.getStatus());
				}
				return mstatus;
			}
		}.schedule();
	}

	public static IStatus downloadAndInstall(String url, String archiveFileName, Path installPath,
			IProgressMonitor monitor) {
		try {
			URL dl = new URL(url);
			Path dlDir = ArduinoPreferences.getArduinoHome().resolve("downloads"); //$NON-NLS-1$
			Files.createDirectories(dlDir);
			Path archivePath = dlDir.resolve(archiveFileName);
			Files.copy(dl.openStream(), archivePath, StandardCopyOption.REPLACE_EXISTING);

			boolean isWin = Platform.getOS().equals(Platform.OS_WIN32);

			// extract
			ArchiveInputStream archiveIn = null;
			try {
				String compressor = null;
				String archiver = null;
				if (archiveFileName.endsWith("tar.bz2")) { //$NON-NLS-1$
					compressor = CompressorStreamFactory.BZIP2;
					archiver = ArchiveStreamFactory.TAR;
				} else if (archiveFileName.endsWith(".tar.gz") || archiveFileName.endsWith(".tgz")) { //$NON-NLS-1$ //$NON-NLS-2$
					compressor = CompressorStreamFactory.GZIP;
					archiver = ArchiveStreamFactory.TAR;
				} else if (archiveFileName.endsWith(".tar.xz")) { //$NON-NLS-1$
					compressor = CompressorStreamFactory.XZ;
					archiver = ArchiveStreamFactory.TAR;
				} else if (archiveFileName.endsWith(".zip")) { //$NON-NLS-1$
					archiver = ArchiveStreamFactory.ZIP;
				}

				InputStream in = new BufferedInputStream(new FileInputStream(archivePath.toFile()));
				if (compressor != null) {
					in = new CompressorStreamFactory().createCompressorInputStream(compressor, in);
				}
				archiveIn = new ArchiveStreamFactory().createArchiveInputStream(archiver, in);

				for (ArchiveEntry entry = archiveIn.getNextEntry(); entry != null; entry = archiveIn.getNextEntry()) {
					if (entry.isDirectory()) {
						continue;
					}

					Path entryPath = installPath.resolve(entry.getName());
					Files.createDirectories(entryPath.getParent());

					if (entry instanceof TarArchiveEntry) {
						TarArchiveEntry tarEntry = (TarArchiveEntry) entry;
						if (tarEntry.isLink()) {
							Path linkPath = installPath.resolve(tarEntry.getLinkName());
							Files.createSymbolicLink(entryPath, entryPath.getParent().relativize(linkPath));
						} else {
							Files.copy(archiveIn, entryPath, StandardCopyOption.REPLACE_EXISTING);
						}
						if (!isWin) {
							int mode = tarEntry.getMode();
							Files.setPosixFilePermissions(entryPath, toPerms(mode));
						}
					} else {
						Files.copy(archiveIn, entryPath, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			} finally {
				if (archiveIn != null) {
					archiveIn.close();
				}
			}

			// Fix up directory
			File[] children = installPath.toFile().listFiles();
			if (children.length == 1 && children[0].isDirectory()) {
				// make that directory the install path
				Path childPath = children[0].toPath();
				Path tmpPath = installPath.getParent().resolve("_t"); //$NON-NLS-1$
				Files.move(childPath, tmpPath);
				Files.delete(installPath);
				Files.move(tmpPath, installPath);
			}
			return Status.OK_STATUS;
		} catch (IOException | CompressorException | ArchiveException e) {
			return new Status(IStatus.ERROR, Activator.getId(), "Installing Platform", e);
		}
	}

	private static Set<PosixFilePermission> toPerms(int mode) {
		Set<PosixFilePermission> perms = new HashSet<>();
		if ((mode & 0400) != 0) {
			perms.add(PosixFilePermission.OWNER_READ);
		}
		if ((mode & 0200) != 0) {
			perms.add(PosixFilePermission.OWNER_WRITE);
		}
		if ((mode & 0100) != 0) {
			perms.add(PosixFilePermission.OWNER_EXECUTE);
		}
		if ((mode & 0040) != 0) {
			perms.add(PosixFilePermission.GROUP_READ);
		}
		if ((mode & 0020) != 0) {
			perms.add(PosixFilePermission.GROUP_WRITE);
		}
		if ((mode & 0010) != 0) {
			perms.add(PosixFilePermission.GROUP_EXECUTE);
		}
		if ((mode & 0004) != 0) {
			perms.add(PosixFilePermission.OTHERS_READ);
		}
		if ((mode & 0002) != 0) {
			perms.add(PosixFilePermission.OTHERS_WRITE);
		}
		if ((mode & 0001) != 0) {
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
		}
		return perms;
	}

}
