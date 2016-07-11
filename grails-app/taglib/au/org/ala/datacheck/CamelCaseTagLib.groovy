package au.org.ala.datacheck

class CamelCaseTagLib {

  def camelCaseService

  def formatFieldName = { attr, body ->
    def fieldName = attr['value']
    out << camelCaseService.formatFieldName(fieldName)
  }

  def prettyCamel = { attr, body ->
    def value = attr['value]']
    out << camelCaseService.prettyCamel(value)
  }

}