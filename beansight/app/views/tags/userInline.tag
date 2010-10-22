*{ Display the name and teh avatar of the expert  }*
*{ @param user: the user  }*

<img width="30px" src="@{Application.showAvatar(_user.id)}"/>
<a href="@{Application.showUser(_user.id)}">${_user.userName}</a>
	
