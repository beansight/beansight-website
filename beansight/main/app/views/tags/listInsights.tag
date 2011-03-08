#{list items:_insights, as:'insight' }
    <li class="insightItem">
        #{insightLine insight:insight, targetUser:_targetUser /}
    </li>
#{/list}
