*{ A star to follow an insight  }*
*{ @param insigth: the insight  }*
<a href="#" class="addfav #{if controllers.CurrentUser.getCurrentUser().isFollowingInsight(_insight)}active#{/if}" onClick="return toggleFollowingInsight('${_insight.uniqueId}');">Add to favorites</a>