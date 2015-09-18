package au.org.ala.datacheck
import grails.converters.JSON
import org.apache.commons.collections.map.LRUMap
import org.apache.commons.io.FileUtils

class TagService {

    static transactional = false
    
    static MAX_TAG_LENGTH = 15
    
    def grailsApplication
    
    Map cache = new LRUMap(10000)
    Object cacheLock = new Object()
    
    def put(String tag, Map params){
        
        //limit tag length
        tag = tag.length() > MAX_TAG_LENGTH ? tag.substring(0, MAX_TAG_LENGTH) : tag
        
        synchronized (cacheLock) {
            cache.put(tag, params)
        }
        
        //write file copy
        try {
            def tagFile = new File(grailsApplication.config.uploadFilePath + File.separatorChar + 'tags' + File.separatorChar + tag + '.json')
            if (!tagFile.getParentFile().exists()) tagFile.getParentFile().mkdirs()
            FileUtils.writeStringToFile(tagFile, (params as JSON).toString())
        } catch (err) {
            log.error('failed to write .tag.json for ' + params + ': ' + err.getMessage())
        }
    }

    def get(String tag){

        //limit tag length
        tag = tag.length() > MAX_TAG_LENGTH ? tag.substring(0, MAX_TAG_LENGTH) : tag

        def params
        synchronized (cacheLock) {
            params = cache.get(tag)
        }
        
        if (!params) {
            //search for file copy
            try {
                def tagFile = new File(grailsApplication.config.uploadFilePath + File.separatorChar + 'tags' + File.separatorChar + tag + '.json')
                if (tagFile.exists()) {
                    params = JSON.parse(FileUtils.readFileToString(tagFile)) as Map
                    synchronized (cacheLock) {
                        cache.put(tag, params)
                    }
                }
            } catch (err) {
                log.error('failed to read .tag.json for ' + tag + ': ' + err.getMessage())
            }
        }
        
        if (!params) {
            //put a placeholder in cache so repeat calls do not read from disk
            synchronized (cacheLock) {
                //was it added between now and the last cache lock?
                params = cache.get(tag)
                if (!params) {
                    params = [missing: true]
                    cache.put(tag, params)
                }
            }
        }
        
        params
    }
}