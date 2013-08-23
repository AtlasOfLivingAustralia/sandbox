package au.org.ala.datacheck

import groovy.json.JsonSlurper

class FormatProcessedValueTagLib {

  static namespace = 'dc'

  def countByQaStatus = { attrs, body ->
      def count = 0
      attrs.assertions.each {
        if(it.qaStatus == attrs.qaStatus){
            count++
        }
      }
      out << count
  }

  def formatProperty = { attrs, body ->
    def messageSource = grailsAttributes.applicationContext.messageSource

    if(attrs.value?.startsWith("[") && attrs.value?.endsWith("]")){
        def js = new JsonSlurper()
        def json = js.parseText(attrs.value)
        def buff = []
        json.each { buff << messageSource.getMessage(it, null, it, Locale.getDefault()) }
        //convert to thumb URL
        out << buff.join(", ")
    } else {
        out <<  messageSource.getMessage(attrs.value, null, attrs.value, Locale.getDefault())
    }
  }
}
