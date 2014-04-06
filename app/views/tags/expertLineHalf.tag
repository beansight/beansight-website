*{ Display the info for a given expert, the size of the line is half the size of the content }*
*{ @param expert: the user  }*

#{userContainer user:_expert}
<div class="content-expert">
    <a href="@{Application.showUser(_expert.userName)}">
        <p class="index-expert">${_index}</p>
        <div class="avatar-expert">
            <img src="@{Application.showAvatarMedium(_expert.userName, _expert.avatarHashCode())}" alt="${_expert.userName}"/>
        </div>
        <div class="resume-expert">
            <h3>${_expert.userName}</h3>
            <p>${_expert.description}</p>
        </div>
    </a>
</div>

<hr class="clear"/>

#{/userContainer}
