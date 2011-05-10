<div class="list-fav activity favoriteInsights">
#{list items:_followedInsightActivities , as:'insightActivity'}
    <p class="title-activity">#{insightActivity insightActivity:insightActivity /}</p>
#{/list}
	<hr class="clear"/>
</div>