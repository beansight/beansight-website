*{ Display the info for a given expert  }*
*{ @param expert: the user  }*

#{userContainer user:_expert}
	<img width="30px" src="@{Application.showAvatar(_expert.id)}"/>
	<a href="@{Application.showUser(_expert.id)}">${_expert.userName}</a>
	
	#{isConnected}
	    #{followUserWidget user:_expert/}
	#{/isConnected}

#{/userContainer}
