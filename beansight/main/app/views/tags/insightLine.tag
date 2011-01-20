*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
#{insightContainer insight:_insight}
<div class="item-insight">
    <div class="content-insight">
    #{isConnected}
        #{agree-disagreeWidget insight:_insight/}
    #{/isConnected}
        <a href="@{Application.showInsight(_insight.uniqueId)}" class="permalink">
            <h3>${_insight.content}</h3>
            <p class="date-insight">${_insight.endDate.format("yyyy MM dd")}</p>
        </a>
        <hr class="clear"/>
    </div>
</div>
#{/insightContainer}