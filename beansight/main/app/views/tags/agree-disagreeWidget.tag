*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
<div class="vote-insight">
    <p class="vote-more"><a href="#" onClick="agree('${_insight.uniqueId}')" class="agreeCount">${_insight.agreeCount}</a></p>
    <p class="vote-less"><a href="#" onClick="disagree('${_insight.uniqueId}')" class="disagreeCount">${_insight.disagreeCount}</a></p>
</div>

<!-- TODO COLORZ was: class="icon agree interactive" -->

