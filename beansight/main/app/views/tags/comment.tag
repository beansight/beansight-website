<div class="item-comment first">
    <div class="user-comment">
        <a href="#" class="name-comment">${_comment.user.userName}</a>
        <span class="date-comment">${_comment.creationDate.since(true)}</span>
        <a href="#" class="avatar-comment"><img src="@{Application.showAvatarSmall(_comment.user.userName)}" alt=""/></a>
        *{ TODO COLORZ : display the user vote 
        <span class="vote-comment agreeaction">Vote</span>
        }*
        <hr class="clear"/>
    </div>
    <p class="txt-comment">${_comment.content.escape().nl2br()}</p>
    <hr class="clear"/>
</div>
