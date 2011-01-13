*{ Display the name and the avatar of the expert  }*
*{ @param user: the user  }*

<img width="30px" src="@{Application.showAvatar(_user.id)}"/>
<a href="@{Application.showUser(_user.userName)}">${_user.userName}</a>
	
