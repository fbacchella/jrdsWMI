package jrds.probe;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import jrds.Tools;
import jrds.wmi.WMIConnection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.core.WmiDispatch;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;
import org.junit.BeforeClass;
import org.junit.Test;

public class WMIPlay {
    static final private Logger logger = Logger.getLogger(WMIPlay.class);

    @BeforeClass
    static public void configure() throws ParserConfigurationException, IOException {
        Tools.configure();
        logger.setLevel(Level.ERROR);
        Tools.setLevel(new String[] {"org.jinterop", "jrds.wmi", "jrds"}, logger.getLevel());
    }

    @Test
    public void testConnexion() throws JIException {
        WMIConnection cnx = new WMIConnection("XXXX", "XXXXXX", "XXXXXXX") {
            @Override
            public String getHostName() {
                return "ng137.prod";
            }
        };
        long d1 = new Date().getTime();
        cnx.startConnection();
        long d2 = new Date().getTime();
        WmiDispatch wbemServices = (WmiDispatch) cnx.getConnection();

        long d12 = new Date().getTime();

        @SuppressWarnings("unused")
        Set<WmiDispatch> processes = new HashSet<WmiDispatch>();
        String wbemClass = "Win32_Process";

        @SuppressWarnings("unused")
        int wbemFlagForwardOnly = 0x20;
        @SuppressWarnings("unused")
        int wbemQueryFlagShallow = 0x1;
        WmiDispatch instanceSet = (WmiDispatch) wbemServices.runMethod("InstancesOf", wbemClass, 0 , null).get(0);
        System.out.println(instanceSet.dispatched);
        //int count = (Integer) instanceSet.runMethod("Count").get(0);

        IJIComObject o = (IJIComObject) instanceSet.getField("_NewEnum");
        IJIEnumVariant enumVARIANT = (IJIEnumVariant)JIObjectFactory.narrowObject(o.queryInterface(IJIEnumVariant.IID));

        //instanceSet.dumpFuncs();

        //int count = (Integer) instanceSet.getField("Count");
        //		Object[] valuesAll = enumVARIANT.next(1);
        //		int count = (Integer)valuesAll[1];
        //		while((Integer)valuesAll[1] >0) {
        //			for(Object obj: valuesAll) {
        //				System.out.println(obj);
        //			}
        //			valuesAll = enumVARIANT.next(1);
        //		}

        int count = (Integer) instanceSet.getField("Count");

        long d14 = new Date().getTime();
        try {
            long d18 = new Date().getTime();
            Object[] valuesAll = enumVARIANT.next(count);
            long d19 = new Date().getTime();
            System.out.println("count:" + (d19 - d18));
            System.out.println(valuesAll.length);
            for(int i=0; i< (valuesAll.length - 1); i++) {
                JIArray array = (JIArray) valuesAll[i];
                Object[] arrayObj = (Object[])array.getArrayInstance();
                System.out.println("    " + arrayObj.length);
                for (int j = 0; j < arrayObj.length; j++) {
                    WmiDispatch wbemObject = WmiDispatch.build((JIVariant)arrayObj[j]);
                    @SuppressWarnings("unused")
                    long d16 = new Date().getTime();
                    @SuppressWarnings("unused")
                    Object CommandLine = wbemObject.getField("CommandLine");
                    @SuppressWarnings("unused")
                    long d17 = new Date().getTime();
                    //logger.trace("CommandLine=" + CommandLine);
                    //System.out.println("cmd line:" + (d17 - d16));
                }
            }
            long d20 = new Date().getTime();
            System.out.println("all cmd lines: " + (d20 - d19));
        } catch (Exception e) {
            e.printStackTrace();
        }
        long d15 = new Date().getTime();

        long d13 = new Date().getTime();

        //		for(int i=0 ; i < count; i++) {
        //			Object[] values = enumVARIANT.next(1);
        //			JIArray array = (JIArray)values[0];
        //			Object[] arrayObj = (Object[])array.getArrayInstance();
        //			for (int j = 0; j < arrayObj.length; j++) {
        //				WmiDispatch wbemObject = WmiDispatch.build((JIVariant)arrayObj[j], cnx.getDispIdCache());
        //				Object CommandLine = wbemObject.getField("CommandLine");
        //				logger.trace("CommandLine=" + CommandLine);
        //
        //			}
        //		}

        //		long d3 = new Date().getTime();
        //		System.out.println("==========================");
        //		long d4 = new Date().getTime();
        //		logger.debug(wbemServices.runMethod("Get", "Win32_PerfRawData_PerfOS_Memory=@").get(0));
        //		long d5 = new Date().getTime();
        //		long d10 = new Date().getTime();
        //		int dispid = wbemServices.getIDsOfNames("Get");
        //		long d11 = new Date().getTime();
        //		System.out.println("==========================");
        //		long d6 = new Date().getTime();
        //		logger.debug(wbemServices.runMethod(dispid, "Win32_PerfRawData_PerfOS_Memory=@").get(0));
        //		long d7 = new Date().getTime();
        //		System.out.println("==========================");
        long d8 = new Date().getTime();
        cnx.stopConnection();
        long d9 = new Date().getTime();

        System.out.println("start:" + (d2 - d1));
        //		System.out.println("get id:" + (d11 - d10));
        //		System.out.println("get 1:" + (d5 - d4));
        //		System.out.println("get 2:" + (d7 - d6));
        System.out.println("process:" + (d13 - d12));
        System.out.println("enumeration:" + (d15 - d14));
        System.out.println("end:" + (d9 - d8));

    }

}
