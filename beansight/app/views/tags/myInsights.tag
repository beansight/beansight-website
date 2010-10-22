<ul>
	#{list items:controllers.CurrentUser.getCurrentUser().getCreatedInsights() , as:'insight'} 
		<li>#{insight insight:insight /}</li>
	#{/list}
</ul>
