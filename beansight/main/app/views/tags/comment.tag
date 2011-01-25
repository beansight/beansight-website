<div class="item-comment first">
    <div class="user-comment">
        <a href="#" class="name-comment">${_comment.user}</a>
        <span class="date-comment">${_comment.creationDate.since(true)}</span>
        <!-- TODO COLORZ : display the user vote -->
        <!-- 
        <a href="#" class="avatar-comment"><img src="images/content/sample-26x26.jpg" alt=""/></a>
        <span class="vote-comment agreeaction">Vote</span>
         -->
        <hr class="clear"/>
    </div>
    <p class="txt-comment">${_comment.content.escape().nl2br()}</p>
    <hr class="clear"/>
</div>