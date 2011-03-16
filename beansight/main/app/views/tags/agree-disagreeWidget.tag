*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
<div class="vote-insight">
    <p class="vote-more">
        <a href="#" #{isNotConnected}title="&{'agree-disagree.loginToVoteTooltip.agree'}"#{/isNotConnected} onClick="return #{isConnected}agree('${_insight.uniqueId}');#{/isConnected}#{isNotConnected}false;#{/isNotConnected}" class="agreeCount loginTooltip">
            ${_insight.agreeCount}
        </a>
    </p>
    <p class="vote-less">
        <a href="#" #{isNotConnected}title="&{'agree-disagree.loginToVoteTooltip.disagree'}"#{/isNotConnected} onClick="return #{isConnected}disagree('${_insight.uniqueId}');#{/isConnected}#{isNotConnected}false;#{/isNotConnected}" class="disagreeCount loginTooltip">
            ${_insight.disagreeCount}
        </a>
    </p>
</div>

<!-- TODO COLORZ was: class="icon agree interactive" -->

