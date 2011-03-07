*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
#{insightContainer insight:_insight}
<div class="item-insight">
    <div class="content-insight">
    #{isConnected}
        #{agree-disagreeWidget insight:_insight/}
    #{/isConnected}
        <a href="@{Application.showInsight(_insight.uniqueId)}" class="permalink">
            <h3>${_insight.endDate.in(true)}, ${_insight.content}</h3> 
            *{<p class="date-insight"> &{'insights.endDate'} ${_insight.endDate.format()} _insight.endDate.in(true)}</p>}*
            <p class="date-insight">(${_insight.comments.size()} &{'insights.comments', _insight.comments.size().pluralize2()})</p>
        </a>
        <hr class="clear"/>
    </div>
</div>
#{/insightContainer}