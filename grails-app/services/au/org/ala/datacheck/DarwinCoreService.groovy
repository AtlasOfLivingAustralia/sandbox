package au.org.ala.datacheck

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import grails.converters.JSON
import grails.plugin.cache.Cacheable

class DarwinCoreService {

  static transactional = false
  def grailsApplication

  @Cacheable('sandboxCache')
  def List getBiocacheList() {
    def biocacheList = new ArrayList<String>()
    try {
      def http = new HttpClient()
      def get = new GetMethod(grailsApplication.config.biocacheServiceUrl + "/index/fields")
      http.executeMethod(get)
      String jsonResponse = get.getResponseBodyAsString()
      def biocacheJson = JSON.parse(jsonResponse)
      biocacheList = biocacheJson.findAll{data -> data.dwcTerm != null && !data.dwcTerm.endsWith("_raw")}
      return biocacheList
    } catch(Exception e){
      log.error("Problem retrieving DWC terms from Biocache-service: " + e.getMessage(), e)
      biocacheList
    }

  }

  def List autoComplete(String startsWith,Integer limit){

    // call getBiocacheList via proxy application bean because we call the cached method internally
    // Reason is described here: http://gpc.github.io/grails-springcache/docs/guide/3.%20Caching%20Service%20Methods.html#3.2%20Calling%20Cached%20Methods%20Internally
    def list =  grailsApplication.mainContext.darwinCoreService.getBiocacheList().grep{it.dwcTerm.startsWith(startsWith)}.collect{it.dwcTerm}

    log.debug("Full DWC Terms List: " + list)

    if (list.size() > 10 && limit > 0) {
      if (limit < 0) {
        return list.subList(0, 0)
      } else {
        return list.subList(0, limit)
      }
    } else {
      return list
    }

  }

}
