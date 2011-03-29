<!-- MENU NAV -->
<h2 class="azurme"><span class="cuf-grb">&{'menutitle'}</span></h2>
<ul class="menuleft">
    <li><a href="@{Application.insights()}" class="cuf-grb #{currentMenu 'menuInsights' /}">&{'menuinsights'}</a></li>
    <li><a href="@{Application.insights('trending', 0, 'all', null, true)}" class="cuf-grb #{currentMenu 'menuClosedInsights' /}">&{'menuClosedInsights'}</a></li>
#{isConnected}    
    <li><a href="@{Application.profile()}" class="cuf-grb #{currentMenu 'menuProfile' /}">&{'menuprofile'}</a></li>
#{/isConnected}
    <li><a href="@{Application.experts()}" class="cuf-grb #{currentMenu 'menuExperts' /}">&{'menuexperts'}</a></li>
</ul>