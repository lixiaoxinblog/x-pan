package com.xiaoxin.pan.lock.core.key;

import com.xiaoxin.pan.core.utils.SpElUtil;
import com.xiaoxin.pan.lock.core.annotation.Lock;
import com.xiaoxin.pan.lock.core.context.LockContext;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认的key生成器
 */
public abstract class AbstractKeyGenerator implements KeyGenerator{

    /**
     * 生成锁的key
     * @param lockContext
     * @return
     */
    @Override
    public String generateKey(LockContext lockContext) {
        Lock annotation = lockContext.getAnnotation();
        String[] keys = annotation.keys();
        HashMap<String, String> kvMap = new HashMap<>();
        if (ArrayUtils.isNotEmpty(keys)){
            Arrays.stream(keys)
                    .forEach(key ->{
                        kvMap.put(key, SpElUtil.getStringValue(key,
                                lockContext.getClassName(),
                                lockContext.getMethodName(),
                                lockContext.getClassType(),
                                lockContext.getMethod(),
                                lockContext.getArgs(),
                                lockContext.getParameterTypes(),
                                lockContext.getTarget()));
                    });
        }
        return doGenerateKey(lockContext,kvMap);
    }

    /**
     * 子类去实现
     * @param kvMap
     * @return
     */
    protected abstract String doGenerateKey(LockContext lockContext, Map<String,String> kvMap) ;
}
