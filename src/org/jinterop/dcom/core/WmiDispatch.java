package org.jinterop.dcom.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jinterop.dcom.common.IJIUnreferenced;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJITypeInfo;
import org.jinterop.dcom.impls.automation.JIExcepInfo;
import org.jinterop.dcom.impls.automation.TypeAttr;

public class WmiDispatch implements IJIDispatch {
    public final IJIDispatch dispatched;
    Map<String, Integer> cacheDispId;
    String wbemClass;

    static public WmiDispatch build(JIVariant comObject) throws JIException {
        IJIComObject temp = comObject.getObjectAsComObject();
        IJIDispatch temp2 = (IJIDispatch)JIObjectFactory.narrowObject(temp);
        return new WmiDispatch(temp2);
    }

    static public WmiDispatch build(JIVariant comObject, String IID) throws JIException {
        IJIComObject temp = comObject.getObjectAsComObject();
        IJIDispatch temp2 = (IJIDispatch)JIObjectFactory.narrowObject(temp.queryInterface(IID));
        return new WmiDispatch(temp2);
    }

    static public WmiDispatch build(IJIComObject comObject) throws JIException {
        IJIComObject temp = comObject.queryInterface(IJIDispatch.IID);
        IJIDispatch temp2 = (IJIDispatch)JIObjectFactory.narrowObject(temp);
        return new WmiDispatch(temp2);
    }

    private void dumpSWbemObject() {
        //		try {
        //			Object[] derivations = (Object[]) getField("Derivation_");
        //			for(Object o: derivations) {
        //				System.out.println(o);
        //			}
        //		} catch (JIException e) {
        //		}
    }

    public void dumpFields() throws JIException {
        int n = dispatched.getTypeInfoCount();
        for(int i=0; i< n; i++) {
            IJITypeInfo ti = dispatched.getTypeInfo(i);
            TypeAttr type = ti.getTypeAttr();
            System.out.println(type.cVars);
            for(int j =0 ; j<type.cVars; j++) {
                int m = ti.getVarDesc(j).memberId;
                JIArray info = (JIArray) ti.getNames(m, 2)[0];
                for(Object name: (Object[]) info.getArrayInstance()) {
                    System.out.println(name);
                }
            }
        }
    }

    public void dumpFuncs() throws JIException {
        int n = dispatched.getTypeInfoCount();
        for(int i=0; i< n; i++) {
            IJITypeInfo ti = dispatched.getTypeInfo(i);
            TypeAttr type = ti.getTypeAttr();
            for(int j =0 ; j<type.cFuncs; j++) {
                int m = ti.getFuncDesc(j).memberId;
                JIArray info = (JIArray) ti.getNames(m, 2)[0];
                for(Object name: (Object[]) info.getArrayInstance()) {
                    System.out.println(name);
                }
            }
        }
    }

    private void dump() {
        try {
            //			System.out.println(runMethod("Get", "__CLASS"));
            int n = dispatched.getTypeInfoCount();
            //			System.out.println("*********");
            //			System.out.println(dispatched.getInterfaceIdentifier());
            for(int i=0; i< n; i++) {
                //				IJITypeInfo ti = dispatched.getTypeInfo(i);
                //				Object[] typelib = ti.getContainingTypeLib();
                //				for(Object o: typelib) {
                //					System.out.println(o);
                //				}
                //				TypeAttr type = ti.getTypeAttr();
                //				System.out.println(type.guid);
                //				for(int j =0 ; j<type.cFuncs; j++) {
                //					int m = ti.getFuncDesc(j).memberId;
                //					JIArray info = (JIArray) ti.getNames(m, 2)[0];
                //					for(Object name: (Object[]) info.getArrayInstance()) {
                //						System.out.println(name);
                //					}
                //				}
                //				for(Field f: TypeAttr.class.getFields()) {
                //					System.out.println(f.getName() + " " + f.get(type).toString());
                //				}
            }
            dumpSWbemObject();
        } catch (Exception e) {
            System.out.println(e);
        }		
    }

