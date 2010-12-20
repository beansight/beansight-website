*{ A star to follow a user  }*
*{ @param user: the user  }*

<span onClick="toggleFollowingUser(${_user.id})" >
<a href="" onClick="return false" title="add or remove this user from your favorites" 
    class="icon favorite  #{if controllers.CurrentUser.getCurrentUser().isFollowingUser(_user)}active#{/if}#{else}inactive#{/else}"></a>
</span>
