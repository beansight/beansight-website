*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
*{ @param display: quantity of information that will be displayed ["SMALL", "NORMAL"] default to "NORMAL" }*

<a href="@{Application.showInsight(_insight.id)}">${_insight.content}</a>

#{if _display != "SMALL"}
	(by <a href="@{Application.showUser(_insight.creator.id)}">${_insight.creator.userName}</a>) 
	<span onClick="agree(${_insight.id})" ><a href="" onClick="return false">Agree</a></span> (<span id="agreeCount_${_insight.id}">${_insight.agreeCount}</span>) 
	<span onClick="disagree(${_insight.id})" ><a href="" onClick="return false">Disagree</a></span> (<span id="disagreeCount_${_insight.id}">${_insight.disagreeCount}</span>)
	#{isConnected}
		#{if _insight.isCreator(controllers.CurrentUser.getCurrentUser())==false}
		    #{if controllers.CurrentUser.getCurrentUser().isFollowingInsight(_insight)}
		        <a href="@{Application.stopFollowingInsight(_insight.id)}" title="remove this insight from favorites" class="favorite active"></a>
		    #{/if}
		    #{else} 
		        <a href="@{Application.startFollowingInsight(_insight.id)}" title="add this insight to your favorites" class="favorite inactive"></a>
		    #{/else}
		#{/if}
	#{/isConnected}
#{/if}