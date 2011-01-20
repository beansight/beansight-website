*{ Display the name and the avatar of the expert  }*
*{ @param user: the user  }*
<!-- avatar 26x26 -->
<a href="@{Application.showUser(_user.userName)}" class="item-avatar">
    <span class="backavatar"><img src="@{Application.showAvatar(_user.id)}"/></span>
    <p>${_user.userName}</p>
</a>