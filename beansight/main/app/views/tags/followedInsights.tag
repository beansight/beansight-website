<div class="list-fav">
#{list items:_followedInsights , as:'insight'}
    <div class="item-fav .insight_${insight.uniqueId} ">
        #{insight insight:insight /}
    </div>
#{/list}
    <hr class="clear"/>
</div>