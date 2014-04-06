*{@param _value : the value between 0 and 1}*
*{@return the value as a percentage (XX%)}*
%{ double percentage = _value * 100; }%
${percentage.format('##0')}%