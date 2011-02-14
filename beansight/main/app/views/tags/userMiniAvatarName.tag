*{ Display the name and the avatar of the expert  }*
*{ @param user: the user  }*
<a href="@{Application.showUser(_user.userName)}" class="item-avatar">
    <span class="backavatar"><img src="@{Application.showAvatarSmall(_user.userName)}"/></span>
    <p>${_user.userName.abbreviate(12)}</p>
</a>
