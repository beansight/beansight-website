*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
*{ @param display: quantity of information that will be displayed ["SMALL", "NORMAL"] default to "NORMAL" }*

#{isConnected}
*{ what is the vote of this user for this insight /}*
%{ 
      state = models.Vote.whatVoteForInsight(controllers.CurrentUser.getCurrentUser().id, _insight.id)
/}%
#{/isConnected}

<div id="insight_${_insight.id}" class="#{if state.equals(models.Vote.State.AGREE) } voteAgree #{/if } #{elseif state.equals(models.Vote.State.DISAGREE)} voteDisagree #{/elseif}">
<a href="@{Application.showInsight(_insight.id)}">${_insight.content}</a>

#{if _display != "SMALL"}

	(by <a href="@{Application.showUser(_insight.creator.id)}">${_insight.creator.userName}</a>) 
	<span onClick="agree(${_insight.id})" ><a href="" onClick="return false" class="icon agree"></a></span> (<span id="agreeCount">${_insight.agreeCount}</span>)
	<span onClick="disagree(${_insight.id})" ><a href="" onClick="return false" class="icon disagree"></a></span> (<span id="disagreeCount">${_insight.disagreeCount}</span>)

	#{isConnected}
	
		#{if _insight.isCreator(controllers.CurrentUser.getCurrentUser())==false}
		    #{if controllers.CurrentUser.getCurrentUser().isFollowingInsight(_insight)}
		        <a href="@{Application.stopFollowingInsight(_insight.id)}" title="remove this insight from favorites" class="icon favorite active"></a>
		    #{/if}
		    #{else} 
		        <a href="@{Application.startFollowingInsight(_insight.id)}" title="add this insight to your favorites" class="icon favorite inactive"></a>
		    #{/else}
		#{/if}
		
	#{/isConnected}
#{/if}
</div>