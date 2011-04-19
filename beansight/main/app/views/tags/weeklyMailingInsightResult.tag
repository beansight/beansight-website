*{@param _insight : the insight}*
*{@return a styled <span> that describe the current validation state for this user }*
%{ 
  String validation = "";
  String style = "";
  
  if(_insight.isValidatedTrue()) {
    validation = play.i18n.Messages.get("email.beansightWeeklyUpdate.validation.true");
    style = "background-color: green;";
  }
  if(_insight.isValidatedFalse()) {
    validation = play.i18n.Messages.get("email.beansightWeeklyUpdate.validation.false");;
    style = "background-color: red;";
  }
  if(_insight.isValidatedUnknown()) {
    validation = play.i18n.Messages.get("email.beansightWeeklyUpdate.validation.cantdecide");;
    style = "background-color: gray;";
  }
/}%
<span style="${style}">${validation}</span>