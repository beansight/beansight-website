*{@param _userId : the ID of the user}*
*{@param _insightUniqueId : the uniqueId of the insight}*
*{@return a style <span> that describe the current vote for this user}*
%{ 
  String lastVote = "";
  String style = "";
  voteTargetUser = models.Vote.findLastVoteByUserAndInsight(_userId,  _insightUniqueId);
  if(voteTargetUser && voteTargetUser.state.equals(models.Vote.State.AGREE)) {
    style = "background-color: green;";
    lastVote = play.i18n.Messages.get("email.beansightWeeklyUpdate.userVote.agree");
  } else if(voteTargetUser && voteTargetUser.state.equals(models.Vote.State.DISAGREE)) {
    style = "background-color: red;";
    lastVote = play.i18n.Messages.get("email.beansightWeeklyUpdate.userVote.disagree");
  }
/}%
<spans style="${style}">${lastVote}</span>