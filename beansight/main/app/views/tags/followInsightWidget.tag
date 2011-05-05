*{ A star to follow an insight  }*
*{ @param insigth: the insight  }*
<a href="#" title="&{'showInsight.addInsightToFavorite'}" class="addfav insight #{if controllers.CurrentUser.getCurrentUser().isFollowingInsight(_insight)}active#{/if}" onClick="return toggleFollowingInsight('${_insight.uniqueId}');">Add to favorites</a>