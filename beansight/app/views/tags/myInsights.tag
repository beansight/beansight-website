#{isConnected}
	<h3>My insights</h3>
	<ul>
		#{list items:controllers.MyInsightsTag.myInsights() , as:'insight'}
			<li><a href="#">${insight.content}</a></li>
		#{/list}
	</ul>	
#{/isConnected}