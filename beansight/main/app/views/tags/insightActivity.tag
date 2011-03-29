*{ Display the content of a vote activity  }*
*{ @param insightActivity: the insight activity object  }*

#{if _insightActivity.newFavoriteCount != 0}&{'activityfavorited', _insightActivity.newFavoriteCount},#{/if}

#{if _insightActivity.voteChangeCount != 0}
    ${_insightActivity.voteChangeCount} <span class="acnewavis acicon">&{'activitychanged'}</span>
#{/if}

#{if _insightActivity.newAgreeCount != 0}
    ${_insightActivity.newAgreeCount} <span class="acagree acicon">&{'activityagreed'}</span>
#{/if}

#{if _insightActivity.newDisagreeCount != 0}
    ${_insightActivity.newDisagreeCount} <span class="acdisagree acicon">&{'activitydisagreed'}</span>
#{/if}

#{insight insight:_insightActivity.insight /}