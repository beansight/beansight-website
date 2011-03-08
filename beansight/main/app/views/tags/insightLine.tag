*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
#{insightContainer insight:_insight}
<div class="item-insight">
    <div class="content-insight">
    #{isConnected}
        #{agree-disagreeWidget insight:_insight/}
    #{/isConnected}
        <a href="@{Application.showInsight(_insight.uniqueId)}" class="permalink">
            <p class="date-insight" style="color: #A1A5A6">${_insight.endDate.in(true)}, </p>
            <h3>${_insight.content}</h3> 
            *{<p class="date-insight"> &{'insights.endDate'} ${_insight.endDate.format()} _insight.endDate.in(true)}</p>}*
            #{if _insight.comments.size() > 0}
            	<p class="date-insight" style="float: right;">(${_insight.comments.size()} &{'insights.comments', _insight.comments.size().pluralize2()})</p>
            #{/if}
        </a>
        <hr class="clear"/>
    </div>
</div>
#{/insightContainer}