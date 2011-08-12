package edu.columbia.irt.ccn.services;

import edu.columbia.irt.netserv.core.osgi.Controller;
import edu.columbia.irt.netserv.core.osgi.Launch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetServBridge {

    public static Controller create() {
        try {
            return Launch.launch("embedded");
        } catch (Exception ex) {
            Logger.getLogger(NetServBridge.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
