package edu.columbia.irt.ccn.services;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;

import org.ccnx.ccn.impl.support.Log;

/* Class to dynamically load java JAR files during runtime */
public class DynamicLoader {

	private static final Class<?>[] parameters = new Class[] { URL.class };
	Class<?> serviceClass;

	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}

	public static void addFile(File f) throws IOException {
		addURL(f.toURI().toURL());
	}

	public static void addURL(URL u) throws IOException {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException(
					"Error, could not add URL to system classloader");
		}
	}

	public DynamicLoader(String filePrefix, String service) throws IOException,
			ClassNotFoundException {
		String file = filePrefix + "/" + service + ".jar";
		Log.info("Loading.. {0} into classpath ..", file);
		DynamicLoader.addFile(file);
		Log.info("Loading service ... {0}.jar\n", service);
		serviceClass = ClassLoader.getSystemClassLoader().loadClass(service);
	}

	public Class<?> getServiceClass() {
		return serviceClass;
	}
}
