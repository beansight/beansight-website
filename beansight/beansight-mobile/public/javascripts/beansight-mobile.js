/** Current user agree an insight */
function agree(insightUniqueId) {
    $.getJSON(agreeAction, {"insightUniqueId": insightUniqueId}, onVoteSuccess);
    
    var insightContainer = $(".insight_" + insightUniqueId);
   	insightContainer.removeClass("voteDisagree").addClass("voteAgree");
   	
   	return false;
}

/** Current user disagree an insight */
function disagree(insightUniqueId) {
    $.getJSON(disagreeAction, {"insightUniqueId": insightUniqueId}, onVoteSuccess);

    var insightContainer = $(".insight_" + insightUniqueId);
   	insightContainer.addClass("voteDisagree").removeClass("voteAgree");
   	
    return false;
}

/** Callback after a vote is done */
function onVoteSuccess(data) {

}

/** Load the insight list */
function getInsights(sort) {
    $.getJSON(getInsightsAction, {sort: sort}, onGetInsightsSuccess);
}

function onGetInsightsSuccess(data) {
	$("#insightList")
		.html( $("#insightTemplate").tmpl( data ) )
		.listview('refresh'); 
}

//function getMoreInsights() {
//    $.getJSON(getInsightsAction, {from: 20} ,onGetMoreInsightsSuccess);
//}
//
//function onGetMoreInsightsSuccess(data) {
//	$("#insightList")
//		.append( $("#insightTemplate").tmpl( data ) )
//		.listview('refresh'); 
//}

// Execute scripts after the document creation
$(document).ready(function() {
	
	$("#filter-upcomming").click(function() {
		getInsights("incoming");
	});
	$("#filter-popularity").click(function() {
		getInsights("trending");
	})
	$("#filter-update").click(function() {
		getInsights("updated");
	});
	
});