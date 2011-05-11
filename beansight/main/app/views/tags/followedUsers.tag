<div class="list-avatars favoriteUsers">
#{list items:_followedUserActivities , as:'userActivity'}
<a href="@{Application.showUser(userActivity.followedUser.userName)}" class="item-avatar">
    <span class="backavatar"><img src="@{Application.showAvatarSmall(userActivity.followedUser.userName, userActivity.followedUser.avatarHashCode())}"/></span>
    #{if userActivity.newInsightCount > 0}  <span class="newInsightCount activityNotification"> ${userActivity.newInsightCount} </span>#{/if}
    #{if userActivity.newVoteCount > 0}     <span class="newVoteCount activityNotification">    ${userActivity.newVoteCount}    </span>#{/if} ${userActivity.totalCount}
    <p>${userActivity.followedUser.userName.abbreviate(11)}</p>
</a>
#{/list}
<hr class="clear"/>
</div>