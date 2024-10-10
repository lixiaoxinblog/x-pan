package com.xiaoxin.pan.lock.core.key;

import com.xiaoxin.pan.lock.core.context.LockContext;

/**
 * 锁键生成器顶级接口
 */
public interface KeyGenerator {

    public String generateKey(LockContext lockContext);

}
