/*
 * Copyright 2011 Jeroen Meetsma
 *
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.configuration.classloading;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.exception.ResourceException;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.io.FileSupport;
import org.ijsberg.iglu.util.io.StreamSupport;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Classes are loaded throughout an application in various ways.
 * In most cases classes are loaded implicitly by a current classloader
 * when code is executed and objects are created.
 * The method Class.forName that takes one parameter does the same.
 * In other cases the thread's context classloader is
 * used or a dedicated classloader or explicitly the
 * system classloader.
 * <p/>
 * If an application can be loaded in a separate, dedicated classloader,
 * it can be made possible to upgrade that application,
 * without shutting down the Iglu runtime environment.
 * To achieve that as much classes are loaded by a dedicated
 * classloader, the application class itself must be loaded
 * by the dedicated classloader. The thread's context
 * classloader must also be set to the dedicated classloader,
 * to inform 3<sup>rd</sup> party components of the desire to use a dedicated
 * classloader.
 * <p/>
 * Problems may arise if 3<sup>rd</sup> party implementations (e.g.: database drivers)
 * contain generic libraries that are partially loaded by the
 * system and the dedicated classloader.
 * <p/>
 * This classloader:
 * <ul>
 * <li>tries to load classes defined as class-files or jars from a dedicated classpath</li>
 * <li><b>except for</b> Iglu's enterprise interfaces of which loading is done by the parent</li>
 * <li>delegates remaining unresolved requests to the parent as well</li>
 * </ul>
 * <p/>
 * If classes must not be loaded via the dedicated class loader, it's advised to
 * store them (in jars) elsewhere, such as the <code>./ext</code> directory, and
 * refer to them by classpath.
 * The Iglu jar must be in the normal, as well as the dedicated classpath (<code>./lib</code>).
 * It is the only jar of which classes are loaded by the system class loader and this class loader.
 * <p/>
 * Use of a dedicated application class loader produces the following results:
 * <ul>
 * <li>common interfaces are always valid.</li>
 * <li>a maximum effort is made to load as much classes by the dedicated
 * class loader as possible.</li>
 * <li>classes (in jars) in the classpath are loaded via the
 * parent class loader</li>
 * <li>classes can be loaded from (and updated in) a generic location</li>
 * </ul>
 * <p/>
 * The Iglu application class loader inherits from URLClassLoader.
 * It gives components, such as a JSP-engine, the opportunity to expand the classpath.
 */
public class ExtendedClassPathClassLoader extends URLClassLoader {

	//contains either a filename pointing to a JAR containing the resource or an actual file
	private TreeMap classResourceLocations = new TreeMap();
	private HashMap propertiesResourceLocations = new HashMap(10);
	private TreeMap mixedResourceLocations = new TreeMap();

	private String classpath = "./classes:./lib";
	private String excludedPackageName;// = Cluster.class.getPackage().getName();
	private HashMap fileCreationTimes = new HashMap(10);

	//statistics
	private int nrofResourcesDeleted;
	private int nrofResourcesUpdated;

	/**
	 * @param classpath a dedicated classpath; both colons and semicolons are valid separators
	 */
	public ExtendedClassPathClassLoader(String classpath) {
		super(new URL[]{});
		this.classpath = classpath;
		init();
	}


	/**
	 * @param classpath a dedicated classpath; both colons and semicolons are valid separators
	 * @param excludedPackageName name of package of which classes must be loaded by super class loader
	 */
	public ExtendedClassPathClassLoader(String classpath, String excludedPackageName) {
		super(new URL[]{});
		this.classpath = classpath;
		this.excludedPackageName = excludedPackageName;
		init();
	}

	
	/**
	 * Creates a map containing references of each resource to a jar or directory.
	 */
	private void init() {
		Collection paths = StringSupport.split(classpath, ";:", false);

		Iterator i = paths.iterator();
		while (i.hasNext()) {
			String location = FileSupport.convertToUnixStylePath((String) i.next());
			if (location.endsWith(".zip") || location.endsWith(".jar")) {
				mapFilesInZip(location);
			}
			else {
				File dir = new File(location);
				if (dir.isDirectory()) {
					if (!location.endsWith("/")) {
						location += '/';
					}
					Collection classFiles = FileSupport.getFilesInDirectoryTree(dir);
					Iterator j = classFiles.iterator();
					while (j.hasNext()) {
						File file = (File) j.next();
						String fileName = file.getPath();
						if ((fileName.endsWith(".jar") || fileName.endsWith(".zip")) && !file.isHidden()) {
							mapFilesInZip(fileName);
						}
						else// .class .properties etc.
						{
							mapClassResourceEntry(file.getPath().substring(location.length()), file);
							fileCreationTimes.put(file, new Long(file.lastModified()));
						}
					}
				}
				else {
					//path can not be mapped
				}
			}
		}
	}

