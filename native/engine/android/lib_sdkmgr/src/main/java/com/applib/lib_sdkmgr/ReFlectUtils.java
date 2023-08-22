package com.applib.lib_sdkmgr;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * reflect调用工具
 *
 */
public class ReFlectUtils {
    /**
     * get CLASS object
     * @param className
     * @return
     */
    public static Class<?> getClass(String className){
        try {
            Class<?> reClass = Class.forName(className);
            return reClass;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Object getStaticObject(Class<?> clz, String varName) {
        Object obj = null;
        try {
            Field field = clz.getDeclaredField(varName);
            obj = field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
    public static Object getStaticObject(String clz, String varName) {
        Object obj = null;
        try {
            Class managerClass = getClass(clz);
            Field field = managerClass.getDeclaredField(varName);
            obj = field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
    /**
     * excute method
     * @param className
     * @param methodName
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object processMethod(String className,String methodName) {
        Class manager = getClass(className);
        try {
            Method method = manager.getDeclaredMethod(methodName);
            return method.invoke(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     *excute method
     * @param className
     * @param methodName
     * @param params
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object processMethod(String className,String methodName,Class[] paramsTypes, Object[] params) {
        Class manager = getClass(className);
        try {
            Method method = manager.getDeclaredMethod(methodName,paramsTypes);
            return method.invoke(manager, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * excute method
     * @param classObject
     * @param methodName
     * @param paramsTypes
     * @param params
     * @return
     */
    public static Object processMethod(Object classObject,String methodName,Class[] paramsTypes, Object[] params) {

        if(classObject == null){
            return null;
        }

        Class manager = classObject.getClass();
        try {
            Method method = manager.getDeclaredMethod(methodName,paramsTypes);
            return method.invoke(classObject, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * excute method
     * @param classObject
     * @param methodName
     * @return
     */
    public static Object processMethod(Object classObject,String methodName) {
        Class manager = classObject.getClass();
        try {
            Method method = manager.getDeclaredMethod(methodName);
            return method.invoke(classObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * copy static method
     * @param className
     * @param varName
     * @param varValue
     */
    public static void setStaticValue(String className,String varName,Object varValue){
        Class manager = getClass(className);
        Field field;
        try {
            field = manager.getDeclaredField(varName);
            field.setAccessible(true);
            field.set(manager, varValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param manager
     * @param varName
     * @param varValue
     */
    public static void setStaticValue(Class manager,String varName,Object varValue){
        Field field;
        try {
            field = manager.getDeclaredField(varName);
            field.setAccessible(true);
            field.set(manager, varValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
