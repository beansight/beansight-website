<ul>
    #{list items:_insights, as:'insight' }
        <li>
            #{insight insight:insight /}
        </li>
    #{/list}
</ul>