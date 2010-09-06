<h3>My insights</h3>
<ul>
	#{list items:controllers.CurrentUser.getCurrentUser().getCreatedInsights() , as:'insight'} 
		<li>#{insight insight:insight, display:"SMALL" /}</li>
	#{/list}
</ul>
