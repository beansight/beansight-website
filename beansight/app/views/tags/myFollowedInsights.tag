<h3>My followed insights</h3>
<ul>
	#{list items:controllers.CurrentUser.getCurrentUser().getFollowedInsights() , as:'insight'}
           <li>#{insight insight:insight /}</li>
	#{/list}
</ul>	
