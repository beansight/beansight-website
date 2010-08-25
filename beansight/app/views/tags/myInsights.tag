#{isConnected}
	<h3>My insights</h3>
	<ul>
		#{list items:controllers.MyInsightsTag.myInsights() , as:'insight'}
			<li>#{insight insight:insight, display:"SMALL" /}</li>
		#{/list}
	</ul>	
#{/isConnected}