*{ Display a single content for an insight  }*
*{ @param insigth: the insight  }*

#{insightContainer insight:_insight, tag:"span"}
    <a href="@{Application.showInsight(_insight.uniqueId)}">${_insight.content}</a>
#{/insightContainer}