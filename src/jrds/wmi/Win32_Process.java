package jrds.wmi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.WmiDispatch;

public class Win32_Process extends WMIProbeIndexed {
    private String pattern;

    public void configure(String label, String pattern) {
        super.configure(label);
        this.pattern = pattern;
    }

    protected Set<WmiDispatch> locatWbemObject() throws JIException {
        final Pattern p = Pattern.compile(pattern);

        WMIConnection.Matcher m = new WMIConnection.Matcher() {
            public boolean match(WmiDispatch wbemObject) {
                try {
                    Object CommandLine = wbemObject.getField("CommandLine");
                    log(Level.TRACE, "CommandLine=%s", CommandLine);
                    return p.matcher(CommandLine.toString()).matches();
                } catch (JIException e) {
                    log(Level.ERROR,e, e.getMessage());
                }
                return false;
            }
        };

        WMIConnection cnx = find(WMIConnection.class, getConnectionName());
        return cnx.findInCache(getPd().getSpecific("wbemClass"), m);
    }

    /* (non-Javadoc)
     * @see jrds.wmi.WMIProbe#doCollect()
     */
    @Override
    protected Map<String, Number> doCollect() throws JIException {
        Map<String, Number> retValues = new HashMap<String, Number>(getPd().getCollectMapping().size());
        Set<WmiDispatch> wbemObjectSet = locatWbemObject();
        for(WmiDispatch wbemObject: wbemObjectSet) {
            Map<String, Number> tempValues = collectObject(wbemObject);
            join(retValues, tempValues);
        }
        if(retValues.size() > 0) {
            retValues.put("ProcessCount", wbemObjectSet.size());
        }
        return retValues;
    }

    protected void join(Map<String, Number> retValues,
            Map<String, Number> tempValues) {

        for(Map.Entry<String, Number> e: tempValues.entrySet()) {
            double d;
            if(retValues.containsKey(e.getKey())) {
                d = retValues.get(e.getKey()).doubleValue();
                if( ! Double.isNaN(d)) {
                    d += e.getValue().doubleValue();
                }
                else d = e.getValue().doubleValue(); 
            }
            else {
                d = e.getValue().doubleValue();
            }
            retValues.put(e.getKey(), d);
        }
    }

}
