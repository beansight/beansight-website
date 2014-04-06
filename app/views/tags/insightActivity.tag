*{ Display the content of a vote activity  }*
*{ @param insightActivity: the insight activity object  }*

*{
#{if _insightActivity.newFavoriteCount != 0}&{'activityfavorited', _insightActivity.newFavoriteCount},#{/if}
}*

#{if _insightActivity.newAgreeCount != 0}
    <span class="goodpts">${_insightActivity.newAgreeCount}</span><!-- &{'activityagreed'} -->
#{/if}

#{if _insightActivity.newDisagreeCount != 0}
    <span class="badpts">${_insightActivity.newDisagreeCount}</span><!-- &{'activitydisagreed'} -->
#{/if}

#{if _insightActivity.voteChangeCount != 0}
    <span class="changedpts">${_insightActivity.voteChangeCount}</span><!-- &{'activitychanged'} -->
#{/if}

#{insight insight:_insightActivity.insight /}