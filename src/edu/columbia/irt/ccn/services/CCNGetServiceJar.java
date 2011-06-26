package edu.columbia.irt.ccn.services;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNInputStream;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.ccnx.ccn.impl.support.Log;

/* A modified ccngetfile class to download JAR files 
 * JAR files are served by an instance of CCNFileProxy
 */
public class CCNGetServiceJar {
    public static Integer timeout = null;
    public void getJarFile(String jar) {
        try {
            int readsize = 1024;
            ContentName argName = ContentName.fromURI("/" + jar);
            
            CCNHandle handle = CCNHandle.open();

            File theFile = new File(jar);
            if (theFile.exists()) {
                Log.info("Overwriting file: " + jar);
            }
            FileOutputStream output = new FileOutputStream(theFile);
            
            long starttime = System.currentTimeMillis();
            CCNInputStream input;
            input = new CCNFileInputStream(argName, handle);
            if (timeout != null) {
                input.setTimeout(timeout); 
            }
            byte [] buffer = new byte[readsize];
            
            int readcount = 0;
            long readtotal = 0;
            while ((readcount = input.read(buffer)) != -1){
                readtotal += readcount;
                output.write(buffer, 0, readcount);
                output.flush();
            }
            System.out.println("ccngetfile took: "+(System.currentTimeMillis() - starttime)+"ms");
        } catch (ConfigurationException e) {
            System.out.println("Configuration exception in ccngetfile: " + e.getMessage());
            e.printStackTrace();
        } catch (MalformedContentNameStringException e) {
            System.out.println("Malformed name: " + jar + " " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Cannot write file or read content. " + e.getMessage());
            e.printStackTrace();
        }
    }
}

