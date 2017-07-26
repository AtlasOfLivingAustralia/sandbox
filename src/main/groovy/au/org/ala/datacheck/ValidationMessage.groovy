package au.org.ala.datacheck
class ValidationMessage {
    String   code
    String[] args
    String   message

    ValidationMessage(String code, String[] args) {
        this.code = code
        this.args = args
    }
}
