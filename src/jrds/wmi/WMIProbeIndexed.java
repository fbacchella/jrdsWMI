package jrds.wmi;

import java.util.Map;

import jrds.Util;
import jrds.probe.IndexedProbe;

import org.apache.log4j.Level;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.WmiDispatch;

public class WMIProbeIndexed extends WMIProbe implements IndexedProbe {

    private String index;

    public void configure(String index) {
        this.index = index;
    }

    public String getIndexName() {
        return index;
    }

    /* (non-Javadoc)
     * @see jrds.wmi.WMIProbe#doCollect()
     */
    @Override
    protected Map<String, Number> doCollect() throws JIException {
        String wbemClass = getPd().getSpecific("wbemClass");
        String key = getPd().getSpecific("key");

        String relpath = Util.parseTemplate(wbemClass+"." + key + "=\"" + index + "\"", this);
        log(Level.DEBUG, "Using index %s", index);

        WmiDispatch wbemObject = (WmiDispatch) dispatcher.runMethod("Get", relpath).get(0);

        return collectObject(wbemObject);
    }

}
