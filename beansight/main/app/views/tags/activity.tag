<div id="insightActivity" #{if _insightActivities.isEmpty()}style="display:none;#{/if}>
    <p><a id="resetActivity" href="" onClick="resetInsightActivity(); return false;">&{'activityreset'}</a></p>
    <ul id="insightActivityList">
        #{list items:_insightActivities , as:'insightActivity'} 
            <li>#{insightActivity insightActivity:insightActivity /}</li>
        #{/list}
    </ul>
</div>
<div id="noInsightActivity" #{if !_insightActivities.isEmpty()}style="display:none;#{/if}">
   <p>&{'noInsightActivity'}</p>
</div>