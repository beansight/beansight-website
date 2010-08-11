#{isConnected}
	<h3>My followed insights</h3>
	<ul>
		#{list items:controllers.MyFollowedInsightsTag.myFollowedInsights() , as:'insight'}
			<li><a href="#">${insight.content}</a></li>
		#{/list}
	</ul>	
#{/isConnected}