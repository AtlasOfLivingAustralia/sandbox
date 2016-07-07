<%@ page contentType="text/html;charset=UTF-8" %>
<!-- Login Alert Modal -->
<div id="loginAlertModel" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">Please login to upload data</h4>
            </div>
            <div class="modal-body">
                <p>Please login to upload your data and visual through the Atlas.</p>
                <p><a href="${loginLink}">Click here</a> to login.</p>
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<!-- Not Authorised Alert Modal -->
<div id="notAuthorisedAlertModel" class="modal fade" tabindex="-1" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">You don't have the access to upload to this sandbox</h4>
            </div>
            <div class="modal-body">
                <p>This sandbox instance has been setup for a specific group of users.</p>
                <p>If you think you should have access, please send an email to ${grailsApplication.config.skin.supportEmail}</p>
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" data-dismiss="modal" aria-hidden="true">Close</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->