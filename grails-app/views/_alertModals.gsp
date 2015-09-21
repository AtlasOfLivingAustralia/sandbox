<%@ page contentType="text/html;charset=UTF-8" %>
<!-- Login Alert Modal -->
<div id="loginAlertModel" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 class="myModalLabel">Please login to upload data</h3>
    </div>
    <div class="modal-body">
        <p>Please login to upload your data and visual through the Atlas.</p>
        <p><a href="${loginLink}">Click here</a> to login.</p>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    </div>
</div>

<!-- Not Authorised Alert Modal -->
<div id="notAuthorisedAlertModel" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 class="myModalLabel">You don't have the access to upload to this sandbox</h3>
    </div>
    <div class="modal-body">
        <p>This sandbox instance has been setup for a specific group of users.</p>
        <p>If you think you should have access, please send an email to ${grailsApplication.config.skin.supportEmail}</p>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    </div>
</div>