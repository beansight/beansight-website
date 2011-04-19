*{@param _insight : the insight}*
*{@return a styled <span> that describe the current validation state for this user }*
%{ 
  String validation = "";
  String style = "";
  
  if(_insight.isValidatedTrue()) {
    validation = play.i18n.Messages.get("email.beansightWeeklyUpdate.validation.true");
    style = "color: #7AA13D;";
  }
  if(_insight.isValidatedFalse()) {
    validation = play.i18n.Messages.get("email.beansightWeeklyUpdate.validation.false");;
    style = "color: #A13D3D;";
  }
  if(_insight.isValidatedUnknown()) {
    validation = play.i18n.Messages.get("email.beansightWeeklyUpdate.validation.cantdecide");;
    style = "color: #595C5E;";
  }
/}%
<span style="${style} text-align: center;">${validation}</span>