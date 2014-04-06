#{if _friendsToAdd.size() > 0}
<div id="followFbFriends" class="list-avatars-add">
<div class="invite">
    <p>&{'facebookFriendsSidebar.title'}</p>
</div>
#{list items:_friendsToAdd , as:'friend'}
    #{friendToAddMiniAvatarName user:friend /}
#{/list}
<div id="note" class="note">
    <p><a href="@{Application.manageFacebookFriendsFromSideBar()}">&{'manageFacebookFriend.menuLinkToFbMngPage'}</a></p>
</div>
<hr class="clear"/>
</div>
#{/if}