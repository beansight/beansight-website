<div id="menuBar">
<ul>
    <li><a href="@{Application.index()}"            #{currentMenu 'menuHome' /}         >&{'menuhome'}      </a></li>
    <li><a href="@{Application.experts()}"          #{currentMenu 'menuExperts' /}      >&{'menuexperts'}   </a></li>
    <li><a href="@{Application.create()}"           #{currentMenu 'menuCreate' /}       >&{'menucreate'}    </a></li>
</ul>
</div>