	/**
	 * @param fileName
	 * @return class name
	 */
	private static String convertFileNameToClassName(String fileName) {
		String className = fileName;
		int dotIndex = className.lastIndexOf('.');
		if (dotIndex >= 0) {
			className = className.substring(0, dotIndex);
		}
		className = className.replace('/', '.');
		className = className.replace('\\', '.');
		return className;

	}

	/**
	 * Creates a mapping to locate files in a zip file.
	 *
	 * @param fileName
	 */
	private void mapFilesInZip(String fileName) {
		File file = new File(fileName);
		fileCreationTimes.put(file, new Long(file.lastModified()));

		ZipFile zipfile;
		try {
			zipfile = new ZipFile(fileName);
		}
		catch (IOException ioe) {
			throw new NoClassDefFoundError("class location '" + fileName + "' can not be accessed by classloader: " + ioe);
		}
		Enumeration e = zipfile.entries();
		while (e.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) e.nextElement();
			if (!entry.isDirectory()) {
				mapClassResourceEntry(entry.getName(), fileName);
			}
		}
		try {
			zipfile.close();
		}
		catch (IOException ioe) {
			throw new NoClassDefFoundError("class fileName '" + fileName + "' can not be accessed by classloader: " + ioe);
		}
	}

	/**
	 * Maps (class) resources to files.
	 *
	 * @param fileName
	 * @param value
	 */
	private void mapClassResourceEntry(String fileName, Object value) {
		Object existingValue = mixedResourceLocations.get(fileName);
		if (existingValue != null
				//&& existingValue.getClass() == value.getClass()
				&& !existingValue.equals(value)) {
			//this may indicate a conflict
			//it occurs frequently for "/MANIFEST.MF" which shouldn't be a problem
			//it may occur for other resources
			//log those occurrences that seem suspicious
			if (!fileName.startsWith("META-INF/") && !resourcesMatchInSize(fileName, existingValue, value)) {
				System.out.println(new LogEntry("WARNING: " + this.getClass().getName() + " found multiple locations for resource '" + fileName + "' ('" + existingValue + "' and '" + value + "')"));
			}
		}
		if (fileName.endsWith(".properties")) {
			String className = convertFileNameToClassName(fileName);
			propertiesResourceLocations.put(className, fileName);
		}
		else if (fileName.endsWith(".class")) {
			String className = convertFileNameToClassName(fileName);
			classResourceLocations.put(className, value);
		}
		mixedResourceLocations.put(fileName, value);
	}


	/**
	 * Loads classes in the following order:
	 * <ul>
	 * <li>classes defined as class-files or jars in the dedicated classpath first</li>
	 * <li>loading of enterprise interfaces is delegated to the parent classloader</li>
	 * <li>remaining unresolved requests are delegated to the parent as well</li>
	 * </ul>
	 *
	 * @param className
	 * @param resolve
	 * @return
	 * @throws ClassNotFoundException
	 * @see super#loadClass(String, boolean)
	 */
	public synchronized Class loadClass(String className, boolean resolve) throws ClassNotFoundException {
		//find classes already loaded by this class loader
		Class retval = super.findLoadedClass(className);
		if (retval == null) {
			if (excludedPackageName == null || !className.startsWith(excludedPackageName)) {
				try {
					retval = findClass(className);
					if(retval == ResourceBundle.class) {
						throw new ClassNotFoundException("found properties instead of class with name " + className);
					}
					else {
						if (resolve) {
							resolveClass(retval);
						}
						return retval;
					}
				}
				catch (ClassNotFoundException e) {
					if(retval == ResourceBundle.class) {
						throw e;
					}
					retval = super.loadClass(className, resolve);
				}
			}
			else {
				retval = super.loadClass(className, resolve);
			}
		}
		return retval;
	}


	/**
	 * Tries to locate a class in the specific classpath.
	 *
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Class findClass(String className) throws ClassNotFoundException {
		Object location = classResourceLocations.get(className);
		if (location == null && this.propertiesResourceLocations.containsKey(className)) {
			String fileName = (String) propertiesResourceLocations.get(className);
			if (fileName.endsWith(".properties")) {
				//notify invoker that file is present as properties file
				return ResourceBundle.class;
			}
			else {
				throw new ClassNotFoundException("resource '" + fileName + "' for class '" + className + "' has incompatible format");
			}
		}
		if (location == null) {
			throw new ClassNotFoundException("class '" + className + "' not found in " + classpath);
		}
		String fileName = className.replace('.', '/') + ".class";

		byte[] data;
		try {
			data = getData(fileName, location);
		}
		catch (IOException ioe) {
			throw new NoClassDefFoundError("class '" + className + "' could not be loaded from '" + location + "' with message: " + ioe);
		}
		return defineClass(className, data, 0, data.length);
	}

	/**
	 * @param filename
	 * @param location1
	 * @param location2
	 * @return
	 */
	private static boolean resourcesMatchInSize(String filename, Object location1, Object location2) {
		return getFileSize(filename, location1) == getFileSize(filename, location2);
	}

	/**
	 * @param filename
	 * @param location
	 * @return
	 */
	private static long getFileSize(String filename, Object location) {
		if (location instanceof File) {
			return ((File) location).length();// + "-" + new Date(((File)location).lastModified());
		}
		try {
			ZipEntry entry = FileSupport.getZipEntryFromJar(filename, (String) location);
			return entry.getSize();// + "-" + new Date(entry.getTime());
		}
		catch (IOException e) {
		}
		return 0;
	}

	/**
	 * Retrieves resource as byte array from a directory or jar in the filesystem.
	 *
	 * @param fileName
	 * @param location
	 * @return
	 * @throws IOException
	 */
	private static byte[] getData(String fileName, Object location) throws IOException {
		byte[] data;
		if (location instanceof String) {
			data = FileSupport.getBinaryFromJar(fileName, (String) location);
		}
		else if (location instanceof File) {
			InputStream in = new FileInputStream((File) location);
			data = StreamSupport.absorbInputStream(in);
			in.close();
		}
		else {
			throw new NoClassDefFoundError("file '" + fileName + "' could not be loaded from '" + location + '\'');
		}
		return data;
	}


	/**
	 * Retrieves resource as input stream from a directory or jar in the filesystem.
	 *
	 * @param fileName
	 * @return
	 */
	public URL findResource(String fileName) {
		Object location = this.mixedResourceLocations.get(fileName);
		if (location == null) {
			return super.findResource(fileName);
		}
		String url;
		if(location instanceof File) {
			url = "file:/" + FileSupport.convertToUnixStylePath(((File)location).getAbsolutePath());
		}
		else {
			url = "jar:file:" + FileSupport.convertToUnixStylePath((String)location) + "!/" + fileName;
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new ResourceException(e);
		}
	}

	/**
	 * Retrieves resource as input stream from a directory or jar in the filesystem.
	 *
	 * @param fileName
	 * @return
	 */
	public InputStream getResourceAsStream(String fileName) {
		InputStream retval = super.getResourceAsStream(fileName);

		if (retval == null) {
			//String className = this.convertFileNameToClassName(fileName);
			//Object location = classResourceLocations.get(className);
			Object location = this.mixedResourceLocations.get(fileName);
			if (location == null) {
				return null;
			}

			byte[] data;
			try {
				data = getData(fileName, location);
			}
			catch (IOException ioe) {
				return null;
			}
			if (data == null) {
				return null;
			}
			return new ByteArrayInputStream(data);

		}
		return retval;
	}


	/**
	 * @return the number of (class) resources that is no longer available from the classpath since
	 *         initialization of the classloader
	 * @see this#checkResources()
	 */
	public int getNrofResourcesDeleted() {
		return nrofResourcesDeleted;
	}

	/**
	 * @return the number of (class) resources that has been updated the classpath since
	 *         initialization of the classloader
	 * @see this#checkResources()
	 */
	public int getNrofResourcesUpdated() {
		return nrofResourcesUpdated;
	}

	/**
	 * Updates statistics on available resources in the classpath.
	 * These statistics can be used to determine if an application must
	 * be reset due to changed sources in a development environment.
	 * Processing may consume a considerable amount of CPU-time.
	 *
	 * @return true if changes are detected since initialization of the class loader
	 * @see this#getNrofResourcesDeleted()
	 * @see this#getNrofResourcesUpdated()
	 */
	public synchronized boolean checkResources() {
		nrofResourcesDeleted = 0;
		nrofResourcesUpdated = 0;
		Iterator i = fileCreationTimes.keySet().iterator();
		while (i.hasNext()) {
			File file = (File) i.next();
			if (!file.exists()) {
				nrofResourcesDeleted++;
			}
			else if (file.lastModified() != ((Long) fileCreationTimes.get(file)).longValue()) {
				nrofResourcesUpdated++;
			}
		}
		return nrofResourcesDeleted > 0 || nrofResourcesUpdated > 0;
	}
}
