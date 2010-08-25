#{isConnected}
	<h3>My followed insights</h3>
	<ul>
		#{list items:controllers.MyFollowedInsightsTag.myFollowedInsights() , as:'insight'}
            <li>#{insight insight:insight, display:"SMALL" /}</li>
		#{/list}
	</ul>	
#{/isConnected}