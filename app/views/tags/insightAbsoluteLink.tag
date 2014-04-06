*{@param _insight : the insight to link to}*
*{@param _abbreviate : should the content be abbreaviated}*
*{@return an anchor <a> that links to the insight page}*
<a href="${play.configuration.getProperty("domain.name")}@{Application.showInsight(_insight.uniqueId)}">#{if _abbreviate}${_insight.content.abbreviate(40)}#{/if}#{else}${_insight.content}#{/else}</a>