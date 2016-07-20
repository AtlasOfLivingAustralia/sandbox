package au.org.ala.datacheck

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import grails.converters.JSON

class DarwinCoreService {

  static transactional = false
  def grailsApplication

  def autoComplete(String startsWith,Integer limit){

    def http = new HttpClient()
    def get = new GetMethod(grailsApplication.config.biocacheServiceUrl + "/index/fields")
    http.executeMethod(get)
    String jsonResponse = get.getResponseBodyAsString()

    def biocacheList = new ArrayList<String>()

    def biocacheJson = JSON.parse(jsonResponse)
    def list = biocacheJson.findAll{data -> data.dwcTerm != null && data.dwcTerm.startsWith(startsWith) && !data.dwcTerm.endsWith("_raw")}

    for (def it: list) {
      if (biocacheList.size() == limit) {
        break
      } else {
        biocacheList.add(it.dwcTerm)
      }
    }
    return biocacheList
  }

}
