<a href="@{Application.showInsight(_insight.id)}">${_insight.content}</a> 
(by <a href="@{Application.showUser(_insight.creator.id)}">${_insight.creator.userName}</a>) 
<a href="@{Application.agree(_insight.id)}">Agree</a> (${_insight.agreeCount}) 
<a href="@{Application.disagree(_insight.id)}">Disagree</a> (${_insight.disagreeCount})
#{isConnected}
	#{if _insight.isCreator(_currentUser)==false}
	    #{if _currentUser.isFollowingInsight(_insight)}
	        <a href="@{Application.stopFollowingInsight(_insight.id)}">stop following this insight</a>
	    #{/if}
	    #{else} 
	        <a href="@{Application.startFollowingInsight(_insight.id)}">start following this insight</a>
	    #{/else}
	#{/if}
#{/isConnected}