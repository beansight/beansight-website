<!-- ACTIVITY -->
<h2 class="azurme"><span class="cuf-grb">&{'activityaroundme'}</span></h2>
<div class="activity">
    <!-- With Activity -->
    <div id="insightActivity" #{if _insightActivities.isEmpty()}style="display:none;#{/if}>
    <p><a id="resetActivity" href="" onClick="resetInsightActivity(); return false;">&{'activityreset'}</a></p>
        #{list items:_insightActivities , as:'insightActivity'} 
            <p class="title-activity">#{insightActivity insightActivity:insightActivity /}</p>
        #{/list}
    </div>
    <!-- Without activity -->
    <div id="noInsightActivity" #{if !_insightActivities.isEmpty()}style="display:none;#{/if}">
    <p class="message">noInsightActivity</p>
    </div>
</div>
