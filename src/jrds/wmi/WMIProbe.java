package jrds.wmi;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jrds.Util;
import jrds.ProbeConnected;

import org.apache.log4j.Level;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.WmiDispatch;

public class WMIProbe extends ProbeConnected<String, Number, WMIConnection> {
    class BaseFields {
        public Double Frequency_Object;
        public Double Frequency_PerfTime;
        public Double Frequency_Sys100NS;
        public Double Timestamp_Object;
        public Double Timestamp_PerfTime;
        public Double Timestamp_Sys100NS;
    };

    enum CounterType {
        PERF_COUNTER_RAWCOUNT {
            @Override
            double convert(Number val, BaseFields bf) {
                return val.doubleValue();
            }
        },
        PERF_COUNTER_LARGE_RAWCOUNT {
            @Override
            double convert(Number val, BaseFields bf) {
                return val.doubleValue();
            }
        },
        PERF_COUNTER_COUNTER {
            @Override
            double convert(Number val, BaseFields bf) {
                return val.doubleValue();
            }
        },
        PERF_100NSEC_TIMER {
            @Override
            double convert(Number val, BaseFields bf) {
                return val.doubleValue()  / bf.Frequency_Sys100NS;
            }
        },
        PERF_100NSEC_TIMER_INV {
            @Override
            double convert(Number val, BaseFields bf) {
                return (bf.Timestamp_Sys100NS - val.longValue()) / bf.Frequency_Sys100NS;
            }
        },
        UNIT_100NSEC {
            @Override
            double convert(Number val, BaseFields bf) {
                return val.doubleValue() * 0.0000001;
            }
        };
        abstract double convert(Number val, BaseFields bf);
    }

    protected WmiDispatch dispatcher = null;

    public WMIProbe() {
        super(WMIConnection.class.getName());
    }

    protected BaseFields getBaseFields(WmiDispatch wbemObject) {
        BaseFields bf = new BaseFields();
        String fieldName = "";
        try {
            for(Field f: BaseFields.class.getFields()) {
                fieldName = f.getName();
                Object o;
                o = wbemObject.getField(fieldName);
                if(o instanceof Number) {
                    f.set(bf, o);
                }
                else {
                    Number n = jrds.Util.parseStringNumber(o.toString(), Double.NaN);
                    f.set(bf, n);
                }
            }
        } catch (JIException e) {
            //Expected exception, thrown if the wbemObject is not an Win32_PerfRawData instance
        } catch (Exception e) {
            log(Level.ERROR, e, "Error for field ", fieldName, ": ", e.getMessage());
        }
        return bf;
    }

    protected Map<String, Number> collectObject(WmiDispatch wbemObject) {
        BaseFields bf = getBaseFields(wbemObject);
        Map<String, Number> retValues = new HashMap<String, Number>(getCollectMapping().size());
        for(String collect : getCollectMapping().keySet()) {
            String[] parsed = collect.toString().split("#");
            String field = parsed[0];
            CounterType type = CounterType.PERF_COUNTER_RAWCOUNT;
            try {
                if(parsed.length == 2 && ! "".equals(parsed[1]))
                    type = CounterType.valueOf(parsed[1]);
            } catch (java.lang.IllegalArgumentException e2) {
                log(Level.ERROR, e2, "Illegal type conversion for %s: %s", field, ": ", parsed[1]);
            }

            try {
                Object o = wbemObject.getField(field);
                if(o instanceof Number) {
                    retValues.put(collect, type.convert((Number) o, bf));
                }
                else if(o instanceof String) {
                    Number n = Util.parseStringNumber((String) o, Double.NaN);
                    retValues.put(collect,  type.convert((Number) n, bf));
                }
            } catch (Exception e1) {
                log(Level.ERROR, e1, "Error getting %s: %s", collect, e1);
            }
        }
        return retValues;
    }

    protected Map<String, Number> doCollect() throws JIException {
        String wbemClass = getPd().getSpecific("wbemClass");
        String relpath = Util.parseTemplate(wbemClass + "=@", this);
        WmiDispatch wbemObject = (WmiDispatch) dispatcher.runMethod("Get", relpath).get(0);
        return collectObject(wbemObject);
    }

    @Override
    public Map<String, Number> getNewSampleValuesConnected(WMIConnection cnx) {
        Date start = new Date();
        try {
            dispatcher = (WmiDispatch) cnx.getConnection();
            Map<String, Number> retValues = doCollect();
            Date end  = new Date();
            long duration = end.getTime() - start.getTime();
            log(Level.DEBUG, "Ran for %d ms", duration);
            return retValues;
        } catch (JIException e) {
            log(Level.ERROR, e , "Error: %s", e.getMessage());
        }

        return Collections.emptyMap();
    }

    @Override
    public String getSourceType() {
        return "WMI";
    }
}
