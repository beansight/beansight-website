*{ Display the vote info for a given insight }*
*{ @param insigth: the insight  }*
*{ @param insigth: the insight  }*
<!-- TODO COLORZ : agree disagree is broken -->
<div class="votezone">
    <div class="linkvote morevote"><a href="#" onClick="agree('${_insight.uniqueId}')" class="agreeCount">${_insight.agreeCount} <img src="/public/images/icon-more.png" alt=""/></a></div>
    <div class="linkvote lessvote"><a href="#" onClick="disagree('${_insight.uniqueId}')" class="disagreeCount">${_insight.disagreeCount} <img src="/public/images/icon-less.png" alt=""/></a></div>
    #{if _lastUserVote != null}
    <p>#{if _lastUserVote.state.equals(models.Vote.State.AGREE)} &{'youagree'} #{/if} #{else} &{'youdisagree'} #{/else} <span class="timevote">${_lastUserVote.creationDate.format("dd MMMM yyyy")}</span></p>
    #{/if}
</div>

