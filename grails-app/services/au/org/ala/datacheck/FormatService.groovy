package au.org.ala.datacheck

import groovy.json.JsonSlurper
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder

class FormatService {

    def messageSource

    def formatFieldName(String fieldName) {
        if(fieldName.endsWith("_s") || fieldName.endsWith("_i") || fieldName.endsWith("_d")){
            fieldName = fieldName.substring(0, fieldName.length() - 2)
        }
        formatCamelCase(fieldName.replaceAll('_', ' '))
    }

    def prettyCamel(String value) {

        String i18nMessage
        try {
            i18nMessage = messageSource.getMessage(value, null, LocaleContextHolder.locale)
        } catch (NoSuchMessageException e) { }
        if (i18nMessage != null && !i18nMessage.isEmpty() && i18nMessage != value) {
            return i18nMessage
        } else if(value.contains("ID")){
            return formatCamelCase(value.replaceAll("ID", "")) + " ID"
        } else {
            return formatCamelCase(value)
        }
    }

    private def formatCamelCase(String original){
        def value = original.replaceAll("([A-Z])") { " " + it[0] }
        def parts = value.trim().split(" ")
        def buff = ""
        parts.eachWithIndex { String entry, int idx ->
            if(idx>0)
                buff += " "
            buff += entry.capitalize()
        }
        buff
    }

    def formatProperty(String value) {

        if (value?.startsWith("[") && value?.endsWith("]")) {
            def js = new JsonSlurper()
            def json = js.parseText(value)
            def buff = []
            json.each { buff << messageSource.getMessage(it, null, it, Locale.getDefault()) }
            //convert to thumb URL
            return buff.join(", ")
        } else {
            return messageSource.getMessage(value, null, value, Locale.getDefault())
        }
    }

}
