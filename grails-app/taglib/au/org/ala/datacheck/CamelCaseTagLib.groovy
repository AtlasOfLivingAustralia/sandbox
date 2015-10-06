package au.org.ala.datacheck

class CamelCaseTagLib {

  def formatFieldName = { attr, body ->
    def fieldName = attr['value']
    if(fieldName.endsWith("_s") || fieldName.endsWith("_i") || fieldName.endsWith("_d")){
      fieldName = fieldName.substring(0, fieldName.length() - 2)
    }
    out << formatCamelCase(fieldName.replaceAll('_', ' '))
  }

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
    def value = original.replaceAll("([A-Z])") { " " + it[0] }
    def parts = value.trim().split(" ")
    def buff = ""
    parts.eachWithIndex{ String entry, int idx ->
        if(idx>0)
            buff += " "
        buff += entry.capitalize()
    }
    buff
  }
}