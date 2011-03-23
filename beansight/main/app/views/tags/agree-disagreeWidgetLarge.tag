*{ Display the vote info for a given insight }*
*{ @param insigth: the insight  }*
*{ @param lastUserVote: the last vote of the current user for this insight }*
<div class="voteWidgetLarge">
    #{if _lastUserVote != null && !_insight.validated}
    <p class="smallText top">&{'agree-disagree.changeyourmind'}</p>
    #{/if}
    #{if _insight.validated}
    <p class="smallText top">&{'agree-disagree.validate'}</p>
    #{/if}
        
    <div class="linkvote agreeaction">
        #{isConnected}<a href="#" class="voteNumber clickvote" onClick="return agree('${_insight.uniqueId}');">#{/isConnected}
        #{isNotConnected}<span class="voteNumber loginTooltip" title="&{'agree-disagree.loginToVoteTooltip.agree'}"/>#{/isNotConnected}
            <span class="voteCount agreeCount">${_insight.agreeCount}</span> <img src="/public/images/icon-more.png" alt=""/>
        #{isConnected}</a>#{/isConnected}
        #{isNotConnected}</span>#{/isNotConnected}
        </div>
    <div class="linkvote disagreeaction">
        #{isConnected}<a href="#" class="voteNumber clickvote" onClick="return disagree('${_insight.uniqueId}');">#{/isConnected}
        #{isNotConnected}<span class="voteNumber loginTooltip" title="&{'agree-disagree.loginToVoteTooltip.disagree'}"/>#{/isNotConnected}
            <span class="voteCount disagreeCount">${_insight.disagreeCount}</span> <img src="/public/images/icon-less.png" alt=""/>
        #{isConnected}</a>#{/isConnected}
        #{isNotConnected}</span>#{/isNotConnected}
        </div>
    
    <p class="smallText bottom">
    #{isConnected}
	    #{if _lastUserVote != null}
	       <span id="lastVote">#{if _lastUserVote.state.equals(models.Vote.State.AGREE)} &{'youagree'} #{/if} #{else} &{'youdisagree'} #{/else} <span class="timevote">${_lastUserVote.creationDate.format("dd MMMM yyyy")}</span></span>
	    #{/if}
	    #{else}
	       #{if _insight.validated}
	       <span id="lastVote">&{'agree-disagree.youhavenotvalidatedyet'}</span>
	       #{/if}
	       #{else}
	       <span id="lastVote">&{'agree-disagree.youhavenotvotedyet'}</span>
	       #{/else}
	    #{/else}
    #{/isConnected}
    #{isNotConnected}
        <span>&{'agree-disagree.notconnected'}</span>
    #{/isNotConnected}
    </p>
</div>
