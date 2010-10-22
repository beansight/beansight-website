*{ Display the info for a given expert  }*
*{ @param expert: the user  }*

<a href="@{Application.showUser(_expert.id)}">${_expert.userName}</a>
#{isConnected}
#{/isConnected}
