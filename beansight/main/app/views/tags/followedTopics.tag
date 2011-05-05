<div class="list-fav favoriteTopics">
#{list items:_followedTopicActivities , as:'followedTopicActivity'}
    <div class="item-fav">
        #{if followedTopicActivity.newInsightCount > 0}<span class="newInsightCount activityNotification">${followedTopicActivity.newInsightCount}</span>#{/if}
        <a href="@{Application.insights(null, null, null, followedTopicActivity.topic.label, null)}">${followedTopicActivity.topic.label}</a>
    </div>
#{/list}
    <hr class="clear"/>
</div>