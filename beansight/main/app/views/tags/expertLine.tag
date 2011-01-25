*{ Display the info for a given expert  }*
*{ @param expert: the user  }*

#{userContainer user:_expert}
	    <div class="item-expert">
        <div class="content-expert">
        <!-- TODO COLORZ : image 41x41 -> sera 46x46 -->
            <a href="@{Application.showUser(_expert.userName)}">
                <div class="avatar-expert">
                    <img src="@{Application.showAvatar(_expert.id)}" alt="${_expert.userName}"/>
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
