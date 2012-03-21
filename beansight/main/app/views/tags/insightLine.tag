*{ Display the info for a given insight  }*
*{ @param insigth: the insight  }*
*{ @param targetUser: a specific user that we want to display the vote, leave null if no specific user }*
*{ @param suggest: suggest data }*
%{ 
    String voteTargetUserClass = "";
    if(_targetUser != null) {
      voteTargetUser = models.Vote.findLastVoteByUserAndInsight(_targetUser.id, _insight.uniqueId);
      if(voteTargetUser && voteTargetUser.state.equals(models.Vote.State.AGREE)) {
        voteTargetUserClass = "agreeaction";
      } else if(voteTargetUser && voteTargetUser.state.equals(models.Vote.State.DISAGREE)) {
        voteTargetUserClass = "disagreeaction";
      }
    }
/}%
#{insightContainer insight:_insight}
<div class="item-insight">
    <div class="content-insight">
    <div class="container-insight">
        #{agree-disagreeWidget insight:_insight/}
        <a href="@{Application.showInsight(_insight.uniqueId)}" class="permalink #{if _targetUser != null}leftbusy#{/if}">
            <p class="date-insight"><span class="datetxt">${_insight.generateDateLabel()},</span> </p> 
            <h3>${_insight.content}</h3> 

            #{if _targetUser != null}
            <span class="target-user">
                <span class="avatar-user"><img src="@{Application.showAvatarSmall(_targetUser.userName, _targetUser.avatarHashCode())}" alt="${_targetUser.userName}"/></span>
                <span class="vote-user ${voteTargetUserClass}">Vote ${_targetUser.userName}</span>
            </span>
            #{/if}
            #{if _insight.comments.size() > 0}
                <p class="comment-insight"><span class="nbcom">${_insight.comments.size()}</span></p>
            #{/if}
        </a>

        #{if _insight.sponsored}
            <p class="sponsor-insight"> 
                &{'insightLine.sponsoredby'}
                <a href="@{Application.showUser(_insight.sponsor.userName)}" class="sponsor">${_insight.sponsor.userName}</a>
            </p>
        #{/if}

        *{ Suggestions }*
        #{if _suggest != null }
            <div class="suggestedbecause">
            #{if _suggest.becauseFollowedUserCreated}
                <span class="created">
                    &{'insights.suggested.created'}
                    <a href="@{Application.showUser(_insight.creator.userName)}" class="s-author">
                        #{imgAvatar user:_insight.creator /}
                        ${_insight.creator.userName}
                    </a>
                </span>
            #{/if}
            #{if _suggest.becauseFollowedTag != null && !_suggest.becauseFollowedTag.isEmpty()}
                <span>&{'insights.suggested.tag'}
                #{list items:_suggest.becauseFollowedTag, as:'tag' }
                    <a href="@{Application.insights(null, null, null, tag.label, null)}" class="tag">${tag.label}</a>
                #{/list}
                </span>
            #{/if}
            #{if _suggest.becauseFollowedUserVoted != null && !_suggest.becauseFollowedUserVoted.isEmpty()}
                #{if _suggest.becauseFollowedUserVoted.size() > 1 || !_suggest.becauseFollowedUserVoted.contains(_insight.creator)}
                <span>&{'insights.suggested.voted'}</span>
                #{/if}
                #{list items:_suggest.becauseFollowedUserVoted, as:'userVoted' }
                    #{if userVoted != _insight.creator}
                    <a href="@{Application.showUser(userVoted.userName)}" class="s-author">#{imgAvatar user:userVoted /}</a>
                    #{/if}
                #{/list}
            #{/if}
            </div>
        #{/if}

        <hr class="clear"/>
    </div
    </div>
</div>
#{/insightContainer}
