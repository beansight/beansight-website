*{@param _insight : the insight}*
*{@return a styled <span> that describe the current validation state for this user }*
%{ 
  String validation = "";
  String style = "";
  
  if(_insight.isValidatedTrue()) {
    validation = "True";
    style = "background-color: green;";
  }
  if(_insight.isValidatedFalse()) {
    validation = "False";
    style = "background-color: red;";
  }
  if(_insight.isValidatedUnknown()) {
    validation = "Can't decide";
    style = "background-color: gray;";
  }
/}%
<span style="${style}">${validation}</span>