    public WmiDispatch(IJIDispatch dispatched) {
        this.dispatched = dispatched;
        dump();
    }

    public WmiDispatch(IJIComObject dispatched) throws JIException {
        this.dispatched = (IJIDispatch)JIObjectFactory.narrowObject(dispatched);
        dump();
    }

    private List<Object> evaluate(JIVariant[] args) throws JIException {
        List<Object> out = new ArrayList<Object>(args.length);
        for(JIVariant variant: args) {
            out.add(evaluateVariant(variant));
        }
        return out;
    }

    private Object evaluateVariant(JIVariant variant) throws JIException {
        if(variant.getObject() instanceof IJIComObject) {
            IJIComObject temp =  JIObjectFactory.narrowObject(variant.getObjectAsComObject());
            if(temp instanceof IJIDispatch) {
                return new WmiDispatch(temp);
            }
            return temp;
        }
        else if(variant.getObject() instanceof JIString) {
            return variant.getObjectAsString2();
        }
        else if(variant.getObject() instanceof JIArray) {
            JIArray array = variant.getObjectAsArray();
            return array.getArrayInstance();
        }
        else {
            return variant.getObject();
        }

    }

    public List<Object> runMethod(String methodName, Object... args) throws JIException {
        Object[] formatedArgs = new Object[args.length];
        for(int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if (obj == null) {
                formatedArgs[i] = JIVariant.OPTIONAL_PARAM();
            }
            else if(obj instanceof String) {
                formatedArgs[i] = new JIString((String) obj);
            }
            else {
                formatedArgs[i] = obj;
            }
        }

        JIVariant[] out = dispatched.callMethodA(methodName, formatedArgs);
        return evaluate(out);
    }

    public List<Object> runMethod(int dispid, Object... args) throws JIException {
        Object[] formatedArgs = new Object[args.length];
        for(int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if (obj == null) {
                formatedArgs[i] = JIVariant.OPTIONAL_PARAM();
            }
            else if(obj instanceof String) {
                formatedArgs[i] = new JIString((String) obj);
            }
            else {
                formatedArgs[i] = obj;
            }
        }

        JIVariant[] out = dispatched.callMethodA(dispid, formatedArgs);
        return evaluate(out);
    }

