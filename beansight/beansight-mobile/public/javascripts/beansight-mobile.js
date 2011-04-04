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




// Execute scripts after the document creation
$(document).ready(function() {
	

	
});