*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*

#{insightContainer insight:_insight}
<span class="insight-line">
<a href="@{Application.showInsight(_insight.id)}"><span class="date">${_insight.endDate.format("yyyy MM dd")},</span> <span class="content">${_insight.content}</span></a>

#{isConnected}
    #{agree-disagreeWidget insight:_insight/}
    #{followInsightWidget insight:_insight /}
#{/isConnected}
</span>
#{/insightContainer}