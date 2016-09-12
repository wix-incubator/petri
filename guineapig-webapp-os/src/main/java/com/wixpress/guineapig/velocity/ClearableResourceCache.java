package com.wixpress.guineapig.velocity;

import org.apache.velocity.runtime.resource.ResourceCacheImpl;

/**
 * @author shaiyallin
 * @since 8/19/12
 */
public class ClearableResourceCache extends ResourceCacheImpl {

    public void removeAll() {
        cache.clear();
    }
}
