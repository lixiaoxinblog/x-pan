package com.xiaoxin.pan.lock.core.key;

import com.xiaoxin.pan.lock.core.context.LockContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标准的key生成器
 */
public class StandardKeyGenerator extends AbstractKeyGenerator {
    /**
     * 执行执行key生成
     * 生成格式：classname:methodName:parameterTyp1:...:value1:value2:...
     *
     * @param lockContext
     * @param kvMap
     * @return
     */
    @Override
    protected String doGenerateKey(LockContext lockContext, Map<String, String> kvMap) {
        List<String> keyList = new ArrayList<>();
        keyList.add(lockContext.getClassName());
        keyList.add(lockContext.getMethodName());
        Class<?>[] parameterTypes = lockContext.getParameterTypes();
        if (ArrayUtils.isNotEmpty(parameterTypes)) {
            Arrays.stream(parameterTypes)
                    .forEach(e -> keyList.add(e.toGenericString()));
        }else{
            keyList.add(Void.class.toString());
        }
        Collection<String> values = kvMap.values();
        if (CollectionUtils.isNotEmpty(values)){
            keyList.addAll(values);
        }
        return String.join(",", keyList);
    }
}
