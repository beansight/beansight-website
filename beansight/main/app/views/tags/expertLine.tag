*{ Display the info for a given expert  }*
*{ @param expert: the user  }*

#{userContainer user:_expert}
	#{userInline user:_expert /}
	
	#{isConnected}
	    #{followUserWidget user:_expert/}
	#{/isConnected}

#{/userContainer}
