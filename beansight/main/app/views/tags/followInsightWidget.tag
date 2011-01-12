*{ A star to follow an insight  }*
*{ @param insigth: the insight  }*

<span onClick="toggleFollowingInsight('${_insight.uniqueId}')" >
<a href="" onClick="return false" title="add or remove this insight from your favorites" 
    class="icon favorite  #{if controllers.CurrentUser.getCurrentUser().isFollowingInsight(_insight)}active#{/if}#{else}inactive#{/else}"></a>
</span>
