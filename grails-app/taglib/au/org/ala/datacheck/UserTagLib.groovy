package au.org.ala.datacheck

class UserTagLib {
    static defaultEncodeAs = [taglib:'html']
    static returnObjectForTags = ['userId']
    static namespace = "u"

    def authService

    def userId = { attrs, body ->
        return authService.userId
    }
}
