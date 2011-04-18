*{@param _userId : the ID of the user}*
*{@param _insightUniqueId : the uniqueId of the insight}*
*{@return a style <span> that describe the current vote for this user}*
%{ 
  String lastVote = "";
  String style = "";
  voteTargetUser = models.Vote.findLastVoteByUserAndInsight(_userId,  _insightUniqueId);
  if(voteTargetUser && voteTargetUser.state.equals(models.Vote.State.AGREE)) {
    style = "background-color: green;";
    lastVote = "You agree";
  } else if(voteTargetUser && voteTargetUser.state.equals(models.Vote.State.DISAGREE)) {
    style = "background-color: red;";
    lastVote = "You disagree";
  }
/}%
<spans style="${style}">${lastVote}</span>