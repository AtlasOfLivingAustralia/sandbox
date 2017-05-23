package au.org.ala.datacheck

import org.apache.commons.collections.map.LRUMap

/**
 * In memory cache of upload tags
 */
class TagService {

    static transactional = false

    static MAX_TAG_LENGTH = 15

    def grailsApplication

    Map cache = new LRUMap(10000)
    Object cacheLock = new Object()

    def put(String tag, String uid){

        //limit tag length
        tag = tag.length() > MAX_TAG_LENGTH ? tag.substring(0, MAX_TAG_LENGTH) : tag

        synchronized (cacheLock) {
            cache.put(tag, uid)
        }
    }

    def get(String tag){

        //limit tag length
        tag = tag.length() > MAX_TAG_LENGTH ? tag.substring(0, MAX_TAG_LENGTH) : tag

        def params
        synchronized (cacheLock) {
            params = cache.get(tag)
        }

        params
    }
}
