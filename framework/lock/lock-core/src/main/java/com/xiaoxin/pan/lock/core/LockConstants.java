package com.xiaoxin.pan.lock.core;

/**
 * 锁相关公用常亮类
 */
public interface LockConstants {
    /**
     * 公共lock的path
     */
    String X_PAN_LOCK = "r-pan-lock:";

    /**
     * 公共lock的path
     * 主要针对zk等节点型软件
     */
    String X_PAN_LOCK_PATH = "/x-pan-lock";

}
