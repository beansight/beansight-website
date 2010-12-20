*{ Display the content of a vote activity  }*
*{ @param insightActivity: the insight activity object  }*

#{if _insightActivity.newFavoriteCount != 0}&{'activityfavorited', _insightActivity.newFavoriteCount},#{/if}

#{if _insightActivity.voteChangeCount != 0}&{'activitychanged', _insightActivity.voteChangeCount}#{/if}
#{if (_insightActivity.voteChangeCount != 0 && _insightActivity.newAgreeCount != 0 && _insightActivity.newDisagreeCount != 0)}, #{/if}

#{if (_insightActivity.voteChangeCount != 0 && (_insightActivity.newAgreeCount != 0 || _insightActivity.newDisagreeCount != 0))} &{'activityand'} #{/if}
#{if _insightActivity.newAgreeCount != 0}&{'activityagreed', _insightActivity.newAgreeCount}#{/if}

#{if (_insightActivity.newAgreeCount != 0 && _insightActivity.newDisagreeCount != 0)} &{'activityand'} #{/if}
#{if _insightActivity.newDisagreeCount != 0}&{'activitydisagreed',_insightActivity.newDisagreeCount}#{/if}

 &{'activitywith'}#{insight insight:_insightActivity.insight /}