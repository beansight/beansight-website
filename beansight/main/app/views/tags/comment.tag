%{ 
      vote = models.Vote.findLastVoteByUserAndInsight(_comment.user.id, _comment.insight.uniqueId);
      String voteClass = "";
      if(vote && vote.state.equals(models.Vote.State.AGREE)) {
        voteClass = "agreeaction";
      } else if(vote && vote.state.equals(models.Vote.State.DISAGREE)) {
        voteClass = "disagreeaction";
      }
/}%
<div class="item-comment first" id="insightComment_${_comment.id}">
    <div class="user-comment">
        <a href="@{Application.showUser(_comment.user.userName)}" class="name-comment">${_comment.user.userName}</a>
        <span class="date-comment">${_comment.creationDate.since(true)}</span>
        <a href="@{Application.showUser(_comment.user.userName)}" class="avatar-comment"><img src="@{Application.showAvatarSmall(_comment.user.userName, _comment.user.avatarHashCode())}" alt=""/></a>
        <span class="vote-comment ${voteClass}">Vote</span>
        <hr class="clear"/>
    </div>
    <p class="txt-comment">${_comment.content.escape().linkifyAll("style='font-style: italic;'").nl2br()}</p>
    #{isConnected}  
    	#{if controllers.CurrentUser.getCurrentUser().id == _comment.user.id && _comment.savedLessThanMinutesAgo(15)}  
    	<hr class="clear"/>
    	<div style="float: right;">
    		<a style="font-style: normal; font-size: 13px; color: #A1A5A6;" href="#" onclick="editComment('${_comment.insight.uniqueId}', ${_comment.id}); return false;" >&{'insights.commentTimeLeftToEdit', _comment.creationDate.still(15)}</a>
    	</div>
    	#{/if}
    #{/isConnected}
	<p>
	#{isConnected}
    	#{secure.check 'admin'}
    	<br>
		<a href="#" onclick="insightHideComment(${_comment.id}); return false;" style="color: red; font-size: 13px;">hide this comment</a>
		#{/secure.check}
	#{/isConnected}
	</p>

    <hr class="clear"/>
</div>
