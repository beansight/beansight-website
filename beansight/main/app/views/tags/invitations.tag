#{if _invitationsLeft != 0}
<div id="invite">
    <p>&{'invitesomeone'}</p> #{if _invitationsLeft != -1}<p>&{'invitationsleft', _invitationsLeft}</p>#{/if}
    <form id="inviteForm">
        <input type="email" name="email" id="emailInvite" placeholder="&{'inviteemailplaceholder'}"/>
        <input type="submit" name="submit" value="&{'invitesubmit'}" />
        <div id="inviteMessage" style="display:none;">
            <label for="message">&{'addmessage'}</label>
            <textarea name="message" id="inviteTextArea"></textarea>
        </div>
    </form>
    <p id="inviteconfirm" style="display:none;">&{'inviteconfirm'}</p>
</div>
#{/if}