#{isConnected}
	<h2>Mes Insights</h2>
	<ul>
		#{list items:controllers.MyInsightsTag.myInsights() , as:'insight'}
			<li><a href="#">${insight.content}</a></li>
		#{/list}
	</ul>	
#{/isConnected}