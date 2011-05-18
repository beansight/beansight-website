
<h2 class="greenme azurme"><span class="cuf-grb">&{'invitations.invitesomeonetitle'}</span></h2>
#{if controllers.CurrentUser.getCurrentUser().facebookUserId !=null}
	<ul class="menuleft">
    	<li><a href="@{Application.inviteYourFacebookFriendsOnBeansight()}" class="cuf-grb #{currentMenu 'inviteFacebookFriendsOnBeansight' /}">&{'inviteYourFacebookFriends'}</a>*{I18N}*</li>
	</ul>
#{/if}
#{else}
	#{if _invitationsLeft != 0}
<div id="invite" class="invite">
  <p>&{'invitesomeone'}</p> #{if _invitationsLeft != -1}<p>&{'invitationsleft', _invitationsLeft}</p>#{/if}
  <form id="inviteForm">
      <input type="email" name="email" id="emailInvite" placeholder="&{'inviteemailplaceholder'}" class="input"/>
      
      <div class="inputsubmit">
          <button id="submitinvite"><span class="backbutton"></span><span class="txtbutton cuf-invitation">&{'invitesubmit'}</span></button> 
      </div>
      <div id="inviteMessage" style="display:none;">
          <label for="message">&{'addmessage'}</label>
          <textarea name="message" id="inviteTextArea"></textarea>
      </div>
  </form>
  <p id="inviteconfirm" style="display:none;">&{'inviteconfirm'}</p>
</div>
#{/if}
#{/else}
