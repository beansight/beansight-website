<div class="listinsights listexperts">
    <ul class="menulistinsights">
        <li class="sortby">Sort by</li>
        <li class="first"><a href="#">Favorites</a></li>
        <li class="selectme"><a href="#" id="select-tri">select a category</a>
            <ul class="nojavascript" id="ulselect-tri">
                <li><a href="#"><span>Technology</span></a></li>
                <li><a href="#"><span>Web</span></a></li>
                <li class="last"><a href="#"><span>Sport</span></a></li>
            </ul>
        </li>
    </ul>
    
    <!-- expert search -->
    <div class="item-expert">
        <div class="content-expert">
            <form action="#" method="post" class="searchexp">
                <label for="searchexp" class="nodisplay">&{'expertssearch'}</label>
                <input type="text" value="" name="" id="searchexp" class="txtinput"  placeholder="&{'expertssearch'}"/>
                <input type="submit" name="sub-expert" value="&{expertssearchbutton}" class="subinput"/>
            </form>
        </div>
    </div>
    
	<ul>
	    #{list items:_experts, as:'expert' }
	        <li>
	            #{expertLine expert:expert /}
	        </li>
	    #{/list}
	</ul>

    <div class="seeall">
        <p><a href="#">see more</a></p>
    </div>
</div>