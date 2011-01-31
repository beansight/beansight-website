*{ Display the info for a given expert  }*
*{ @param expert: the user  }*

#{userContainer user:_expert}
	    <div class="item-expert">
        <div class="content-expert">
            <a href="@{Application.showUser(_expert.userName)}">
                <div class="avatar-expert">
                    <img src="@{Application.showAvatarMedium(_expert.userName)}" alt="${_expert.userName}"/>
                </div>
                <div class="resume-expert">
                    <h3>${_expert.userName}</h3>
                    <p>${_expert.description}</p>
                </div>
            </a>
            <hr class="clear borderme"/>
        </div>
    </div>
	
	<!-- TODO COLORZ -->
	<!-- enlever la class borderme pour le dernier -->
    <hr class="clear"/>
#{/userContainer}
