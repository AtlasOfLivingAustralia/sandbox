package au.org.ala.datacheck

import grails.converters.JSON
import org.jasig.cas.client.authentication.AttributePrincipal

class UserTagLib {
    static defaultEncodeAs = 'html'
    static encodeAsForTags = [roles: 'raw']
    static returnObjectForTags = ['userId']
    static namespace = "u"

    def authService

    def userId = { attrs, body ->
        return authService.userId
    }

    def roles = { attrs, body ->
        authService.userInRole()
        def up = request.userPrincipal
        def authority
        if (up instanceof AttributePrincipal) {
            authority = ((String)up.attributes['authority'])?.split(',')?.collect { it.toUpperCase() } ?: []
        } else {
            authority = []
        }
        def json = authority as JSON
        out << json
    }
}
