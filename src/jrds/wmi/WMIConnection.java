package jrds.wmi;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;

import jrds.JrdsLoggerConfiguration;
import jrds.JuliToLog4jHandler;
import jrds.Util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JIProgId;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.core.WmiDispatch;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;

public class WMIConnection extends jrds.starter.Connection<WmiDispatch> {
    static {
        //If not already configured, we filter it
        JrdsLoggerConfiguration.configureLogger("org.jinterop", Level.ERROR);
        Logger.getLogger(WMIConnection.class).debug("Starting wmi");
        java.util.logging.Logger jilogger = JISystem.getLogger();
        jilogger.setUseParentHandlers(false);
        for(Handler h : jilogger.getHandlers()) {
            jilogger.removeHandler(h);
        }
        jilogger.addHandler(new JuliToLog4jHandler());		
        JISystem.setAutoRegisteration(true);
    }

    private static final String WMI_CLSID = "76A6415B-CB41-11d1-8B02-00600806D9B6";
    private static final String WMI_PROGID = "WbemScripting.SWbemLocator";

    private static final String UPTIMERELPATH= "Win32_PerfRawData_PerfOS_System=@";
    private static final String UPTIMEFIELD = "SystemUpTime";

    private static final String LOCALE_en_US = "MS_409";

    private JISession session = null;
    private String domain;
    private String username;
    private String password;
    private String address = null;
    //Point to a SWbemServices object
    private WmiDispatch wbemServices = null;
    final private Map<String, Set<WmiDispatch>> cache = new HashMap<String, Set<WmiDispatch>>();

    public WMIConnection(String domain, String username, String password) {
        super();
        this.domain = domain;
        this.username = username;
        this.password = password;
    }

    public WMIConnection(String username, String password) {
        super();
        this.domain = null;
        this.username = username;
        this.password = password;
    }

    @Override
    public WmiDispatch getConnection() {
        return wbemServices;
    }

    @Override
    public long setUptime() {
        try {
            WmiDispatch perfOs_System = (WmiDispatch) wbemServices.runMethod("Get", UPTIMERELPATH).get(0);
            String uptimeString = (String)perfOs_System.getField(UPTIMEFIELD);
            return Util.parseStringNumber(uptimeString, 0l);
        } catch (JIException e) {
            log(Level.ERROR, e, "Getting uptime error: %s", e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean startConnection() {
        try {
            if(domain == null)
                domain = getHostName();
            session = JISession.createSession(domain, username, password);
            session.useSessionSecurity(true);
            session.setGlobalSocketTimeout(getTimeout() * 1000);
            JIComServer comStub = new JIComServer(JIProgId.valueOf(WMI_PROGID), getHostName(), session);
            //			JIComServer comStub = new JIComServer(JIClsid.valueOf(WMI_CLSID), getHostName(), session);
            WmiDispatch dispatch = WmiDispatch.build((IJIComObject)comStub.createInstance().queryInterface(WMI_CLSID));
            wbemServices = (WmiDispatch)dispatch.runMethod("ConnectServer", address, null, null, null, LOCALE_en_US, null , 0, null).get(0);
            return true;
        } catch (UnknownHostException e) {
            log(Level.ERROR, e, "Unknown host: %s", getHostName());
        } catch (JIException e) {
            log(Level.ERROR, e, "Failed to establish WMI connection with %s: %s", getHostName(), e);
        } catch (SecurityException e) {
            log(Level.ERROR, e, "Security error: %s", e);
        }
        return false;
    }

    @Override
    public void stopConnection() {
        synchronized(cache) {
            cache.clear();
        }
        wbemServices = null;
        if(session != null)
            try {
                JISession.destroySession(session);
            } catch (JIException e) {
                log(Level.ERROR, e, "close error: %s", e.getMessage());
            } catch (Exception e) {
                log(Level.ERROR, e, "Unexpected close error: %s", e.getMessage());
            }
        session = null;
    }

    public interface Matcher {
        public boolean match(WmiDispatch wbemObject) throws JIException;
    }

    public Set<WmiDispatch> findInCache(String wbemClass, Matcher m) throws JIException {
        //A lot of slow call, so a lot of alive check
        if(! isStarted()) {
            return Collections.emptySet();
        }
        Set<WmiDispatch> enumeration = null;
        synchronized(cache) {
            enumeration = cache.get(wbemClass);
        }
        if(enumeration == null) {
            WmiDispatch instanceSet = (WmiDispatch) wbemServices.runMethod("InstancesOf", wbemClass, 0, null).get(0);
            IJIComObject o = (IJIComObject) instanceSet.getField("_NewEnum");
            IJIEnumVariant enumVARIANT = (IJIEnumVariant)JIObjectFactory.narrowObject(o.queryInterface(IJIEnumVariant.IID));
            int count = (Integer) instanceSet.getField("Count");
            enumeration = new HashSet<WmiDispatch>(count);

            //A lot of slow call, so a lot of alive check
            if(! isStarted()) {
                return Collections.emptySet();
            }

            Object[] values = enumVARIANT.next(count);
            //A lot of slow call, so a lot of alive check
            if(!isStarted()) {
                return Collections.emptySet();
            }

            JIArray array = (JIArray)values[0];
            Object[] arrayObj = (Object[])array.getArrayInstance();
            for (int j = 0; j < arrayObj.length; j++) {
                WmiDispatch wbemObject = WmiDispatch.build((JIVariant)arrayObj[j]);
                enumeration.add(wbemObject);
            }
            synchronized(cache) {
                cache.put(wbemClass, enumeration);
            }
        }

        //A lot of slow call, so a lot of alive check
        if(!isStarted()) {
            return Collections.emptySet();
        }

        Set<WmiDispatch> processes = new HashSet<WmiDispatch>();
        for(WmiDispatch wbemObject: enumeration) {
            if(m.match(wbemObject)) {
                processes.add(wbemObject);
            }
        }
        return processes;
    }

}
