package au.org.ala.datacheck

class FormatProcessedValueTagLib {

  static namespace = 'dc'

  def formatService

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
    formatService.formatProperty(attrs.value)
  }
}
