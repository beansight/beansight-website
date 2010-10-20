<div id="menuBar">
<ul>
    <li><a href="@{Application.index()}"    #{currentMenu 'menuHome' /} >Home</a></li>
    <li><a href="@{Application.create()}"   #{currentMenu 'menuCreate' /} >Create</a></li>
    <li><a href="@{Application.favorites()}"    #{currentMenu 'menuFavorites' /} >Favorites</a></li>
    <li><a href="@{Application.insights()}"    #{currentMenu 'menuInsights' /} >Insights</a></li>
    <li><a href="@{Application.experts()}"    #{currentMenu 'menuExperts' /} >Experts</a></li>
</ul>
</div>