    public Object getField(String fieldName) throws JIException {
        return  evaluateVariant(dispatched.get(fieldName));
    }
    /**
     * @throws JIException
     * @see org.jinterop.dcom.core.IJIComObject#addRef()
     */
    public void addRef() throws JIException {
        dispatched.addRef();
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.core.IJIComObject#call(org.jinterop.dcom.core.JICallBuilder, int)
     */
    public Object[] call(JICallBuilder arg0, int arg1) throws JIException {
        return dispatched.call(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.core.IJIComObject#call(org.jinterop.dcom.core.JICallBuilder)
     */
    public Object[] call(JICallBuilder arg0) throws JIException {
        return dispatched.call(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethod(int, java.lang.Object[], int[])
     */
    public void callMethod(int arg0, Object[] arg1, int[] arg2)
            throws JIException {
        dispatched.callMethod(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethod(int, java.lang.Object[])
     */
    public void callMethod(int arg0, Object[] arg1) throws JIException {
        dispatched.callMethod(arg0, arg1);
    }

    /**
     * @param arg0
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethod(int)
     */
    public void callMethod(int arg0) throws JIException {
        dispatched.callMethod(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethod(java.lang.String, java.lang.Object[], int[])
     */
    public void callMethod(String arg0, Object[] arg1, int[] arg2)
            throws JIException {
        dispatched.callMethod(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethod(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public void callMethod(String arg0, Object[] arg1, String[] arg2)
            throws JIException {
        dispatched.callMethod(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethod(java.lang.String, java.lang.Object[])
     */
    public void callMethod(String arg0, Object[] arg1) throws JIException {
        dispatched.callMethod(arg0, arg1);
    }

    /**
     * @param arg0
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethod(java.lang.String)
     */
    public void callMethod(String arg0) throws JIException {
        dispatched.callMethod(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethodA(int, java.lang.Object[], int[])
     */
    public JIVariant[] callMethodA(int arg0, Object[] arg1, int[] arg2)
            throws JIException {
        return dispatched.callMethodA(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethodA(int, java.lang.Object[])
     */
    public JIVariant[] callMethodA(int arg0, Object[] arg1) throws JIException {
        return dispatched.callMethodA(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethodA(int)
     */
    public JIVariant callMethodA(int arg0) throws JIException {
        return dispatched.callMethodA(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethodA(java.lang.String, java.lang.Object[], int[])
     */
    public JIVariant[] callMethodA(String arg0, Object[] arg1, int[] arg2)
            throws JIException {
        return dispatched.callMethodA(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethodA(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public JIVariant[] callMethodA(String arg0, Object[] arg1, String[] arg2)
            throws JIException {
        return dispatched.callMethodA(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethodA(java.lang.String, java.lang.Object[])
     */
    public JIVariant[] callMethodA(String arg0, Object[] arg1)
            throws JIException {
        return dispatched.callMethodA(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#callMethodA(java.lang.String)
     */
    public JIVariant callMethodA(String arg0) throws JIException {
        return dispatched.callMethodA(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#get(int, java.lang.Object[])
     */
    public JIVariant[] get(int arg0, Object[] arg1) throws JIException {
        return dispatched.get(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#get(int)
     */
    public JIVariant get(int arg0) throws JIException {
        return dispatched.get(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#get(java.lang.String, java.lang.Object[])
     */
    public JIVariant[] get(String arg0, Object[] arg1) throws JIException {
        return dispatched.get(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#get(java.lang.String)
     */
    public JIVariant get(String arg0) throws JIException {
        return dispatched.get(arg0);
    }

    /**
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#getAssociatedSession()
     */
    public JISession getAssociatedSession() {
        return dispatched.getAssociatedSession();
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#getIDsOfNames(java.lang.String)
     */
    public int getIDsOfNames(String arg0) throws JIException {
        return dispatched.getIDsOfNames(arg0);
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#getIDsOfNames(java.lang.String[])
     */
    public int[] getIDsOfNames(String[] arg0) throws JIException {
        return dispatched.getIDsOfNames(arg0);
    }

    /**
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#getInstanceLevelSocketTimeout()
     */
    public int getInstanceLevelSocketTimeout() {
        return dispatched.getInstanceLevelSocketTimeout();
    }

    /**
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#getInterfaceIdentifier()
     */
    public String getInterfaceIdentifier() {
        return dispatched.getInterfaceIdentifier();
    }

    /**
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#getIpid()
     */
    public String getIpid() {
        return dispatched.getIpid();
    }

    /**
     * @return
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#getLastExcepInfo()
     */
    public JIExcepInfo getLastExcepInfo() {
        return dispatched.getLastExcepInfo();
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#getTypeInfo(int)
     */
    public IJITypeInfo getTypeInfo(int arg0) throws JIException {
        return dispatched.getTypeInfo(arg0);
    }

    /**
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#getTypeInfoCount()
     */
    public int getTypeInfoCount() throws JIException {
        return dispatched.getTypeInfoCount();
    }

    /**
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#getUnreferencedHandler()
     */
    public IJIUnreferenced getUnreferencedHandler() {
        return dispatched.getUnreferencedHandler();
    }

    /**
     * @param arg0
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#internal_getConnectionInfo(java.lang.String)
     */
    public Object[] internal_getConnectionInfo(String arg0) {
        return dispatched.internal_getConnectionInfo(arg0);
    }

    /**
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#internal_getInterfacePointer()
     */
    public JIInterfacePointer internal_getInterfacePointer() {
        return dispatched.internal_getInterfacePointer();
    }

    /**
     * @param arg0
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#internal_removeConnectionInfo(java.lang.String)
     */
    public Object[] internal_removeConnectionInfo(String arg0) {
        return dispatched.internal_removeConnectionInfo(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#internal_setConnectionInfo(org.jinterop.dcom.core.IJIComObject, java.lang.Integer)
     */
    public String internal_setConnectionInfo(IJIComObject arg0, Integer arg1) {
        return dispatched.internal_setConnectionInfo(arg0, arg1);
    }

    /**
     * @param arg0
     * @see org.jinterop.dcom.core.IJIComObject#internal_setDeffered(boolean)
     */
    public void internal_setDeffered(boolean arg0) {
        dispatched.internal_setDeffered(arg0);
    }

    /**
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#isDispatchSupported()
     */
    public boolean isDispatchSupported() {
        return dispatched.isDispatchSupported();
    }

    /**
     * @return
     * @see org.jinterop.dcom.core.IJIComObject#isLocalReference()
     */
    public boolean isLocalReference() {
        return dispatched.isLocalReference();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#put(int, org.jinterop.dcom.core.JIVariant)
     */
    public void put(int arg0, JIVariant arg1) throws JIException {
        dispatched.put(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#put(int, java.lang.Object[])
     */
    public void put(int arg0, Object[] arg1) throws JIException {
        dispatched.put(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#put(java.lang.String, org.jinterop.dcom.core.JIVariant)
     */
    public void put(String arg0, JIVariant arg1) throws JIException {
        dispatched.put(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#put(java.lang.String, java.lang.Object[])
     */
    public void put(String arg0, Object[] arg1) throws JIException {
        dispatched.put(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#putRef(int, org.jinterop.dcom.core.JIVariant)
     */
    public void putRef(int arg0, JIVariant arg1) throws JIException {
        dispatched.putRef(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#putRef(int, java.lang.Object[])
     */
    public void putRef(int arg0, Object[] arg1) throws JIException {
        dispatched.putRef(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#putRef(java.lang.String, org.jinterop.dcom.core.JIVariant)
     */
    public void putRef(String arg0, JIVariant arg1) throws JIException {
        dispatched.putRef(arg0, arg1);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws JIException
     * @see org.jinterop.dcom.impls.automation.IJIDispatch#putRef(java.lang.String, java.lang.Object[])
     */
    public void putRef(String arg0, Object[] arg1) throws JIException {
        dispatched.putRef(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @throws JIException
     * @see org.jinterop.dcom.core.IJIComObject#queryInterface(java.lang.String)
     */
    public IJIComObject queryInterface(String arg0) throws JIException {
        return dispatched.queryInterface(arg0);
    }

    /**
     * @param arg0
     * @see org.jinterop.dcom.core.IJIComObject#registerUnreferencedHandler(org.jinterop.dcom.common.IJIUnreferenced)
     */
    public void registerUnreferencedHandler(IJIUnreferenced arg0) {
        dispatched.registerUnreferencedHandler(arg0);
    }

    /**
     * @throws JIException
     * @see org.jinterop.dcom.core.IJIComObject#release()
     */
    public void release() throws JIException {
        dispatched.release();
    }

    /**
     * @param arg0
     * @see org.jinterop.dcom.core.IJIComObject#setInstanceLevelSocketTimeout(int)
     */
    public void setInstanceLevelSocketTimeout(int arg0) {
        dispatched.setInstanceLevelSocketTimeout(arg0);
    }

    /**
     * 
     * @see org.jinterop.dcom.core.IJIComObject#unregisterUnreferencedHandler()
     */
    public void unregisterUnreferencedHandler() {
        dispatched.unregisterUnreferencedHandler();
    }

}
