*{@param _userId : the ID of the user}*
*{@param _insightUniqueId : the uniqueId of the insight}*
*{@return "agree" or "disagree"}*
%{ 
  String lastVote = "";
  voteTargetUser = models.Vote.findLastVoteByUserAndInsight(_userId,  _insightUniqueId);
  if(voteTargetUser && voteTargetUser.state.equals(models.Vote.State.AGREE)) {
    lastVote = "agree";
  } else if(voteTargetUser && voteTargetUser.state.equals(models.Vote.State.DISAGREE)) {
    lastVote = "disagree";
  }
/}%
${lastVote}