// Insight Vote

/** Current user agree an insight */
function agree(insightId) {
    $.getJSON(agreeAction({'insightId': insightId}), onVoteSuccess);
}

/** Current user disagree an insight */
function disagree(insightId) {
    $.getJSON(disagreeAction({'insightId': insightId}), onVoteSuccess);
}

/** Callback after a vote is done */
function onVoteSuccess(data) {
    updateAgreeDisagreeCount(data.id, data.updatedAgreeCount, data.updatedDisagreeCount, data.voteState);
}

/** Update the counts of an insight, given new counts */
function updateAgreeDisagreeCount(id, agreeCount, disagreeCount, voteState) {
	var insightline = $("#insight_" + id);
    $("#agreeCount", insightline).text(agreeCount);
    $("#disagreeCount", insightline).text(disagreeCount);
    
    if(voteState == "agree") {
    	insightline.removeClass("voteDisagree");
    	insightline.addClass("voteAgree");
    } else {
    	insightline.addClass("voteDisagree");
    	insightline.removeClass("voteAgree");
    }
}


// Tools
function clearForm( context ) {
    $(':input', context)
     .not(':button, :submit, :reset, :hidden')
     .val('')
     .removeAttr('checked')
     .removeAttr('selected');
}


// Overload jQuery error

/** Called when an AJAX request returns an error */
$("#error").ajaxError(function(event, request, settings){
    $(this).text('Sorry, an error occured during last action.');
});

