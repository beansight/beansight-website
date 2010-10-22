*{ Display the the user and his vote  }*
*{ @param vote: the vote  }*

#{userInline user:_vote.user /} <span href="" onClick="return false" class="icon #{if _vote.state == models.Vote.State.AGREE} agree voteAgree #{/if}#{else} disagree voteDisagree#{/else}"></span>
	
