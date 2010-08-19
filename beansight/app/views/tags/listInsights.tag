<ul>
    #{list items:_insights, as:'insight' }
        <li>
            #{insight insight:insight, currentUser:_currentUser /}
        </li>
    #{/list}
</ul>