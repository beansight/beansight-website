*{@param _insight : the insight to link to}*
*{@return an anchor <a> that links to the insight page}*
<a href="${play.configuration.getProperty("domain.name")}@{Application.showInsight(_insight.uniqueId)}">${_insight.content}</a>