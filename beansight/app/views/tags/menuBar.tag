<div id="menuBar">
<ul>
    <li><a href="@{Application.index()}"    #{currentMenu 'menuHome' /} >Home</a></li>
    <li><a href="@{Application.create()}"   #{currentMenu 'menuCreate' /} >Create</a></li>
    <li><a href="@{Application.index()}"    #{currentMenu 'menuFavorites' /} >Favorites</a></li>
    <li><a href="@{Application.index()}"    #{currentMenu 'menuInsights' /} >Insights</a></li>
    <li><a href="@{Application.index()}"    #{currentMenu 'menuExperts' /} >Experts</a></li>
</ul>
</div>