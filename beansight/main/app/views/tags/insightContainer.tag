*{ Display a single content for an insight  }*
*{ @param insigth: the insight  }*
*{ @param tag: a div or a span ? }*

#{isConnected}
*{ what is the vote of this user for this insight /}*
%{ 
      state = models.Vote.whatVoteForInsight(controllers.CurrentUser.getCurrentUser().id, _insight.uniqueId)
/}%
#{/isConnected}
%{ 
      if(_tag == null ) { _tag = "div" }
/}%
<${_tag} class="insight_${_insight.uniqueId} 
#{if state.equals(models.Vote.State.AGREE) } voteAgree #{/if } 
#{elseif state.equals(models.Vote.State.DISAGREE)} voteDisagree #{/elseif}
#{if _insight.validated }
     validated  
    #{if _insight.validationScore > controllers.Application.INSIGHT_VALIDATED_TRUE_MINVAL} validatedTrue #{/if}
    #{elseif _insight.validationScore < controllers.Application.INSIGHT_VALIDATED_TRUE_MINVAL} validatedFalse #{/elseif}
    #{else} validatedUnknow #{/else}
#{/if }
">
#{doBody /}
</${_tag}>