#{list items:_insights, as:'insight' }
    <li class="insightItem">
    #{if _suggests != null && _suggests.containsKey(insight)}
        #{insightLine insight:insight, suggest: _suggests.get(insight), targetUser:_targetUser /}
    #{/if}
    #{else}
        #{insightLine insight:insight, targetUser:_targetUser /}
    #{/else}
    </li>
#{/list}
