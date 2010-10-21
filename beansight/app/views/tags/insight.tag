*{ Display a single content for an insight  }*
*{ @param insigth: the insight  }*

#{insightContainer insight:_insight}
    <a href="@{Application.showInsight(_insight.id)}">${_insight.content}</a>
#{/insightContainer}