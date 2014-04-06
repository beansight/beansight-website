*{ widget that displays the list of user activity. Designed to be in the left sidebar }*
<div class="list-fav list-avatars favoriteUsers">
<h3>&{'followedUsers.title'}</h3>
#{list items:_followedUserActivities , as:'userActivity'}
<a href="@{Application.showUser(userActivity.followedUser.userName)}" class="item-avatar">
    <span class="backavatar"><img src="@{Application.showAvatarSmall(userActivity.followedUser.userName, userActivity.followedUser.avatarHashCode())}"/></span>
    #{if userActivity.newInsightCount > 0}  <span class="newInsightCount activityNotification"> ${userActivity.newInsightCount} </span>#{/if}
    #{if userActivity.newVoteCount > 0}     <span class="newVoteCount activityNotification">    ${userActivity.newVoteCount}    </span>#{/if}
    <p>${userActivity.followedUser.userName.abbreviate(11)}</p>
</a>
#{/list}
<hr class="clear"/>
</div>