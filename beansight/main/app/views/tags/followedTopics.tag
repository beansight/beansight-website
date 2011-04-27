<div class="list-fav">
#{list items:_followedTopics , as:'followedTopic'}
    <div class="item-fav">
        <a href="@{Application.insights(null, null, null, followedTopic.label, null)}">${followedTopic.label}</a>
    </div>
#{/list}
    <hr class="clear"/>
</div>