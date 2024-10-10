package com.xiaoxin.pan.lock.core.context;

import com.xiaoxin.pan.lock.core.annotation.Lock;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 锁实体的上下文信息
 * 主要做切点的实体解析，为整体逻辑公用
 */
@Data
public class LockContext {

    /**
     * 切点方法所属类名称
     */
    private String className;
    /**
     * 切点方法的名称
     */
    private String methodName;
    /**
     * 切点方法上标注的制定义的锁注解
     */
    private Lock annotation;
    /**
     * 类的Class对象
     */
    private Class classType;
    /**
     * 当前调用的方法实体
     */
    private Method method;
    /**
     * 参数列表
     */
    private Object[] args;
    /**
     * 参数列表类型
     */
    private Class<?>[] parameterTypes;
    /**
     * 代理对象实体
     */
    private Object target;

    public static LockContext init(ProceedingJoinPoint proceedingJoinPoint) {
        LockContext lockContext = new LockContext();
        doInit(lockContext,proceedingJoinPoint);
        return lockContext;
    }

    private static void doInit(LockContext lockContext, ProceedingJoinPoint proceedingJoinPoint) {
        Signature signature = proceedingJoinPoint.getSignature();
        Method method = ((MethodSignature) signature).getMethod();
        lockContext.setArgs(proceedingJoinPoint.getArgs());
        lockContext.setTarget(proceedingJoinPoint.getTarget());
        lockContext.setClassType(signature.getDeclaringType());
        lockContext.setClassName(signature.getName());
        lockContext.setParameterTypes(((MethodSignature) signature).getParameterTypes());
        lockContext.setMethod(method);
        lockContext.setClassName(signature.getDeclaringTypeName());
        lockContext.setAnnotation(method.getAnnotation(Lock.class));
    }
}
