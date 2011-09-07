package ala.datacheck

class CamelCaseTagLib {

  def prettyCamel = { attr, body ->

    String i18nMessage = g.message([code:attr['value']])
    if(i18nMessage != null && !i18nMessage.isEmpty() && i18nMessage != attr['value']){
      out << i18nMessage
    } else if(attr['value'].contains("ID")){
      def value = attr['value'].replaceAll("ID", "")
      out << formatCamelCase(value) + " ID"
    } else {
      out << formatCamelCase(attr['value'])
    }
  }

  private def formatCamelCase(String original){
    def value =  original.replaceAll("([A-Z])") { " " + it[0] }
    value =  value.replaceFirst("([a-z])") { it[0].toUpperCase() }
    value
  }
}
