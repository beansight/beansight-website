*{ Display the list of facebook friends who have an account on Beansight }*
*{ @param friends: facebook friend list  }*
*{ @param currentUser: the currently connected user  }*

#{if (_currentUser.facebookUserId != null)}
	#{if (!_friends.isEmpty())}
	<div id="facebookFriendList" style="margin-left: 50px;">
		<div class="item-avatar">
			<div style="float:right;">
				<span class="ajaxloader" style="display:none;"></span><button id="mngFbFriendFollowAll" onClick="followAllFacebookFriends(); return false;">&{'manageFacebookFriend.followAll'}</button>
			</div>
		</div>	
	
		#{list items:_friends , as:'friend'}
		<div id="managefb${friend.user.id}" class="item-avatar">
			<a href="@{Application.showUser(friend.beansightUserFriend.userName)}">
			    *{<span class="backavatar"><img src="@{Application.showAvatarMedium(friend.beansightUserFriend.userName, friend.beansightUserFriend.avatarHashCode())}"/></span>}*
			    <span class="backavatar"><img src="http://graph.facebook.com/${friend.facebookUser.facebookId}/picture" width="46" height="46"/></span>
			    <p>${friend.beansightUserFriend.userName}</p>
			</a>
			<div id="followHideFacebookFriend${friend.beansightUserFriend.id}" style="float:right;">
					<input 	type="checkbox" 
						name="followHideFbFriend${friend.beansightUserFriend.id}" 
						#{if (friend.isAdded)}checked#{/if} 
						id="followFbFriend${friend.beansightUserFriend.id}" 
						value="follow"
						data-buserid="${friend.beansightUserFriend.id}"
						 />
				<label for="followFbFriend${friend.beansightUserFriend.id}" style="font-size: 60%; margin-top: 6px;">&{'manageFacebookFriend.follow'}</label>
			</div>
		</div>	
		#{/list}
	</div>
	#{/if}
#{/if}