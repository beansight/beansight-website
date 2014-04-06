#{if (_user.facebookUserId != null)}
    <a href="@{Application.manageFacebookFriendsWithSynchronization()}" style="font-size: 80%">&{'manageFacebookFriend.menuLinkToFbMngPage'}</a>
#{/if}
#{else}
    <a href="@{Register.linkBeansightAccountWithFacebook()}" class="social_buttons sb_facebook sb_24">
        <span>&{'settings.linkFacebookOnBeansight'}</span>
    </a>
#{/else}