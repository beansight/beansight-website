*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*

#{insightContainer insight:_insight}

<a href="@{Application.showInsight(_insight.id)}">${_insight.content}</a>

(by <a href="@{Application.showUser(_insight.creator.id)}">${_insight.creator.userName}</a>) 
#{isConnected}
    #{agree-disagreeWidget insight:_insight/}
    #{followInsightWidget insight:_insight /}
#{/isConnected}

#{/insightContainer}