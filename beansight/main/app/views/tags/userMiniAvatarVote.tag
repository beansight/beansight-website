*{ Display the avatar of the user and his vote }*
*{ @param user: the user  }*
*{ @param vote: the vote  }*
<a href="@{Application.showUser(_vote.user.userName)}" class="item-avatar">
    <span class="backavatar #{if _vote.state == models.Vote.State.AGREE} agree #{/if}#{else} disagree #{/else}"><img src="@{Application.showAvatarSmall(_vote.user.userName)}"/></span>
</a>