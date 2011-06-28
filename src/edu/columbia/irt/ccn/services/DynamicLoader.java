package edu.columbia.irt.ccn.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;

import org.ccnx.ccn.impl.support.Log;

/* Class to dynamically load java JAR files during runtime */
public class DynamicLoader {

	private static final Class<?>[] parameters = new Class[] { URL.class };
	Class<?> serviceClass;
	public static int MODULE_LIFETIME = 2400;
	public static String NETSERV_HOME = "/home/amanus/workspace/NetServStreaming/netserv";
	public static String NSIS_TRIGGER = NETSERV_HOME
			+ "/core/nsis/nsis-0.6.0/bin/netserv-trigger";

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
		File f = new File(file);
		if (!f.exists()) {
			Log.info("Service jar does not exist.. {0}", service);
		}
		Log.info("Loading.. {0} into classpath ..", file);
		DynamicLoader.addFile(file);
		Log.info("Loading service ... {0}.jar\n", service);
		serviceClass = ClassLoader.getSystemClassLoader().loadClass(service);
	}

	/**
	 * This constructor is used for NetServ service setup
	 * 
	 * @param filePrefix
	 * @param service
	 * @param controllerIP
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public DynamicLoader(String filePrefix, String service, String controllerIP)
			throws IOException, ClassNotFoundException {
		String file = filePrefix + "/" + service + ".jar";
		File f = new File(file);
		if (!f.exists()) {
			Log.info("Service jar does not exist.. {0}", service);
		} else {
			String node = null;
			for (int i = 0; i < 5; i++) {
				if (sendNetServSetup(controllerIP, filePrefix, service)) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Log.severe("signalThread : Error while sleeping: "
								+ e.toString());
					}
					node = sendNetServProbe(controllerIP, service);
					if (node != null) {
						break;
					}
				}
			}
		}

		Log.info("Loading.. {0} into classpath ..", file);
		DynamicLoader.addFile(file);
		Log.info("Loading service ... {0}.jar\n", service);
		serviceClass = ClassLoader.getSystemClassLoader().loadClass(service);
	}

	public Class<?> getServiceClass() {
		return serviceClass;
	}

	private boolean sendNetServSetup(String client, String filePrefix,
			String service) {
		String s;
		String command = NSIS_TRIGGER + " " + client
				+ " -s -user jae -id CCN.services." + service + " -url "
				+ "http://10.0.1.10/modules/" + service + ".jar -ttl "
				+ MODULE_LIFETIME;

		Log.info("Sending command.. {0}", command);
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			while ((s = stdInput.readLine()) != null) {
				// output will look like: 2 0 0
				if (s.equals("2 0 0")) {
					Log.info(
							"CCN Service {0} installed 0n NetServ container..",
							service);
					return true;
				} else {
					Log.info(
							"Error installing CCN Service {0} 0n NetServ container..",
							service);
					return false;
				}
			}
		} catch (IOException e) {
			Log.severe("sendNetServSetup : Error running trigger setup \n"
					+ e.toString());
		}
		return false;
	}

	private String sendNetServProbe(String client, String service) {
		String command = NSIS_TRIGGER + " " + client
				+ " -p -user jae -id CCN.services." + service + " -probe 2";
		Log.info("Probing NetServ container .. {0}", command);
		BufferedReader stdInput;
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			// output will look like:
			// 1.2.3.4 ACTIVE (for working nodes)
			// 1.2.3.4 NOT PRESENT (for non-working nodes)
			String s;
			while ((s = stdInput.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(s);
				String ipAddr = null;
				String status = null;
				ipAddr = st.nextToken();
				if (ipAddr != null) {
					status = st.nextToken();
				}
				if (status.equals("ACTIVE")) {
					return ipAddr;
				}
			}
		} catch (IOException e) {
			Log.severe("sendNetServProbe : NSIS trigger not present");
			Log.severe("sendNetServProbe : Error adding NetServ node"
					+ e.toString());
		}
		return null;
	}

	public boolean sendNetServTeardown(String client, String service) {
		String command = NSIS_TRIGGER + " " + client
				+ " -r -user jae -id CCN.services." + service;
		try {
			Runtime.getRuntime().exec(command);
			Log.info("sendNetServTeardown : Removing NetServ node " + client);
			return true;
		} catch (IOException e) {
			Log.info("sendNetServTeardown : Error removing node list: "
					+ e.toString());
		}
		return false;
	}
}
