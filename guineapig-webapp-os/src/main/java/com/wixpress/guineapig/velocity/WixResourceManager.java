package com.wixpress.guineapig.velocity;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceCache;
import org.apache.velocity.runtime.resource.ResourceManagerImpl;

/**
 * @author shaiyallin
 * @since 8/19/12
 */
public class WixResourceManager extends ResourceManagerImpl {

    @Override
    public synchronized void initialize(RuntimeServices rsvc) {
        super.initialize(rsvc);
        Object cacheInstance = rsvc.getProperty("resource.manager.cache.instance");
        if (cacheInstance != null && cacheInstance instanceof ResourceCache)
            this.globalCache = (ResourceCache) cacheInstance;
    }

    /**
     * This override is here to prevent duplicate calls to IO-intensive resource loader such as UrlResourceLoader. It
     * wraps calls to super#loadResource with a synchronized and double checking block in order to reduce outgoing calls,
     * trading them for blocks
     */
    @Override
    protected Resource loadResource(String resourceName, int resourceType, String encoding) throws ResourceNotFoundException, ParseErrorException {
        String resourceKey = resourceType + resourceName;
        synchronized (this) {
            Resource resource = globalCache.get(resourceKey);

            if (resource != null) {
                return resource;
            } else {
                resource = super.loadResource(resourceName, resourceType, encoding);
                if (resource.getResourceLoader().isCachingOn())
                {
                    globalCache.put(resourceKey, resource);
                }
                return resource;
            }
        }
    }
}
