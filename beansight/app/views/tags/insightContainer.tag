*{ Display a single content for an insight  }*
*{ @param insigth: the insight  }*

#{isConnected}
*{ what is the vote of this user for this insight /}*
%{ 
      state = models.Vote.whatVoteForInsight(controllers.CurrentUser.getCurrentUser().id, _insight.id)
/}%
#{/isConnected}

<div id="insight_${_insight.id}" class="#{if state.equals(models.Vote.State.AGREE) } voteAgree #{/if } #{elseif state.equals(models.Vote.State.DISAGREE)} voteDisagree #{/elseif}">
#{doBody /}
</div>