<div id="menuBar">
<ul>
    <li><a href="@{Application.index()}"            #{currentMenu 'menuHome' /}         >&{'menuhome'}      </a></li>
    <li><a href="@{Application.myInsights()}"       #{currentMenu 'menuMyInsights' /}   >&{'menumyinsights'}</a></li>
    <li><a href="@{Application.create()}"           #{currentMenu 'menuCreate' /}       >&{'menucreate'}    </a></li>
    <li><a href="@{Application.insights()}"         #{currentMenu 'menuInsights' /}     >&{'menuinsights'}  </a></li>
    <li><a href="@{Application.experts()}"          #{currentMenu 'menuExperts' /}      >&{'menuexperts'}   </a></li>
</ul>
</div>