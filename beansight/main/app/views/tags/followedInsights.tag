<div class="list-fav">
#{list items:_followedInsights , as:'insight'}
    <div class="item-fav first">
    <a href="#">
        <div class="date-fav">
            <span class="date-day">20</span> / <span class="date-month">5</span>
        </div>
        <p class="title-fav">#{insight insight:insight /}</p>
    </a>
    </div>
#{/list}
    <hr class="clear"/>
</div>