*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
<div class="vote-insight">
    <p class="vote-more">
        <a #{isNotConnected}href="@{Register.register()}"#{/isNotConnected} #{isConnected}href="#"#{/isConnected} #{isNotConnected}title="&{'agree-disagree.loginToVoteTooltip.agree'}"#{/isNotConnected} #{isConnected}onClick="return agree('${_insight.uniqueId}');"#{/isConnected} class="agreeCount loginTooltip">
            ${_insight.agreeCount}
        </a>
    </p>
    <p class="vote-less">
        <a #{isNotConnected}href="@{Register.register()}"#{/isNotConnected} #{isConnected}href="#"#{/isConnected} #{isNotConnected}title="&{'agree-disagree.loginToVoteTooltip.disagree'}"#{/isNotConnected} #{isConnected}onClick="return disagree('${_insight.uniqueId}');"#{/isConnected} class="disagreeCount loginTooltip">
            ${_insight.disagreeCount}
        </a>
    </p>
</div>

<!-- TODO COLORZ was: class="icon agree interactive" -->

