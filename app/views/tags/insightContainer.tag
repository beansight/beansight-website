*{ A wrapper for every Insight, add the right classes "agree, validated, over"  }*
*{ @param insigth: the insight  }*
*{ @param tag: a div or a span ? }*

#{isConnected}
*{ what is the vote of this user for this insight /}*
%{ 
      vote = models.Vote.findLastVoteByUserAndInsight(controllers.CurrentUser.getCurrentUser().id, _insight.uniqueId);
/}%
#{/isConnected}
%{ 
      if(_tag == null ) { _tag = "div"; }
/}%
<${_tag} class='insightContainer insight_${_insight.uniqueId} 
#{if vote && vote.state.equals(models.Vote.State.AGREE) } voteAgree #{/if } 
#{elseif vote && vote.state.equals(models.Vote.State.DISAGREE)} voteDisagree #{/elseif}
#{if _insight.validated }
     validated  
    #{if        _insight.isValidatedTrue()      } validatedTrue     #{/if}
    #{elseif    _insight.isValidatedFalse()     } validatedFalse    #{/elseif}
    #{elseif    _insight.isValidatedUnknown()   } validatedUnknown  #{/elseif}
#{/if }
#{if _insight.endDate.getTime() < new Date().getTime()}
     over  
#{/if}
#{if _insight.sponsored}
    sponsored  
#{/if}
'>
#{doBody /}
</${_tag}>