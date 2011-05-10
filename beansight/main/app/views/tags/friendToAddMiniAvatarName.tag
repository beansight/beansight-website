*{ Display the name and the avatar of the expert  }*
*{ @param user: the user  }*
*{
<div id="fb${_user.id}">
	<a href="@{Application.showUser(_user.userName)}" class="item-avatar">
	    <span class="backavatar"><img src="@{Application.showAvatarSmall(_user.userName, _user.avatarHashCode())}"/></span>
	    <p>${_user.userName.abbreviate(11)}</p>
	</a>
	<div class="item-avatar">
		<a class="addfavfb" onClick="addUserToMyFavorites(${_user.id}); return false;" href="#"></a>
		<a class="removefavfb" href="#"></a>
	</div>
</div>
}*

<div id="fb${_user.id}" class="item-avatar">
	<a href="@{Application.showUser(_user.userName)}">
	    *{<span class="backavatar"><img src="@{Application.showAvatarSmall(_user.userName, _user.avatarHashCode())}"/></span>}*
	    <span class="backavatar"><img src="http://graph.facebook.com/${_user.facebookUserId}/picture" width="26" height="26"/></span>
	    <p>${_user.userName.abbreviate(12)}</p>
	</a>
	<button onClick="hideUserFromSuggestedFriends(${_user.id}); return false;" class="addfavfb-btn" style="font-size: 50%; margin-top: 6px; float: right;">&{'facebookFriendsSidebar.hide'}</button>
	<button onClick="addUserToMyFavorites(${_user.id}); return false;" class="addfavfb-btn" style="font-size: 50%; margin-top: 6px; float: right;">&{'facebookFriendsSidebar.follow'}</button>
</div>