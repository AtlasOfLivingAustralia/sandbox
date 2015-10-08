package au.org.ala.datacheck

import grails.transaction.Transactional
import groovy.json.JsonSlurper

@Transactional
class LayerService {

    def serviceMethod() {}

    def getLayers(){
        def js = new JsonSlurper()
        js.parseText(new URL("http://spatial.ala.org.au/ws/layers").getText("UTF-8"))
    }
}
