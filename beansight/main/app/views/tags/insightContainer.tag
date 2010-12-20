*{ Display a single content for an insight  }*
*{ @param insigth: the insight  }*
*{ @param tag: a div or a span ? }*

#{isConnected}
*{ what is the vote of this user for this insight /}*
%{ 
      state = models.Vote.whatVoteForInsight(controllers.CurrentUser.getCurrentUser().id, _insight.id)
/}%
#{/isConnected}
%{ 
      if(_tag == null ) { _tag = "div" }
/}%
<${_tag} class="insight_${_insight.id} #{if state.equals(models.Vote.State.AGREE) } voteAgree #{/if } #{elseif state.equals(models.Vote.State.DISAGREE)} voteDisagree #{/elseif}">
#{doBody /}
</${_tag}>