*{ Display the avatar of the user and his vote }*
*{ @param user: the user  }*
*{ @param vote: the vote  }*
<!-- TODO COLORZ avatar 26x26 -->
<a href="@{Application.showUser(_vote.user.userName)}" class="item-avatar">
    <span class="backavatar #{if _vote.state == models.Vote.State.AGREE} agree #{/if}#{else} disagree #{/else}"><img src="@{Application.showAvatar(_vote.user.id)}"/></span>
</a>