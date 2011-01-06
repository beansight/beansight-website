<div id="menuBar">
<ul>
    <li><a href="@{Application.insights()}"         #{currentMenu 'menuInsights' /}     >&{'menuinsights'}  </a></li>
    <li><a href="@{Application.experts()}"          #{currentMenu 'menuExperts' /}      >&{'menuexperts'}   </a></li>
    <li><a href="@{Application.profile()}"          #{currentMenu 'menuProfile' /}      >&{'menuprofile'}    </a></li>
</ul>
</div>