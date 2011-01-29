*{ Display the vote info for a given insight }*
*{ @param insigth: the insight  }*
<!-- TODO COLORZ : agree disagree is broken -->
<div class="voteWidgetLarge">
    <div class="linkvote agreeaction"><a href="#" onClick="return agree('${_insight.uniqueId}');"><span class="voteCount agreeCount">${_insight.agreeCount}</span> <img src="/public/images/icon-more.png" alt=""/></a></div>
    <div class="linkvote disagreeaction"><a href="#" onClick="return disagree('${_insight.uniqueId}');"><span class="voteCount disagreeCount">${_insight.disagreeCount}</span> <img src="/public/images/icon-less.png" alt=""/></a></div>
    #{if _lastUserVote != null}
    <p>#{if _lastUserVote.state.equals(models.Vote.State.AGREE)} &{'youagree'} #{/if} #{else} &{'youdisagree'} #{/else} <span class="timevote">${_lastUserVote.creationDate.format("dd MMMM yyyy")}</span></p>
    #{/if}
</div>
